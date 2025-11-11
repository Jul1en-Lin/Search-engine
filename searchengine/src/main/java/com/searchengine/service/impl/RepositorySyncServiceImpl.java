package com.searchengine.service.impl;

import com.searchengine.dto.GiteeRepositoryDTO;
import com.searchengine.entity.Repository;
import com.searchengine.index.InvertedIndex;
import com.searchengine.mapper.RepositoryMapper;
import com.searchengine.service.GiteeApiService;
import com.searchengine.service.RepositorySyncService;
import com.searchengine.utils.JiebaTokenizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 仓库同步服务实现类
 * 实现从Gitee同步仓库数据到本地数据库的逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RepositorySyncServiceImpl implements RepositorySyncService {

    private final GiteeApiService giteeApiService;
    private final RepositoryMapper repositoryMapper;
    private final JiebaTokenizer jiebaTokenizer;
    private final InvertedIndex invertedIndex;

    /**
     * 同步用户/组织的仓库数据
     * 使用单线程方式：先获取仓库列表，然后逐个获取README，最后批量保存
     *
     * @param username 用户名或组织名
     * @return 同步的仓库数量
     */
    @Override
    public int syncUserRepositories(String username) {
        log.info("开始同步用户/组织仓库数据：{}", username);

        try {
            // 步骤1：获取仓库列表
            // 优先尝试获取用户仓库，如果为空则获取组织仓库
            List<GiteeRepositoryDTO> repositories = getRepositories(username);
            if (repositories.isEmpty()) {
                log.info("用户 {} 没有仓库数据", username);
                return 0;
            }

            log.info("获取到 {} 个仓库，开始获取README内容", repositories.size());

            // 步骤2：为每个仓库获取README内容
            enrichRepositoriesWithReadme(repositories, username);

            // 步骤3：转换为实体对象并批量保存
            List<Repository> repositoryEntities = convertToEntities(repositories);
            int savedCount = batchSaveRepositories(repositoryEntities);

            log.info("成功同步 {} 个仓库数据", savedCount);
            return savedCount;

        } catch (Exception e) {
            log.error("同步用户/组织仓库数据失败：{}", username, e);
            throw new RuntimeException("同步仓库数据失败：" + e.getMessage(), e);
        }
    }

    /**
     * 获取仓库列表
     * 先尝试获取用户仓库，如果为空则获取组织仓库
     *
     * @param username 用户名或组织名
     * @return 仓库列表
     */
    private List<GiteeRepositoryDTO> getRepositories(String username) {
        log.debug("尝试获取用户仓库：{}", username);

        // 首先尝试获取用户仓库
        List<GiteeRepositoryDTO> repositories = giteeApiService.getUserRepositories(username);

        // 如果用户仓库为空，尝试获取组织仓库
        if (repositories.isEmpty()) {
            log.debug("用户仓库为空，尝试获取组织仓库：{}", username);
            repositories = giteeApiService.getOrgRepositories(username);
        }

        log.debug("获取到 {} 个仓库", repositories.size());
        return repositories;
    }

    /**
     * 为仓库列表填充README内容
     * 使用单线程方式逐个获取README
     *
     * @param repositories 仓库列表
     * @param username 用户名或组织名
     */
    private void enrichRepositoriesWithReadme(List<GiteeRepositoryDTO> repositories, String username) {
        for (GiteeRepositoryDTO repository : repositories) {
            if (repository.getFullName() != null && !repository.getFullName().trim().isEmpty()) {
                try {
                    // 从fullName中解析owner和repo
                    String[] parts = repository.getFullName().split("/");
                    if (parts.length == 2) {
                        String owner = parts[0];
                        String repo = parts[1];

                        log.debug("获取仓库README：{}/{}", owner, repo);

                        // 调用API获取README内容
                        GiteeRepositoryDTO repoWithReadme = giteeApiService.getRepositoryReadme(owner, repo);

                        // 填充README内容
                        if (repoWithReadme != null && repoWithReadme.getReadMe() != null) {
                            repository.setReadMe(repoWithReadme.getReadMe());
                            log.debug("成功获取README，长度：{} 字符", repoWithReadme.getReadMe().length());
                        } else {
                            log.debug("README内容为空：{}/{}", owner, repo);
                        }
                    } else {
                        log.warn("仓库名称格式不正确：{}", repository.getFullName());
                    }
                } catch (Exception e) {
                    log.warn("获取仓库README失败：{}，错误：{}", repository.getFullName(), e.getMessage());
                    // 继续处理下一个仓库，不中断整个流程
                }
            }
        }
    }

    /**
     * 将GiteeRepositoryDTO转换为Repository实体
     *
     * @param giteeRepos Gitee仓库DTO列表
     * @return Repository实体列表
     */
    private List<Repository> convertToEntities(List<GiteeRepositoryDTO> giteeRepos) {
        List<Repository> entities = new ArrayList<>();

        for (GiteeRepositoryDTO giteeRepo : giteeRepos) {
            Repository entity = new Repository();
            entity.setFullName(giteeRepo.getFullName());
            entity.setHtmlUrl(giteeRepo.getHtmlUrl());
            entity.setReadme(giteeRepo.getReadMe());
            entity.setRepositoryId(giteeRepo.getRepositoryId());

            entities.add(entity);
        }

        log.debug("转换了 {} 个实体对象", entities.size());
        return entities;
    }

    /**
     * 批量保存仓库数据到数据库
     *
     * @param repositories 仓库实体列表
     * @return 保存成功的数量
     */
    private int batchSaveRepositories(List<Repository> repositories) {
        if (repositories.isEmpty()) {
            return 0;
        }

        try {
            log.debug("开始批量保存 {} 个仓库", repositories.size());

            // 使用MyBatis Plus的批量插入方法
            int result = repositoryMapper.batchInsert(repositories);

            log.debug("批量保存完成，影响行数：{}", result);
            return result;

        } catch (Exception e) {
            log.error("批量保存仓库数据失败", e);
            throw new RuntimeException("批量保存仓库数据失败：" + e.getMessage(), e);
        }
    }


}
