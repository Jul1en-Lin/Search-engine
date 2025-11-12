package com.searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * Gitee API响应包装类
 * 用于封装Gitee API返回的分页数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiteeApiResponse<T> {
    
    /**
     * 数据列表
     */
    private List<T> data;
    
    /**
     * 总记录数
     */
    private Integer totalCount;
    
    /**
     * 当前页码
     */
    private Integer currentPage;
    
    /**
     * 每页数量
     */
    private Integer perPage;
    
    /**
     * 总页数
     */
    private Integer totalPages;
}

