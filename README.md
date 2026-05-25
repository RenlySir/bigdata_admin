# Big Data Admin Platform

基于 TiDB 的大数据管理平台，支持多模数据存储能力。

## 技术栈

### 后端
- Java 17
- Spring Boot 3.2.0
- MyBatis Plus 3.5.5
- Druid 连接池
- MySQL JDBC (TiDB兼容)
- OpenAPI/Swagger

### 前端
- Vue 3
- Vite 5
- Element Plus
- Pinia
- Vue Router
- ECharts

### 数据库
- TiDB (兼容MySQL协议)

## 功能特性

1. **多模数据存储**
   - 结构化数据 (JSON)
   - 半结构化数据
   - 文本数据
   - 二进制数据

2. **数据源管理**
   - 支持MySQL、PostgreSQL、MongoDB等多种数据源
   - 数据源连接测试

3. **数据集合管理**
   - 创建/编辑/删除数据集合
   - 数据统计
   - 标签分类

4. **数据记录管理**
   - CRUD操作
   - 批量导入
   - 全文搜索
   - 版本控制
   - 数据完整性校验

5. **仪表盘**
   - 数据统计
   - 可视化图表
   - 活动记录

## 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- TiDB 4.0+

### 后端启动

```bash
cd backend

# 配置数据库连接
# 编辑 src/main/resources/application.yml

# 启动应用
mvn spring-boot:run
```

API访问: http://localhost:8080/api
Swagger文档: http://localhost:8080/api/swagger-ui.html

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端访问: http://localhost:5173

### 数据库初始化

```bash
# 连接到TiDB
mysql -h 127.0.0.1 -P 4000 -u root

# 执行初始化脚本
source backend/src/main/resources/schema.sql
```

## 项目结构

```
bigdata_admin/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/        # Java源码
│   │   │   └── resources/   # 配置文件
│   │   └── test/            # 测试代码
│   └── pom.xml              # Maven配置
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/             # API封装
│   │   ├── components/      # 组件
│   │   ├── router/          # 路由
│   │   ├── views/           # 页面
│   │   └── main.js          # 入口
│   └── package.json         # NPM配置
└── README.md
```

## 配置说明

### TiDB连接配置

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:4000/bigdata_admin
    username: root
    password: ""
```

### CORS配置

```yaml
app:
  cors:
    allowed-origins: http://localhost:5173,http://localhost:8080
```

## API文档

启动后端服务后，访问 http://localhost:8080/api/swagger-ui.html 查看完整API文档。

### 主要API端点

- `GET /api/health` - 健康检查
- `GET /api/datasources` - 获取数据源列表
- `POST /api/datasources` - 创建数据源
- `GET /api/collections` - 获取数据集合列表
- `POST /api/collections` - 创建数据集合
- `GET /api/collections/{id}/records` - 获取数据记录
- `POST /api/collections/{id}/records` - 创建数据记录

## 开发计划

- [ ] 用户认证和权限管理
- [ ] 数据导入/导出增强
- [ ] 更多数据源类型支持
- [ ] 数据转换和ETL功能
- [ ] 数据质量检查
- [ ] 监控和告警

## License

MIT License
