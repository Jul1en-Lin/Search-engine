package com.searchengine.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate配置类
 * 配置HTTP客户端用于调用外部API
 */
@Configuration
public class RestTemplateConfig {
    
    /**
     * 创建RestTemplate Bean
     * 配置超时时间等参数
     * 
     * @param giteeConfig Gitee配置
     * @return RestTemplate实例
     */
    @Bean
    public RestTemplate restTemplate(GiteeConfig giteeConfig) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(giteeConfig.getTimeout());
        factory.setReadTimeout(giteeConfig.getTimeout());
        
        return new RestTemplate(factory);
    }
    
    /**
     * 创建ObjectMapper Bean
     * 用于JSON序列化和反序列化
     * 
     * @return ObjectMapper实例
     */
    @Bean
    public com.fasterxml.jackson.databind.ObjectMapper objectMapper() {
        return new com.fasterxml.jackson.databind.ObjectMapper();
    }
}

