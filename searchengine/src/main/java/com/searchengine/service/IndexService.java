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
        // 搜索文档ID
        List<Long> docIds = invertedIndex.search(query);

        // 只要前1000个仓库
        docIds = docIds.stream().limit(1000).collect(Collectors.toList());

        // 获取仓库信息
        return docIds.stream()
                .map(repositoryMapper::selectById)
                .filter(repo -> repo != null)
                .toList();
    }
}