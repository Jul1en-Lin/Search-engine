# 代码仓库搜索引擎前端

基于现代设计风格的代码仓库搜索引擎前端界面。

## 功能特性

- 🔍 实时搜索代码仓库
- 📄 分页展示搜索结果（每页10个）
- 🎨 关键词高亮显示
- 🔤 结巴分词支持
- 📊 显示搜索结果总数
- 🎯 响应式设计

## 快速开始

### 安装依赖

无需安装依赖，使用Node.js内置模块。

### 启动服务器

```bash
npm start
```

或者直接运行：

```bash
node server.js
```

服务器将在 `http://localhost:3000` 启动。

## 使用说明

1. 在搜索框中输入关键词（支持中英文）
2. 点击搜索按钮或按回车键
3. 查看搜索结果，使用分页浏览更多结果
4. 点击仓库名称或链接访问仓库

## 技术栈

- HTML5
- Tailwind CSS (CDN)
- Lucide Icons
- Jieba.js (中文分词)
- Node.js (简单HTTP服务器)

## 后端API

前端通过以下API与后端通信：

```
GET http://localhost/search?query=关键词
```

确保后端服务运行在 `http://localhost` 端口。

