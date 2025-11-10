# 代码仓库搜索引擎 - 后端服务

基于 Spring Boot 3.3.0 的代码仓库搜索引擎后端服务。

## 技术栈

- **框架**: Spring Boot 3.3.0
- **构建工具**: Maven 4.0.0
- **Java版本**: 17
- **数据库**: H2 (开发环境) / MySQL (生产环境)
- **ORM**: Spring Data JPA

## 项目结构

```
searchengine/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/searchengine/
│   │   │       └── SearchEngineApplication.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/
│           └── com/searchengine/
│               └── SearchEngineApplicationTests.java
└── README.md
```

## 快速开始

### 前置要求

- JDK 17 或更高版本
- Maven 3.6+ 

### 运行项目

1. 克隆项目
```bash
git clone <repository-url>
cd searchengine
```

2. 编译项目
```bash
mvn clean compile
```

3. 运行应用
```bash
mvn spring-boot:run
```

或者使用IDE直接运行 `SearchEngineApplication.java`

4. 访问应用
- API地址: http://localhost:8080/api
- H2控制台: http://localhost:8080/api/h2-console

### 配置说明

#### 开发环境 (默认)
使用 H2 内存数据库，无需额外配置。

#### 开发环境 (MySQL)
在 `application.yml` 中设置：
```yaml
spring:
  profiles:
    active: dev
```

#### 生产环境
在 `application.yml` 中设置：
```yaml
spring:
  profiles:
    active: prod
```

并通过环境变量配置数据库连接：
```bash
export DB_URL=jdbc:mysql://localhost:3306/searchengine
export DB_USERNAME=root
export DB_PASSWORD=your_password
```

## 开发指南

### 添加依赖

在 `pom.xml` 的 `<dependencies>` 部分添加所需依赖。

### 创建Controller

在 `src/main/java/com/searchengine/controller/` 目录下创建控制器类。

### 创建Entity

在 `src/main/java/com/searchengine/entity/` 目录下创建实体类。

### 创建Repository

在 `src/main/java/com/searchengine/repository/` 目录下创建数据访问接口。

## API文档

API文档将在后续开发中补充。

## 许可证

MIT License

