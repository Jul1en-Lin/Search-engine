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
            // 1. 获取倒排索引搜索结果
            List<Long> indexResults = invertedIndex.search(query);
            List<Long> indexIds = indexResults.stream()
                    .limit(2000)
                    .collect(Collectors.toList());

            // 2. 调用Python服务获取语义搜索结果
            String pythonServiceUrl = PYTHON_SERVICE_URL + "?q=" + query;
            SearchResult[] semanticResults = restTemplate.getForObject(pythonServiceUrl, SearchResult[].class);
            List<Long> semanticsIds = Arrays.stream(semanticResults)
                    .map(SearchResult::getId)
                    .collect(Collectors.toList());

            // 3. 求交集
            Set<Long> indexIdSet = new HashSet<>(indexIds);
            List<Long> resultIds = semanticsIds.stream()
                    .filter(indexIdSet::contains)
                    .collect(Collectors.toList());

            // 交集为空，直接返回
            if (resultIds.isEmpty()) {
                return Collections.emptyList();
            }
            // 4. 按顺序查询仓库信息
            List<Repository> repositoryEntityList = repositoryMapper.selectByIdsInOrder(resultIds);

            // 5. 对readme字段进行截取
            List<Repository> result = new ArrayList<>();
            for (Repository repository :repositoryEntityList) {
                if (repository.getReadme().length() > 100) {
                    repository.setReadme(repository.getReadme().substring(0, 100));
                }
                result.add(repository);
            }
            return result;

        } catch (Exception e) {
            // log.error("搜索失败", e);
            return Collections.emptyList();
        }
    }
}