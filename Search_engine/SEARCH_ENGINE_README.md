# 代码仓库搜索引擎

基于倒排索引和中文分词的代码仓库搜索引擎，支持从Gitee同步仓库数据并提供全文搜索功能。

## 功能特性

### 🔍 核心功能
- **仓库同步**: 从Gitee自动同步用户/组织的仓库数据
- **中文分词**: 使用结巴分词进行搜索引擎模式分词
- **倒排索引**: 高效的倒排索引实现，支持多路归并搜索
- **全文搜索**: 基于README内容的全文搜索，按词频排序
- **REST API**: 完整的REST API接口

### 🏗️ 技术架构
- **分词引擎**: Jieba分词（搜索引擎模式）
- **索引结构**: 倒排索引 + 词频统计
- **搜索算法**: 多路归并排序
- **持久化**: 二进制文件持久化，优化内存加载
- **数据库**: MySQL + MyBatis Plus

## 核心组件

### 1. 分词工具 (`JiebaTokenizer`)
- 清理特殊字符
- 搜索引擎模式分词
- 词频统计

### 2. 倒排索引 (`InvertedIndex`)
- 词到文档列表映射
- 词频排序
- 多路归并搜索算法

### 3. 索引持久化 (`IndexPersistence`)
- 二进制文件存储
- 内存优化加载
- 增量更新支持

### 4. 搜索服务 (`SearchService`)
- 全文搜索接口
- 结果排序和过滤
- 索引重建功能

## API接口

### 搜索仓库
```http
GET /api/search/repositories?query={关键词}&topN={数量}
```

### 同步仓库数据
```http
POST /api/search/sync/{username}
```

### 索引管理
```http
POST /api/search/index/rebuild      # 重建索引
POST /api/search/index/persist      # 持久化索引
GET  /api/search/index/status       # 索引状态
GET  /api/search/index/statistics   # 索引统计
```

## 使用流程

### 1. 同步数据
```bash
# 同步用户仓库
curl -X POST "http://localhost:8080/api/search/sync/octocat"

# 同步组织仓库
curl -X POST "http://localhost:8080/api/search/sync/apache"
```

### 2. 执行搜索
```bash
# 搜索Spring相关项目
curl "http://localhost:8080/api/search/repositories?query=Spring&topN=10"

# 搜索数据库相关项目
curl "http://localhost:8080/api/search/repositories?query=数据库&topN=5"
```

### 3. 索引管理
```bash
# 查看索引状态
curl "http://localhost:8080/api/search/index/status"

# 持久化索引到磁盘
curl -X POST "http://localhost:8080/api/search/index/persist"
```

## 搜索特性

### 多关键词搜索
- 支持同时搜索多个关键词
- 使用多路归并算法优化性能
- 按匹配度排序结果

### 词频排序
- 文档中出现关键词频率越高，排名越前
- 支持TF-IDF相关性排序

### 中文分词
- 自动识别中文词汇
- 搜索引擎模式优化分词效果
- 过滤停用词和单字词

## 性能优化

### 索引加载优化
- 应用启动时自动加载索引到内存
- 二进制文件格式，读取速度快
- 支持增量更新

### 内存管理
- 使用ConcurrentHashMap保证线程安全
- 索引分片存储，减少内存占用
- 定期持久化防止数据丢失

### 搜索优化
- 多路归并算法，适合多关键词搜索
- 优先队列优化排序性能
- 结果数量限制，控制内存使用

## 配置说明

### 应用配置 (`application.yml`)
```yaml
# 数据库配置
datasource:
  url: jdbc:mysql://localhost:3306/search_engine
  username: root
  password: your_password

# 索引配置
searchengine:
  index:
    path: ./data/index

# MyBatis Plus配置
mybatis-plus:
  configuration:
    map-underscore-to-camel-case: false
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl
```

### Gitee API配置
```yaml
gitee:
  api-base-url: https://gitee.com/api/v5
  access-token: your_token  # 可选，用于提高API限制
  per-page: 100
  timeout: 10000
```

## 数据库表结构

```sql
CREATE TABLE repository (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    fullName VARCHAR(255) NOT NULL,
    htmlUrl VARCHAR(255) NOT NULL,
    readme LONGTEXT,
    repositoryId BIGINT,
    INDEX idx_fullName (fullName),
    INDEX idx_repositoryId (repositoryId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## 部署运行

### 环境要求
- JDK 17+
- MySQL 8.0+
- Maven 3.6+

### 启动步骤
```bash
# 1. 克隆项目
git clone <repository-url>
cd searchengine

# 2. 配置数据库
# 修改application.yml中的数据库连接信息

# 3. 编译运行
mvn clean install
mvn spring-boot:run

# 或者直接运行
java -jar target/searchengine-1.0.0.jar
```

### Docker部署（可选）
```dockerfile
FROM openjdk:17-jdk-alpine
COPY target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app.jar"]
```

## 测试

### 单元测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=RepositorySyncSearchIntegrationTest
```

### 手动测试
```bash
# 1. 同步测试数据
curl -X POST "http://localhost:8080/api/search/sync/Cary_C"

# 2. 执行搜索测试
curl "http://localhost:8080/api/search/repositories?query=Java&topN=5"

# 3. 查看索引状态
curl "http://localhost:8080/api/search/index/status"
```

## 监控和维护

### 索引维护
- 定期执行索引持久化：`POST /api/search/index/persist`
- 监控索引文件大小和内存使用
- 根据需要重建索引：`POST /api/search/index/rebuild`

### 性能监控
- 搜索响应时间
- 索引构建耗时
- 内存使用情况
- 磁盘I/O性能

## 扩展开发

### 添加新的分词器
1. 实现 `Tokenizer` 接口
2. 在 `application.yml` 中配置
3. 更新 `SearchService` 依赖注入

### 支持其他数据源
1. 修改 `RepositorySyncService`
2. 添加新的数据源适配器
3. 更新索引构建逻辑

### 优化搜索算法
1. 实现TF-IDF算法
2. 添加相关性排序
3. 支持模糊搜索和通配符

## 许可证

本项目采用 MIT 许可证。
