# 系统架构文档 (System Architecture)

## 架构图 (Architecture Diagram)

<!-- 在此插入系统架构图 -->
```
[架构图占位区域]
```

## 核心组件说明 (Core Components)

<!-- 描述系统的核心组件及其职责 -->

| 组件名称 | 职责描述 | 关键文件/模块 |
|---------|---------|--------------|
| Frontend Server | 提供 Web 界面，处理静态资源请求 | front/server.js, front/index.html |
| Search Controller | 接收搜索请求，协调搜索流程 | SearchController.java |
| IndexService | 协调倒排索引与语义搜索 | IndexService.java |
| InvertedIndex | 基于Jieba分词的倒排索引实现 | InvertedIndex.java |
| Python Semantic Service | 基于FAISS的语义向量搜索 | python/search_server.py |
| GiteeApiService | 从Gitee同步仓库数据 | GiteeApiServiceImpl.java |

## 技术栈清单 (Tech Stack)

| 类别 | 技术选型 | 版本 | 用途说明 |
|-----|---------|-----|---------|
| 编程语言 | Java | 17 | 后端主要开发语言 |
| 编程语言 | Python | - | 语义搜索服务 |
| 编程语言 | JavaScript | Node.js | 前端服务器 |
| 框架 | Spring Boot | 3.3.3 | 后端Web框架 |
| 框架 | MyBatis-Plus | 3.5.5 | ORM框架 |
| 数据库 | MySQL | - | 存储仓库数据 |
| 索引 | 自定义倒排索引 | - | 关键词搜索 |
| 向量索引 | FAISS | - | 语义搜索 |
| 分词工具 | Jieba | 1.0.2 | 中文分词 |
| 语义模型 | text2vec-base-chinese | - | 文本向量化 |
| 构建工具 | Maven | 4.0.0 | 项目管理 |

## 架构决策记录 (Architecture Decision Records)

<!-- 记录重要的架构决策及其原因 -->

| 日期 | 决策内容 | 原因/背景 |
|-----|---------|----------|
| 2024-04 | 采用双索引架构（倒排+向量） | 结合关键词精确匹配与语义理解，提升搜索质量 |
| 2024-04 | Python独立服务处理语义搜索 | 模型加载需要PyTorch环境，独立部署便于资源管理 |
