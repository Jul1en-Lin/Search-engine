package com.searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gitee仓库数据传输对象
 * 包含仓库的基本信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiteeRepositoryDTO {
    
    /**
     * 仓库完整名称（格式：owner/repo）
     */
    private String fullName;
    
    /**
     * 仓库的HTML访问地址
     */
    private String htmlUrl;
    
    /**
     * 仓库的README内容
     */
    private String readMe;
    
    /**
     * 仓库ID
     */
    private Long repositoryId;
}

