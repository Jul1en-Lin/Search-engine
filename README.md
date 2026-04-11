# 基于双索引架构的中文语义检索引擎

## 介绍

基于倒排索引和语义向量双索引架构的中文语义检索引擎，支持从 Gitee 同步仓库数据并提供全文搜索功能。系统结合传统倒排索引搜索与基于 text2vec-base-chinese 的语义向量搜索，实现更精准的检索效果。

## 系统架构

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│   Frontend      │     │  Java Backend   │     │ Python Service  │
│  (Node.js)      │────▶│  (Spring Boot)  │────▶│  (FAISS/PyTorch)│
│  Port: 3000     │     │   Port: 80      │     │  Port: 8080     │
└─────────────────┘     └─────────────────┘     └─────────────────┘
                                │
                        ┌───────┴───────┐
                        │    MySQL      │
                        │ search_engine │
                        └───────────────┘
```

### 搜索流程

1. 前端发送搜索请求到 Java 后端 `/search?query=xxx`
2. Java 后端使用 Jieba 分词进行倒排索引搜索
3. Java 后端调用 Python 服务进行语义向量搜索
4. 结果取交集后返回给前端

## 技术栈

### 后端
<img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" />
<img src="https://img.shields.io/badge/Spring%20Boot-3.3.0-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" />
<img src="https://img.shields.io/badge/Maven-4.0.0-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" />
<img src="https://img.shields.io/badge/Jieba-分词-42B883?style=for-the-badge&logo=java&logoColor=white" />

### 前端
<img src="https://img.shields.io/badge/Node.js-18+-339933?style=for-the-badge&logo=nodedotjs&logoColor=white" />
<img src="https://img.shields.io/badge/JavaScript-ES6+-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black" />

### AI / 语义检索
<img src="https://img.shields.io/badge/Python-3.8+-3776AB?style=for-the-badge&logo=python&logoColor=white" />
<img src="https://img.shields.io/badge/PyTorch-2.0+-EE4C2C?style=for-the-badge&logo=pytorch&logoColor=white" />
<img src="https://img.shields.io/badge/FAISS-向量检索-0096D6?style=for-the-badge&logo=meta&logoColor=white" />
<img src="https://img.shields.io/badge/text2vec-base--chinese-FF6B6B?style=for-the-badge&logo=huggingface&logoColor=white" />

### 数据存储
<img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />

## 快速开始

### 环境要求

- JDK 17+
- Python 3.8+
- Node.js 16+
- MySQL 8.0+
- Maven 4.0+

### 前置准备

1. 创建 MySQL 数据库 `search_engine`，并创建 `repository` 表
2. 准备倒排索引文件 `./inverted_index.dat`
3. 准备向量索引文件到 `D:/vector_index/`（FAISS 索引 + id_mapping.json）
4. 下载 text2vec-base-chinese 模型到 `D:/ModelScope/text2vec-base-chinese`

### 启动后端（Java）

```bash
# 构建
mvn clean package -DskipTests

# 运行（需要 MySQL 正在运行且索引文件存在）
mvn spring-boot:run
```

### 启动前端（Node.js）

```bash
cd front
npm install
npm start    # 运行在 http://localhost:3000
```

### 启动语义搜索服务（Python）

```bash
cd python
pip install -r requirements.txt
python search_server.py    # 运行在 http://localhost:8080
```

## 项目结构

```
├── src/main/java/com/searchengine/    # Java 后端代码
│   ├── controller/                    # REST API 控制器
│   ├── service/                       # 业务逻辑
│   ├── index/                         # 索引相关
│   └── model/                         # 数据模型
├── front/                             # 前端代码
├── python/                            # Python 语义搜索服务
│   ├── search_server.py               # 语义搜索 HTTP 服务
│   └── build_vector_index.py          # 向量索引构建
└── docs/                              # 项目文档
```

## 核心组件

### Java 后端

- `controller/SearchController.java` - 搜索 API 端点 `/search`
- `service/IndexService.java` - 协调倒排索引和语义搜索
- `index/InvertedIndex.java` - 倒排索引核心实现（Jieba 分词）
- `index/IndexPersistence.java` - 索引序列化/反序列化
- `service/GiteeApiService.java` - 从 Gitee API 同步仓库数据

### Python 服务

- `search_server.py` - 提供语义向量搜索的 HTTP 服务
- `build_vector_index.py` - 从 MySQL 数据构建 FAISS 索引

## 数据模型

```java
Repository {
    Long id;              // 主键
    String fullName;      // 仓库全名，如 "owner/repo-name"
    String htmlUrl;       // Gitee 链接
    String readme;        // README 内容（被索引）
    Long repositoryId;    // Gitee 仓库 ID
}
```

## 配置说明

主要配置文件 `src/main/resources/application.yml`：

- MySQL 连接配置（localhost:3306/search_engine）
- Gitee API Token 和设置
- 索引文件路径：`./data/index`

## 开发文档

- [架构文档](docs/architecture.md) - 系统架构详细说明
- [变更日志](docs/changelog.md) - 项目变更记录
- [项目状态](docs/project_status.md) - 开发进度追踪

## 许可证

MIT License
