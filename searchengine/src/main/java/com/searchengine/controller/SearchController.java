package com.searchengine.controller;

import com.searchengine.entity.Repository;
import com.searchengine.service.IndexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 搜索控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchController {
    private final IndexService indexService;

    /**
     * 根据关键词搜索代码仓库
     * @param query 查询关键词
     * @return 仓库列表
     */
    @GetMapping("/search")
    public List<Repository> search(@RequestParam String query) {
        log.info("收到搜索请求，关键词: {}", query);
        List<Repository> results = indexService.search(query);
        log.info("搜索完成，找到 {} 个结果", results.size());
        return results;
    }
}