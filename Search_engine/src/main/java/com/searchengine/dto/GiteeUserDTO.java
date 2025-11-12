package com.searchengine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Gitee用户数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GiteeUserDTO {
    
    /**
     * 用户登录名
     */
    private String login;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名称
     */
    private String name;
}

