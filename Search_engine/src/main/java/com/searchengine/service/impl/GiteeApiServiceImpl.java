package com.searchengine.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchengine.config.GiteeConfig;
import com.searchengine.dto.GiteeRepositoryDTO;
import com.searchengine.dto.GiteeUserDTO;
import com.searchengine.service.GiteeApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gitee API服务实现类
 * 实现与Gitee API的具体交互逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class GiteeApiServiceImpl implements GiteeApiService {
    
    private final GiteeConfig giteeConfig;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键词
     * @return 用户名称列表
     */
    @Override
    public List<String> searchUsers(String keyword) {
        log.info("开始搜索用户，关键词：{}", keyword);
        
        List<String> userNames = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        
        while (hasMore) {
            try {
                // 构建请求URL
                String url = buildUrl("/search/users", 
                    "q", keyword,
                    "page", String.valueOf(page),
                    "per_page", String.valueOf(giteeConfig.getPerPage()));
                
                log.debug("请求URL：{}", url);
                
                // 发送请求
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    buildHttpEntity(),
                    String.class
                );
                
                log.debug("API响应状态码：{}", response.getStatusCode());
                log.debug("API响应内容：{}", response.getBody());
                
                // 解析响应 - Gitee API返回的格式可能是数组或包含items的对象
                JsonNode rootNode = objectMapper.readTree(response.getBody());
                JsonNode itemsNode = null;
                
                // 检查响应格式：可能是直接数组，也可能是包含items的对象
                if (rootNode.isArray()) {
                    // 直接是数组格式
                    itemsNode = rootNode;
                } else if (rootNode.has("items")) {
                    // 包含items字段的对象格式
                    itemsNode = rootNode.get("items");
                } else if (rootNode.has("data")) {
                    // 可能包含data字段
                    itemsNode = rootNode.get("data");
                }
                
                if (itemsNode != null && itemsNode.isArray() && itemsNode.size() > 0) {
                    int currentPageSize = itemsNode.size();
                    log.debug("解析到 {} 个用户", currentPageSize);
                    
                    // 直接从JSON节点提取用户名称，兼容不同的字段名
                    for (JsonNode userNode : itemsNode) {
                        String userName = null;
                        
                        // 尝试多种可能的字段名：login、name、username、user
                        if (userNode.has("login")) {
                            userName = userNode.get("login").asText();
                        } else if (userNode.has("name")) {
                            userName = userNode.get("name").asText();
                        } else if (userNode.has("username")) {
                            userName = userNode.get("username").asText();
                        } else if (userNode.has("user")) {
                            JsonNode userField = userNode.get("user");
                            if (userField.has("login")) {
                                userName = userField.get("login").asText();
                            } else if (userField.has("name")) {
                                userName = userField.get("name").asText();
                            }
                        }
                        
                        if (userName != null && !userName.trim().isEmpty()) {
                            userNames.add(userName.trim());
                        } else {
                            log.warn("无法从用户节点提取用户名，节点内容：{}", userNode.toString());
                        }
                    }
                    
                    log.debug("当前页提取到 {} 个用户名，累计 {} 个", 
                        currentPageSize, userNames.size());
                    
                    // 检查是否还有更多数据
                    // Gitee API可能不返回total_count，需要根据返回的数据量判断
                    hasMore = currentPageSize == giteeConfig.getPerPage();
                    
                    // 如果有total_count字段，使用它来判断
                    if (rootNode.has("total_count")) {
                        int totalCount = rootNode.get("total_count").asInt();
                        hasMore = userNames.size() < totalCount && currentPageSize == giteeConfig.getPerPage();
                    }
                    
                    page++;
                } else {
                    log.debug("未找到用户数据或数据为空");
                    hasMore = false;
                }
                
            } catch (Exception e) {
                log.error("搜索用户时发生错误，页码：{}，错误信息：{}", page, e.getMessage(), e);
                // 打印完整的错误堆栈以便调试
                if (log.isDebugEnabled()) {
                    e.printStackTrace();
                }
                hasMore = false;
            }
        }
        
        log.info("搜索完成，找到 {} 个用户", userNames.size());
        return userNames;
    }
    
    /**
     * 获取用户的所有仓库（支持分页，自动获取全量数据）
     * 
     * @param username 用户名
     * @return 仓库列表
     */
    @Override
    public List<GiteeRepositoryDTO> getUserRepositories(String username) {
        log.info("开始获取用户仓库，用户名：{}", username);
        return fetchAllRepositories("/users/" + username + "/repos");
    }
    
    /**
     * 获取组织的所有仓库（支持分页，自动获取全量数据）
     * 
     * @param orgName 组织名称
     * @return 仓库列表
     */
    @Override
    public List<GiteeRepositoryDTO> getOrgRepositories(String orgName) {
        log.info("开始获取组织仓库，组织名：{}", orgName);
        return fetchAllRepositories("/orgs/" + orgName + "/repos");
    }
    
    /**
     * 获取仓库的README内容
     * 
     * @param owner 所有者名称
     * @param repo 仓库名称
     * @return 包含README字段的仓库对象
     */
    @Override
    public GiteeRepositoryDTO getRepositoryReadme(String owner, String repo) {
        log.info("开始获取仓库README，所有者：{}，仓库：{}", owner, repo);
        
        try {
            // 构建请求URL
            String url = buildUrl("/repos/" + owner + "/" + repo + "/readme");
            
            log.debug("请求URL：{}", url);
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                buildHttpEntity(),
                String.class
            );
            
            // 解析响应
            JsonNode readmeNode = objectMapper.readTree(response.getBody());
            
            // 构建仓库DTO
            GiteeRepositoryDTO repository = new GiteeRepositoryDTO();
            repository.setFullName(owner + "/" + repo);
            repository.setHtmlUrl("https://gitee.com/" + owner + "/" + repo);
            
            // 解析README内容（base64解码）
            if (readmeNode.has("content")) {
                String base64Content = readmeNode.get("content").asText();
                // 移除base64编码中的换行符
                base64Content = base64Content.replaceAll("\\s", "");
                // Base64解码
                byte[] decodedBytes = java.util.Base64.getDecoder().decode(base64Content);
                repository.setReadMe(new String(decodedBytes, java.nio.charset.StandardCharsets.UTF_8));
            }
            
            // 获取仓库ID（需要额外调用仓库信息接口）
            try {
                String repoUrl = buildUrl("/repos/" + owner + "/" + repo);
                ResponseEntity<String> repoResponse = restTemplate.exchange(
                    repoUrl,
                    HttpMethod.GET,
                    buildHttpEntity(),
                    String.class
                );
                JsonNode repoNode = objectMapper.readTree(repoResponse.getBody());
                if (repoNode.has("id")) {
                    repository.setRepositoryId(repoNode.get("id").asLong());
                }
            } catch (Exception e) {
                log.warn("获取仓库ID失败", e);
            }
            
            log.info("成功获取仓库README");
            return repository;
            
        } catch (Exception e) {
            log.error("获取仓库README时发生错误", e);
            throw new RuntimeException("获取仓库README失败：" + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有仓库（支持分页，自动获取全量数据）
     * 内部方法，用于统一处理分页逻辑
     * 
     * @param endpoint API端点路径
     * @return 仓库列表
     */
    private List<GiteeRepositoryDTO> fetchAllRepositories(String endpoint) {
        List<GiteeRepositoryDTO> allRepositories = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;
        
        while (hasMore) {
            try {
                // 构建请求URL
                String url = buildUrl(endpoint,
                    "page", String.valueOf(page),
                    "per_page", String.valueOf(giteeConfig.getPerPage()));
                
                log.debug("请求URL：{}，页码：{}", url, page);
                
                // 发送请求
                ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    buildHttpEntity(),
                    String.class
                );
                
                // 解析响应
                List<GiteeRepositoryDTO> repositories = parseRepositoryList(response.getBody());
                
                if (repositories != null && !repositories.isEmpty()) {
                    allRepositories.addAll(repositories);
                    
                    // 检查是否还有更多数据
                    hasMore = repositories.size() == giteeConfig.getPerPage();
                    page++;
                    
                    log.debug("已获取 {} 个仓库，累计 {} 个", repositories.size(), allRepositories.size());
                } else {
                    hasMore = false;
                }
                
            } catch (Exception e) {
                log.error("获取仓库列表时发生错误，页码：{}", page, e);
                hasMore = false;
            }
        }
        
        // 为每个仓库获取README内容
        log.info("开始为 {} 个仓库获取README内容", allRepositories.size());
        for (GiteeRepositoryDTO repo : allRepositories) {
            if (repo.getFullName() != null && repo.getReadMe() == null) {
                String[] parts = repo.getFullName().split("/");
                if (parts.length == 2) {
                    try {
                        GiteeRepositoryDTO repoWithReadme = getRepositoryReadme(parts[0], parts[1]);
                        repo.setReadMe(repoWithReadme.getReadMe());
                    } catch (Exception e) {
                        log.warn("获取仓库 {} 的README失败", repo.getFullName(), e);
                    }
                }
            }
        }
        
        log.info("获取完成，共 {} 个仓库", allRepositories.size());
        return allRepositories;
    }
    
    /**
     * 解析仓库列表JSON响应
     * 
     * @param jsonBody JSON响应体
     * @return 仓库列表
     */
    private List<GiteeRepositoryDTO> parseRepositoryList(String jsonBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(jsonBody);
            List<GiteeRepositoryDTO> repositories = new ArrayList<>();
            
            if (rootNode.isArray()) {
                for (JsonNode repoNode : rootNode) {
                    GiteeRepositoryDTO repo = new GiteeRepositoryDTO();
                    
                    // 提取fullName
                    if (repoNode.has("full_name")) {
                        repo.setFullName(repoNode.get("full_name").asText());
                    }
                    
                    // 提取htmlUrl
                    if (repoNode.has("html_url")) {
                        repo.setHtmlUrl(repoNode.get("html_url").asText());
                    }
                    
                    // 提取repositoryId
                    if (repoNode.has("id")) {
                        repo.setRepositoryId(repoNode.get("id").asLong());
                    }
                    
                    repositories.add(repo);
                }
            }
            
            return repositories;
        } catch (Exception e) {
            log.error("解析仓库列表失败", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * 构建请求URL
     * 
     * @param endpoint API端点
     * @param params 查询参数（键值对）
     * @return 完整的URL
     */
    private String buildUrl(String endpoint, String... params) {
        UriComponentsBuilder builder = UriComponentsBuilder
            .fromHttpUrl(giteeConfig.getApiBaseUrl() + endpoint);
        
        // 添加查询参数
        for (int i = 0; i < params.length; i += 2) {
            if (i + 1 < params.length) {
                builder.queryParam(params[i], params[i + 1]);
            }
        }
        
        // 添加访问令牌（如果配置了）
        if (giteeConfig.getAccessToken() != null && !giteeConfig.getAccessToken().isEmpty()) {
            builder.queryParam("access_token", giteeConfig.getAccessToken());
        }
        
        return builder.toUriString();
    }
    
    /**
     * 构建HTTP请求实体
     * 
     * @return HTTP实体对象
     */
    private HttpEntity<String> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/json");
        headers.set("User-Agent", "SearchEngine/1.0");
        return new HttpEntity<>(headers);
    }
}

