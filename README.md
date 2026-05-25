# Big Data Admin Platform

基于 TiDB 的企业级大数据管理平台，支持多模数据存储、ETL 数据转换和系统监控告警。

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

### 3. ETL 数据转换
- **字段映射** (Mapping) - 灵活的字段转换
- **数据过滤** (Filter) - 条件筛选
- **数据聚合** (Aggregate) - 求和、平均、计数等
- **数据导出** (Export) - 批量导出功能

### 4. 系统监控告警
- **实时指标采集** - CPU、内存、磁盘、线程
- **告警规则引擎** - 灵活的阈值配置
- **告警通知** - 支持多种通知渠道
- **告警工作流** - 触发、确认、解决

### 5. 数据管理能力
- 数据源管理 (多数据源接入)
- 数据导入/导出 (批量处理)
- 数据转换 (ETL 支持)
- 数据治理 (质量检查、血缘追踪)

## 🏗️ 技术架构

### 后端技术栈
```
Spring Boot 3.2.0
├── Spring Security (JWT认证)
├── MyBatis Plus 3.5.5
├── Druid 连接池
├── Redis (缓存 & 限流)
├── TiDB JDBC Driver
└── OpenAPI/Swagger (API文档)
```

### 前端技术栈
```
Vue 3 + Vite 5
├── Element Plus (UI组件)
├── Pinia (状态管理)
├── Vue Router (路由)
├── Axios (HTTP客户端)
└── ECharts (数据可视化)
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
# 编辑 src/main/resources/application.yml

# 启动应用
mvn clean install
mvn spring-boot:run
```

API 访问: http://localhost:8081/api
Swagger 文档: http://localhost:8081/swagger-ui.html

### 前端启动

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端访问: http://localhost:5173

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

EtlTransformation (ETL转换)
  ├── id, name, description
  ├── sourceCollectionId, targetCollectionId
  ├── transformationType (mapping/filter/aggregate/export)
  ├── transformationRules (JSON)
  └── status

EtlExecution (ETL执行记录)
  ├── id, transformationId
  ├── status (running/completed/failed)
  ├── recordsProcessed, recordsSuccess, recordsFailed
  └── startedAt, completedAt, durationMs

SystemMetric (系统指标)
  ├── id, metricName (cpu_usage/memory_usage/etc)
  ├── metricValue, metricUnit
  ├── metricSource (system/application)
  └── recordedAt

AlertRule (告警规则)
  ├── id, name, description
  ├── metricName, condition (gt/lt/eq), threshold
  ├── severity (info/warning/critical)
  ├── status (active/disabled)
  └── cooldownMinutes

AlertHistory (告警历史)
  ├── id, ruleId, ruleName
  ├── severity, metricName, metricValue, threshold
  ├── status (triggered/resolved/acknowledged)
  └── triggeredAt, resolvedAt, acknowledgedAt
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

### ETL 转换管理
- `GET /api/etl/transformations` - 获取转换列表
- `POST /api/etl/transformations` - 创建转换
- `PUT /api/etl/transformations/{id}` - 更新转换
- `DELETE /api/etl/transformations/{id}` - 删除转换
- `POST /api/etl/transformations/{id}/execute` - 执行转换
- `GET /api/etl/executions` - 获取执行历史

### 系统监控接口
- `GET /api/monitoring/metrics/current` - 获取当前指标
- `GET /api/monitoring/metrics/history` - 获取指标历史
- `GET /api/monitoring/alerts/rules` - 获取告警规则
- `POST /api/monitoring/alerts/rules` - 创建告警规则
- `PUT /api/monitoring/alerts/rules/{id}` - 更新告警规则
- `DELETE /api/monitoring/alerts/rules/{id}` - 删除告警规则
- `GET /api/monitoring/alerts/history` - 获取告警历史
- `GET /api/monitoring/alerts/active` - 获取活跃告警
- `POST /api/monitoring/alerts/{id}/acknowledge` - 确认告警
- `POST /api/monitoring/alerts/{id}/resolve` - 解决告警

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

### 项目结构

```
bigdata_admin/
├── backend/                          # 后端项目
│   ├── src/main/java/
│   │   └── com/bigdata/admin/
│   │       ├── entity/              # 实体类
│   │       ├── mapper/              # MyBatis Mapper
│   │       ├── service/             # 业务逻辑层
│   │       ├── controller/          # 控制器
│   │       ├── config/              # 配置类
│   │       ├── dto/                 # 数据传输对象
│   │       └── common/              # 公共类
│   ├── src/test/java/               # 单元测试
│   └── pom.xml                      # Maven配置
├── frontend/                         # 前端项目
│   ├── src/
│   │   ├── types/                   # TypeScript类型定义
│   │   ├── services/                # API服务封装
│   │   ├── composables/             # 组合式函数
│   │   ├── views/                   # 页面组件
│   │   └── main.ts                  # 入口文件
│   └── package.json                 # NPM配置
└── README.md
```

### 添加新功能

**后端开发流程：**
1. 创建 Entity 实体类
2. 创建 Mapper 接口
3. 创建 Service 服务类
4. 创建 Controller 控制器
5. 编写单元测试
6. 更新 API 文档

**前端开发流程：**
1. 在 types/ 添加 TypeScript 类型定义
2. 在 services/ 添加 API 服务封装
3. 在 composables/ 添加组合式函数
4. 在 views/ 添加页面组件
5. 更新路由配置

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
export REDIS_HOST=your-redis-host

# 构建并运行
cd backend
mvn clean package
java -jar target/admin-1.0.0.jar
```

### Docker 部署

```bash
# 构建镜像
docker build -t bigdata-admin:latest .

# 运行容器
docker run -d \
  -p 8081:8081 \
  -e TIDB_HOST=tidb \
  -e TIDB_PASSWORD=password \
  -e REDIS_HOST=redis \
  bigdata-admin:latest
```

### Docker Compose 部署

```bash
# 一键启动所有服务
docker-compose up -d
```

## 🧪 测试

```bash
# 后端单元测试
cd backend
mvn test

# 前端单元测试
cd frontend
npm test
```

## 📝 更新日志

### v1.0.0 (2024)
- ✅ 基础数据管理功能
- ✅ TiDB 数据源支持
- ✅ ETL 数据转换功能
- ✅ 系统监控和告警
- ✅ 用户认证和权限管理
- ✅ API 限流和安全防护

## 🤝 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'feat: Add AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 开启 Pull Request

## 📄 许可证

MIT License

## 📞 联系方式

- 项目地址: [GitHub](https://github.com/RenlySir/bigdata_admin)
- 问题反馈: [Issues](https://github.com/RenlySir/bigdata_admin/issues)
- 技术讨论: [Discussions](https://github.com/RenlySir/bigdata_admin/discussions)

---

Made with ❤️ using Spring Boot 3, Vue 3, and TiDB
