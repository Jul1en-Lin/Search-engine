package com.searchengine.service;

import java.util.List;

/**
 * 仓库同步服务接口
 * 负责从Gitee同步仓库数据到本地数据库
 */
public interface RepositorySyncService {

    /**
     * 同步用户/组织的仓库数据
     * 根据用户名先尝试获取用户仓库，如果为空则获取组织仓库
     * 为每个仓库获取README内容并批量保存到数据库
     *
     * @param username 用户名或组织名
     * @return 同步的仓库数量
     */
    int syncUserRepositories(String username);
}
