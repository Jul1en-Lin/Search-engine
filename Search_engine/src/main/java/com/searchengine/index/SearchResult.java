package com.searchengine.index;

import lombok.Data;

/**
 * 搜索结果
 */
@Data
public class SearchResult {
    /**
     * 仓库ID
     */
    private Long id;

    /**
     * 相似度分数
     */
    private Double score;
}