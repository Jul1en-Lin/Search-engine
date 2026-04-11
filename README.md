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

| 类别 | 技术 |
|-----|------|
| 编程语言 | Java 17、Python 3.x、JavaScript |
| 后端框架 | Spring Boot 3.3.0 |
| 前端 | Node.js |
| 数据库 | MySQL |
| 倒排索引 | 自定义实现（Jieba 分词） |
| 语义向量 | text2vec-base-chinese |
| 向量检索 | FAISS |
| 构建工具 | Maven 4.0.0 |

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
