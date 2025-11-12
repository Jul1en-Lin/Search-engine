package com.searchengine.service;

import com.searchengine.SearchEngineApplication;
import com.searchengine.dto.GiteeRepositoryDTO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * Gitee API服务测试类
 * 用于测试Gitee API服务的功能
 */
@Slf4j
@SpringBootTest(classes = SearchEngineApplication.class)
class GiteeApiServiceTest {
    
    @Autowired
    private GiteeApiService giteeApiService;
    
    /**
     * 测试搜索用户功能
     * 单元测试通过
     */
    @Test
    void testSearchUsers() {
        giteeApiService.searchUsers("bite").forEach(name -> {
            System.out.println(name);
        });
    }
    
    /**
     * 测试获取用户仓库功能
     * 单元测试已通过
     */
    @Test
    void testGetUserRepositories() {
        log.info("开始测试获取用户仓库功能");
        List<GiteeRepositoryDTO> repos = giteeApiService.getUserRepositories("Cary_C");
        log.info("获取到 {} 个仓库", repos.size());
        repos.forEach(repo -> {
            log.info("仓库：{} - {}", repo.getFullName(), repo.getHtmlUrl());
            if (repo.getReadMe() != null && !repo.getReadMe().isEmpty()) {
                log.info("  README长度：{} 字符", repo.getReadMe().length());
                // 如果需要查看README内容，可以取消下面的注释
                // log.info("  README内容预览：{}", 
                //     repo.getReadMe().substring(0, Math.min(200, repo.getReadMe().length())));
            }
        });
    }
    
    /**
     * 测试获取组织仓库功能
     * 单元测试已通过
     */
    @Test
    void testGetOrgRepositories() {
        log.info("开始测试获取组织仓库功能");
        List<GiteeRepositoryDTO> repos = giteeApiService.getOrgRepositories("dromara");
        log.info("获取到 {} 个仓库", repos.size());
        repos.forEach(repo -> {
            log.info("仓库：{} - {}", repo.getFullName(), repo.getHtmlUrl());
            if (repo.getReadMe() != null && !repo.getReadMe().isEmpty()) {
                log.info("  README长度：{} 字符", repo.getReadMe().length());
                // 如果需要查看README内容，可以取消下面的注释
                 log.info("  README内容预览：{}",
                     repo.getReadMe().substring(0, Math.min(200, repo.getReadMe().length())));
            }
        });
    }
    
    /**
     * 测试获取仓库README功能
     * 单元测试通过
     */
    @Test
    void testGetRepositoryReadme() {
        log.info("开始测试获取仓库README功能");
        GiteeRepositoryDTO repo = giteeApiService.getRepositoryReadme("dromara", "yft-design");
        log.info("仓库信息：{}", repo.getFullName());
        log.info("仓库ID：{}", repo.getRepositoryId());
        log.info("仓库URL：{}", repo.getHtmlUrl());
        
        if (repo.getReadMe() != null && !repo.getReadMe().isEmpty()) {
            log.info("README长度：{} 字符", repo.getReadMe().length());
            log.info("========== README内容开始 ==========");
            System.out.println(repo.getReadMe());
            log.info("========== README内容结束 ==========");
        } else {
            log.warn("README内容为空");
        }
    }
}

