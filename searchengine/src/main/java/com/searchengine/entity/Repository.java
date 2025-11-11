package com.searchengine.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
/**
 * 仓库实体类
 */
@Data
@TableName("repository")
public class Repository {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 仓库完整名称
     */
    private String fullName;

    /**
     * 仓库HTML访问地址
     */
    private String htmlUrl;

    /**
     * 仓库README内容
     */
    private String readme;

    /**
     * Gitee仓库ID
     */
    private Long repositoryId;
}
