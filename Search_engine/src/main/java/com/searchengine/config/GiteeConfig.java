package com.searchengine.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Gitee API配置类
 * 用于配置Gitee API的基础信息
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "gitee")
public class GiteeConfig {
    
    /**
     * Gitee API基础地址
     */
    private String apiBaseUrl = "https://gitee.com/api/v5";
    
    /**
     * Gitee访问令牌（可选，用于提高API调用限制）
     */
    private String accessToken;
    
    /**
     * 每页获取的数据量（默认20，最大100）
     */
    private Integer perPage = 100;
    
    /**
     * 请求超时时间（毫秒）
     */
    private Integer timeout = 10000;
}

