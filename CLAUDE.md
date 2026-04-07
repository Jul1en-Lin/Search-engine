# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A code repository search engine based on inverted index and Chinese word segmentation. It supports syncing repository data from Gitee and provides full-text search functionality. The system combines traditional inverted index search with semantic vector search using the text2vec-base-chinese model.

## Architecture

The system consists of three main components:

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

### Search Flow
1. Frontend sends query to Java backend `/search?query=xxx`
2. Java performs inverted index search using Jieba tokenization
3. Java calls Python service for semantic vector search
4. Results are intersected and returned to frontend

## Build and Run Commands

### Backend (Java)
```bash
# Build
mvn clean package -DskipTests

# Run (requires MySQL running and index file)
mvn spring-boot:run

# Run tests
mvn test

# Run single test class
mvn test -Dtest=IndexServiceTest
```

### Frontend (Node.js)
```bash
cd front
npm install
npm start    # Runs on http://localhost:3000
```

### Python Semantic Search Service
```bash
cd python
pip install -r requirements.txt
python search_server.py    # Runs on http://localhost:8080
```

### Prerequisites
- MySQL database `search_engine` with `repository` table
- Inverted index file at `./inverted_index.dat`
- Vector index files at `D:/vector_index/` (FAISS index + id_mapping.json)
- text2vec-base-chinese model at `D:/ModelScope/text2vec-base-chinese`

## Key Components

### Java Backend Structure
- `controller/SearchController.java` - REST API endpoint `/search`
- `service/IndexService.java` - Coordinates inverted index + semantic search
- `index/InvertedIndex.java` - Core inverted index implementation with Jieba tokenization
- `index/IndexPersistence.java` - Serialize/deserialize index to file
- `service/GiteeApiService.java` - Sync repositories from Gitee API

### Python Service
- `search_server.py` - HTTP server providing semantic vector search
- `build_vector_index.py` - Build FAISS index from MySQL data

### Data Model
```java
Repository {
    Long id;              // Primary key
    String fullName;      // e.g., "owner/repo-name"
    String htmlUrl;       // Gitee URL
    String readme;        // README content (indexed)
    Long repositoryId;    // Gitee repository ID
}
```

## Configuration

Key settings in `src/main/resources/application.yml`:
- MySQL connection (localhost:3306/search_engine)
- Gitee API token and settings
- Index file path: `./data/index`

Hardcoded paths (may need adjustment):
- Stopwords: `D:\Project\Search_engine\stopwords.txt` (in SearchEngineApplication.java)
- Vector index: `D:/vector_index` (in search_server.py)
- Model path: `D:/ModelScope/text2vec-base-chinese` (in search_server.py)

## 开发核心准则 (Development Core Guidelines)

1. **文档更新义务**：在项目完成里程碑或主要新增内容后，更新 `docs/project_spec.md` 文件，确保规格说明与实现保持同步。

2. **提交规范**：在 git 进行提交时使用 `/update-docs-and-commit` 命令，自动将变更摘要记录到文档并生成规范的 commit message。

3. **规格溯源原则**：所有的逻辑实现必须溯源至 `docs/project_spec.md`。若代码实现与规格说明不符，以规格说明为准；若需要修改规格，必须先询问用户是否要更新文档，获得确认后再修改代码。

## 进度记录 (Progress Records)

### 2026-04-07
- **类型**: Docs
- **范围**: 项目文档体系 (CLAUDE.md, docs/)
- **变更摘要**:
  - 创建 docs/ 目录，建立项目文档结构
  - 添加 architecture.md 系统架构文档
  - 添加 changelog.md 变更日志
  - 添加 project_status.md 项目状态追踪
- **里程碑**: 建立项目文档管理体系

## Project Rules

### 文档更新规则

1. **架构对齐**：每当引入新的第三方库、修改核心类交互或调整数据库模式时，必须同步更新 `docs/architecture.md`。

2. **变更溯源**：每次 Git 提交前，需在 `docs/changelog.md` 中追加一条简要变更记录。

3. **进度锚定**：在每个任务（Issue）开始前，先读取 `docs/project_status.md` 确认当前里程碑；任务完成后，更新该文件的"已完成工作"与"待办与后续计划"部分。
