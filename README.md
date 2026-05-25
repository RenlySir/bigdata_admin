# Big Data Admin Platform

基于 TiDB 的企业级大数据管理平台，支持多模数据存储和管理能力。

## 🎯 核心特性

### 1. 多模数据存储
- **结构化数据** - 传统关系型数据
- **半结构化数据** - JSON、XML 等灵活格式
- **非结构化数据** - 文本、二进制文件

### 2. 基于 TiDB
- 分布式事务支持 (ACID)
- HTAP 混合事务/分析处理
- 水平扩展能力
- MySQL 协议兼容

### 3. 数据管理能力
- 数据源管理 (多数据源接入)
- 数据导入/导出 (批量处理)
- 数据转换 (ETL 支持)
- 数据治理 (质量检查、血缘追踪)

## 🏗️ 技术架构

### 后端架构
```
┌─────────────────────────────────────────┐
│           Spring Boot 3.2                │
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │ Security │  │  Cache   │  │  API   ││
│  │   JWT    │  │  Redis   │  │ Layer  ││
│  └──────────┘  └──────────┘  └────────┘│
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │ Service  │  │  MyBatis │  │  TiDB  ││
│  │  Layer   │  │   Plus   │  │ Driver ││
│  └──────────┘  └──────────┘  └────────┘│
└─────────────────────────────────────────┘
```

### 前端架构
```
┌─────────────────────────────────────────┐
│           Vue 3 + Vite                   │
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │  Router  │  │  Pinia   │  │ Axios  ││
│  └──────────┘  └──────────┘  └────────┘│
├─────────────────────────────────────────┤
│  ┌──────────┐  ┌──────────┐  ┌────────┐│
│  │Element+  │  │ECharts   │  │ Utils  ││
│  └──────────┘  └──────────┘  └────────┘│
└─────────────────────────────────────────┘
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- TiDB 4.0+ (或 MySQL 8.0+)
- Redis 6.0+ (可选，用于缓存)

### 后端启动

```bash
cd backend

# 配置数据库连接
cp src/main/resources/application-dev.yml.template src/main/resources/application-dev.yml
# 编辑 application-dev.yml 配置 TiDB 连接信息

# 启动应用
mvn spring-boot:run
```

API 访问: http://localhost:8081/api
Swagger 文档: http://localhost:8081/api/swagger-ui.html

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端访问: http://localhost:5173

### Docker 部署

```bash
# 使用 Docker Compose 一键启动
docker-compose up -d
```

访问: http://localhost:8080

## 📊 数据模型

### 核心实体

```
DataSource (数据源)
  ├── id, name, type
  ├── connectionConfig (JSON)
  └── status

DataCollection (数据集合)
  ├── id, name, description
  ├── dataSourceId
  ├── schemaDefinition (JSON)
  └── recordCount, sizeInBytes

DataRecord (数据记录)
  ├── id, collectionId
  ├── dataType (json/text/binary)
  ├── jsonData, textContent
  └── version, checksum
```

## 🔌 API 接口

### 数据源管理
- `GET /api/datasources` - 获取数据源列表
- `POST /api/datasources` - 创建数据源
- `POST /api/datasources/{id}/test` - 测试连接

### TiDB 专用接口
- `POST /api/datasources/tidb/test` - 测试 TiDB 连接
- `GET /api/datasources/{id}/tidb/databases` - 获取数据库列表
- `GET /api/datasources/{id}/tidb/tables` - 获取表列表
- `POST /api/datasources/{id}/tidb/query` - 执行 SQL 查询

### 数据集合管理
- `GET /api/collections` - 获取集合列表
- `POST /api/collections` - 创建集合
- `GET /api/collections/{id}` - 获取集合详情
- `PUT /api/collections/{id}` - 更新集合
- `DELETE /api/collections/{id}` - 删除集合

### 数据记录管理
- `GET /api/collections/{id}/records` - 获取记录列表
- `POST /api/collections/{id}/records` - 创建记录
- `POST /api/collections/{id}/records/batch` - 批量导入
- `PUT /api/collections/{id}/records/{recordId}` - 更新记录
- `DELETE /api/collections/{id}/records/{recordId}` - 删除记录

## 🔐 安全特性

- JWT Token 认证
- 基于 RBAC 的权限控制
- API 请求限流
- CORS 跨域配置
- SQL 注入防护

## 📈 性能优化

- MyBatis Plus 二级缓存
- Redis 分布式缓存
- 数据库连接池 (Druid)
- 分页查询优化
- 索引策略优化

## 🛠️ 开发指南

### 添加新功能

1. **后端**
   ```bash
   # 创建 Entity
   # 创建 Mapper
   # 创建 Service
   # 创建 Controller
   # 编写单元测试
   ```

2. **前端**
   ```bash
   # 在 types/ 添加类型定义
   # 在 services/ 添加 API 服务
   # 在 composables/ 添加组合式函数
   # 在 views/ 添加页面组件
   ```

### 代码规范

- 后端遵循阿里巴巴 Java 规范
- 前端遵循 Vue 3 风格指南
- 提交前运行 `mvn test` 和 `npm test`

## 📦 部署说明

### 生产环境配置

```bash
# 设置环境变量
export TIDB_HOST=your-tidb-host
export TIDB_PORT=4000
export TIDB_USERNAME=root
export TIDB_PASSWORD=your-password
export JWT_SECRET=your-secret-key

# 构建并运行
mvn clean package
java -jar backend/target/admin-1.0.0.jar
```

### Docker 部署

```bash
# 构建镜像
docker build -t bigdata-admin:latest .

# 运行容器
docker run -d \
  -p 8080:8080 \
  -e TIDB_HOST=tidb \
  -e TIDB_PASSWORD=password \
  bigdata-admin:latest
```

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

MIT License

## 📞 联系方式

- 问题反馈: [GitHub Issues](https://github.com/RenlySir/bigdata_admin/issues)
- 技术讨论: [Discussions](https://github.com/RenlySir/bigdata_admin/discussions)

---

Made with ❤️ using Spring Boot 3, Vue 3, and TiDB

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
