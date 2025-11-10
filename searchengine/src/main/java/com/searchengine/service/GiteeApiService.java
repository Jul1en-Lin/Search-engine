package com.searchengine.service;

import com.searchengine.dto.GiteeRepositoryDTO;
import com.searchengine.dto.GiteeUserDTO;

import java.util.List;

/**
 * Gitee API服务接口
 * 定义与Gitee API交互的方法
 */
public interface GiteeApiService {
    
    /**
     * 搜索用户
     * 根据关键词搜索Gitee用户
     * 
     * @param keyword 搜索关键词
     * @return 用户名称列表
     */
    List<String> searchUsers(String keyword);
    
    /**
     * 获取用户的所有仓库（支持分页，自动获取全量数据）
     * 根据用户名获取该用户的所有仓库信息
     * 
     * @param username 用户名
     * @return 仓库列表，包含fullName、htmlUrl、readMe、repositoryId字段
     */
    List<GiteeRepositoryDTO> getUserRepositories(String username);
    
    /**
     * 获取组织的所有仓库（支持分页，自动获取全量数据）
     * 根据组织名获取该组织的所有仓库信息
     * 
     * @param orgName 组织名称
     * @return 仓库列表，包含fullName、htmlUrl、readMe、repositoryId字段
     */
    List<GiteeRepositoryDTO> getOrgRepositories(String orgName);
    
    /**
     * 获取仓库的README内容
     * 根据所有者名称和仓库名称获取仓库的README内容
     * 
     * @param owner 所有者名称
     * @param repo 仓库名称
     * @return 包含README字段的仓库对象
     */
    GiteeRepositoryDTO getRepositoryReadme(String owner, String repo);
}

