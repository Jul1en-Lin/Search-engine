package com.searchengine.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.searchengine.entity.Repository;
import com.searchengine.index.IndexPersistence;
import com.searchengine.index.InvertedIndex;
import com.searchengine.mapper.RepositoryMapper;
import com.searchengine.index.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 索引服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IndexService {
    private final InvertedIndex invertedIndex;
    private final RepositoryMapper repositoryMapper;
    private final IndexPersistence indexStorage;
    private final RestTemplate restTemplate;
    private static final String PYTHON_SERVICE_URL = "http://localhost:8080";

    /**
     * 加载停用词
     * @param filePath 停用词的文件路径
     * 在服务类实现预加载，并不需要直接在InvertedIndex实现类中做，实现解耦
     * 后续在启动类中跟倒排索引一样实现预加载
     * 优化性能 避免每次启动都需要读取停止词文件
     */
    public void loadStopWords(String filePath) {
        invertedIndex.loadStopWords(filePath);
    }

    /**
     * 构建倒排索引
     */
    public void buildIndex() {
        log.info("开始构建倒排索引...");

        // 清空现有索引
        invertedIndex.clear();

        // 分批加载数据
        int pageSize = 1000;
        long current = 1;
        long total = 0;

        while (true) {
            // 使用MyBatis-Plus的分页查询
            Page<Repository> page = new Page<>(current, pageSize);
            Page<Repository> result = repositoryMapper.selectPage(page,
                    new LambdaQueryWrapper<Repository>()
                            .isNotNull(Repository::getReadme));

            List<Repository> repositories = result.getRecords();
            if (repositories.isEmpty()) {
                break;
            }

            // 构建文档映射
            Map<Long, String> documents = new HashMap<>();
            for (Repository repo : repositories) {
                documents.put(repo.getId(), repo.getReadme());
            }

            // 批量添加到索引
            invertedIndex.addDocuments(documents);

            total += repositories.size();
            log.info("已处理 {} 条数据", total);

            // 判断是否还有下一页
            if (current >= result.getPages()) {
                break;
            }
            current++;
        }

        log.info("倒排索引构建完成，共 {} 个词条", invertedIndex.size());

        // 保存索引到文件
        indexStorage.saveIndex(invertedIndex.getIndex());
    }

    /**
     * 加载索引
     */
    public void loadIndex() {
        log.info("开始加载倒排索引...");
        invertedIndex.setIndex(indexStorage.loadIndex());
        log.info("倒排索引加载完成，共 {} 个词条", invertedIndex.size());
    }

    /**
     * 搜索仓库
     * @param query 查询词
     * @return 搜索结果列表
     */
    public List<Repository> search(String query) {
        try {
            log.info("开始搜索，关键词: {}", query);
            
            // 1. 获取倒排索引搜索结果
            List<Long> indexResults = invertedIndex.search(query);
            List<Long> indexIds = indexResults.stream()
                    .limit(2000)
                    .collect(Collectors.toList());
            
            log.info("倒排索引搜索到 {} 个结果", indexIds.size());

            List<Long> resultIds;

            // 2. 尝试调用Python服务获取语义搜索结果
            try {
                String pythonServiceUrl = PYTHON_SERVICE_URL + "?q=" + java.net.URLEncoder.encode(query, "UTF-8");
                log.debug("调用Python语义搜索服务: {}", pythonServiceUrl);
                
                SearchResult[] semanticResults = restTemplate.getForObject(pythonServiceUrl, SearchResult[].class);
                
                if (semanticResults == null || semanticResults.length == 0) {
                    log.warn("Python服务返回空结果，使用倒排索引结果");
                    resultIds = indexIds;
                } else {
                    List<Long> semanticsIds = Arrays.stream(semanticResults)
                            .map(SearchResult::getId)
                            .collect(Collectors.toList());
                    
                    log.info("语义搜索返回 {} 个结果", semanticsIds.size());

                    // 3. 求交集（倒排索引结果与语义搜索结果的交集）
                    Set<Long> indexIdSet = new HashSet<>(indexIds);
                    resultIds = semanticsIds.stream()
                            .filter(indexIdSet::contains)
                            .collect(Collectors.toList());

                    log.info("交集结果: {} 个", resultIds.size());
                }

            } catch (Exception e) {
                log.warn("Python语义搜索服务不可用，将使用倒排索引搜索结果: {}", e.getMessage());
                // Python服务不可用时，降级使用倒排索引结果
                resultIds = indexIds;
            }

            // 交集为空，直接返回
            if (resultIds.isEmpty()) {
                log.info("搜索完成，无匹配结果");
                return Collections.emptyList();
            }

            // 4. 按顺序查询仓库信息
            List<Repository> repositoryEntityList = repositoryMapper.selectByIdsInOrder(resultIds);

            // 5. 对readme字段进行截取
            List<Repository> result = new ArrayList<>();
            for (Repository repository : repositoryEntityList) {
                if (repository.getReadme() != null && repository.getReadme().length() > 100) {
                    repository.setReadme(repository.getReadme().substring(0, 100));
                }
                result.add(repository);
            }

            log.info("搜索完成，返回 {} 个结果", result.size());
            return result;

        } catch (Exception e) {
            log.error("搜索失败", e);
            return Collections.emptyList();
        }
    }

}