# Big Data Admin Platform - 部署指南

本文档提供 Big Data Admin Platform 的详细部署指南。

## 目录

- [环境准备](#环境准备)
- [本地开发部署](#本地开发部署)
- [生产环境部署](#生产环境部署)
- [Docker 部署](#docker-部署)
- [Kubernetes 部署](#kubernetes-部署)
- [监控和运维](#监控和运维)
- [故障排查](#故障排查)

## 环境准备

### 硬件要求

**最低配置：**
- CPU: 2 核
- 内存: 4GB
- 磁盘: 20GB

**推荐配置：**
- CPU: 4 核+
- 内存: 8GB+
- 磁盘: 100GB+

### 软件要求

| 软件 | 版本 | 用途 |
|------|------|------|
| JDK | 17+ | 后端运行环境 |
| Node.js | 18+ | 前端构建 |
| TiDB | 4.0+ | 主数据库 |
| Redis | 6.0+ | 缓存和会话存储 |
| Nginx | 1.18+ | 反向代理（可选） |

## 本地开发部署

### 1. 克隆项目

```bash
git clone https://github.com/RenlySir/bigdata_admin.git
cd bigdata_admin
```

### 2. 配置 TiDB

```bash
# 使用 Docker 启动 TiDB
docker run -d \
  --name tidb \
  -p 4000:4000 \
  -p 10080:10080 \
  pingcap/tidb:latest

# 等待 TiDB 启动
docker logs -f tidb
```

### 3. 配置后端

```bash
cd backend

# 创建配置文件
cp src/main/resources/application-dev.yml.template \
   src/main/resources/application-dev.yml

# 编辑配置文件
vim src/main/resources/application-dev.yml
```

配置示例：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:4000/bigdata_admin
    username: root
    password: ""
  redis:
    host: localhost
    port: 6379

tidb:
  host: localhost
  port: 4000
  database: bigdata_admin
  username: root
  password: ""

jwt:
  secret: your-secret-key-here
  expiration: 86400000
```

### 4. 初始化数据库

```bash
# 连接到 TiDB
mysql -h 127.0.0.1 -P 4000 -u root

# 创建数据库
CREATE DATABASE IF NOT EXISTS bigdata_admin;

# 使用数据库
USE bigdata_admin;

# 执行初始化脚本
source backend/src/main/resources/schema.sql
```

### 5. 启动后端

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 6. 启动前端

```bash
cd frontend
npm install
npm run dev
```

## 生产环境部署

### 1. 构建后端

```bash
cd backend
mvn clean package -DskipTests
```

### 2. 构建前端

```bash
cd frontend
npm run build
```

### 3. 配置 Nginx

```nginx
# /etc/nginx/conf.d/bigdata-admin.conf

upstream backend {
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name your-domain.com;

    # 前端静态文件
    location / {
        root /var/www/bigdata-admin/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket 支持
    location /ws/ {
        proxy_pass http://backend;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 4. 配置 Systemd 服务

```ini
# /etc/systemd/system/bigdata-admin.service

[Unit]
Description=Big Data Admin Application
After=network.target tidb.service redis.service

[Service]
Type=simple
User=bigdata
WorkingDirectory=/opt/bigdata-admin/backend
ExecStart=/usr/bin/java -jar /opt/bigdata-admin/backend/target/admin-1.0.0.jar
Restart=on-failure
RestartSec=10

Environment="TIDB_HOST=localhost"
Environment="TIDB_PORT=4000"
Environment="TIDB_USERNAME=root"
Environment="TIDB_PASSWORD=your-password"
Environment="JWT_SECRET=your-production-secret"
Environment="REDIS_HOST=localhost"
Environment="REDIS_PORT=6379"

[Install]
WantedBy=multi-user.target
```

启动服务：

```bash
sudo systemctl daemon-reload
sudo systemctl enable bigdata-admin
sudo systemctl start bigdata-admin
sudo systemctl status bigdata-admin
```

## Docker 部署

### 1. 构建镜像

```bash
# 构建后端镜像
cd backend
docker build -t bigdata-admin-backend:latest .

# 构建前端镜像
cd ../frontend
docker build -t bigdata-admin-frontend:latest .
```

### 2. Docker Compose 部署

```yaml
# docker-compose.yml

version: '3.8'

services:
  tidb:
    image: pingcap/tidb:latest
    ports:
      - "4000:4000"
      - "10080:10080"
    volumes:
      - tidb-data:/var/lib/tidb
    environment:
      TIDB_SM_CONNECTION_TIMEOUT: 30

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    command: redis-server --appendonly yes

  backend:
    image: bigdata-admin-backend:latest
    ports:
      - "8081:8081"
    depends_on:
      - tidb
      - redis
    environment:
      TIDB_HOST: tidb
      TIDB_PORT: 4000
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: your-production-secret
    restart: on-failure

  frontend:
    image: bigdata-admin-frontend:latest
    ports:
      - "80:80"
    depends_on:
      - backend
    restart: on-failure

volumes:
  tidb-data:
  redis-data:
```

启动：

```bash
docker-compose up -d
```

## Kubernetes 部署

### 1. ConfigMap

```yaml
# configmap.yaml

apiVersion: v1
kind: ConfigMap
metadata:
  name: bigdata-admin-config
data:
  TIDB_HOST: "tidb-service"
  TIDB_PORT: "4000"
  REDIS_HOST: "redis-service"
  REDIS_PORT: "6379"
```

### 2. Secret

```yaml
# secret.yaml

apiVersion: v1
kind: Secret
metadata:
  name: bigdata-admin-secret
type: Opaque
data:
  TIDB_PASSWORD: eW91ci1wYXNzd29yZA==
  JWT_SECRET: eW91ci1wcm9kdWN0aW9uLXNlY3JldA==
```

### 3. Deployment

```yaml
# deployment.yaml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: bigdata-admin-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bigdata-admin-backend
  template:
    metadata:
      labels:
        app: bigdata-admin-backend
    spec:
      containers:
      - name: backend
        image: bigdata-admin-backend:latest
        ports:
        - containerPort: 8081
        envFrom:
        - configMapRef:
            name: bigdata-admin-config
        - secretRef:
            name: bigdata-admin-secret
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
        livenessProbe:
          httpGet:
            path: /api/health
            port: 8081
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /api/health
            port: 8081
          initialDelaySeconds: 30
          periodSeconds: 5
```

### 4. Service

```yaml
# service.yaml

apiVersion: v1
kind: Service
metadata:
  name: bigdata-admin-backend
spec:
  selector:
    app: bigdata-admin-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8081
  type: LoadBalancer
```

部署到 Kubernetes：

```bash
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f deployment.yaml
kubectl apply -f service.yaml
```

## 监控和运维

### 健康检查

```bash
# 应用健康检查
curl http://localhost:8081/api/health

# 数据库连接检查
curl http://localhost:8081/api/datasources/tidb/test
```

### 日志查看

```bash
# Systemd 服务日志
sudo journalctl -u bigdata-admin -f

# Docker 容器日志
docker logs -f bigdata-admin-backend

# Kubernetes Pod 日志
kubectl logs -f deployment/bigdata-admin-backend
```

### 性能监控

应用内置监控接口：

```bash
# 获取当前系统指标
curl http://localhost:8081/api/monitoring/metrics/current

# 获取活跃告警
curl http://localhost:8081/api/monitoring/alerts/active
```

### 备份策略

**数据库备份：**

```bash
# TiDB 数据备份
mydumper -h 127.0.0.1 -P 4000 -u root -o /backup/tidb/

# 定时备份（cron）
0 2 * * * mydumper -h 127.0.0.1 -P 4000 -u root -o /backup/tidb/$(date +\%Y\%m\%d)
```

**配置备份：**

```bash
# 备份配置文件
tar -czf config-backup-$(date +%Y%m%d).tar.gz \
    backend/src/main/resources/application*.yml \
    docker-compose.yml \
    k8s/
```

## 故障排查

### 常见问题

**1. 无法连接到 TiDB**

```bash
# 检查 TiDB 状态
curl http://localhost:10080/status

# 检查网络连接
telnet localhost 4000

# 查看 TiDB 日志
docker logs tidb
```

**2. Redis 连接失败**

```bash
# 检查 Redis 状态
redis-cli ping

# 查看 Redis 日志
docker logs redis
```

**3. 应用启动失败**

```bash
# 查看应用日志
tail -f /var/log/bigdata-admin/application.log

# 检查端口占用
netstat -tuln | grep 8081

# 检查 Java 版本
java -version
```

**4. 内存不足**

```bash
# 调整 JVM 内存
java -Xms512m -Xmx1024m -jar admin-1.0.0.jar

# 查看 JVM 堆内存使用
jmap -heap <pid>
```

### 日志配置

```yaml
# logback-spring.xml

<configuration>
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>/var/log/bigdata-admin/application.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>/var/log/bigdata-admin/application.%d{yyyy-MM-dd}.log</fileNamePattern>
            <maxHistory>30</maxHistory>
        </rollingPolicy>
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <root level="INFO">
        <appender-ref ref="FILE" />
        <appender-ref ref="CONSOLE" />
    </root>
</configuration>
```

## 升级指南

### 滚动升级

```bash
# 1. 备份数据
mydumper -h localhost -P 4000 -u root -o /backup/pre-upgrade/

# 2. 拉取新版本
git pull origin main

# 3. 构建新版本
mvn clean package

# 4. 停止服务
sudo systemctl stop bigdata-admin

# 5. 替换 JAR 文件
cp target/admin-1.0.0.jar /opt/bigdata-admin/backend/

# 6. 启动服务
sudo systemctl start bigdata-admin

# 7. 验证升级
curl http://localhost:8081/api/health
```

## 安全加固

### 1. SSL/TLS 配置

```yaml
# application-prod.yml

server:
  ssl:
    enabled: true
    key-store: /path/to/keystore.p12
    key-store-password: your-password
    key-store-type: PKCS12
```

### 2. 防火墙配置

```bash
# 只允许必要端口
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp
sudo ufw enable
```

### 3. 数据库安全

```sql
-- 创建专用数据库用户
CREATE USER 'bigdata_admin'@'%' IDENTIFIED BY 'strong-password';
GRANT SELECT, INSERT, UPDATE, DELETE ON bigdata_admin.* TO 'bigdata_admin'@'%';
FLUSH PRIVILEGES;
```

## 支持和反馈

- 文档: [GitHub Wiki](https://github.com/RenlySir/bigdata_admin/wiki)
- 问题反馈: [GitHub Issues](https://github.com/RenlySir/bigdata_admin/issues)
- 技术讨论: [Discussions](https://github.com/RenlySir/bigdata_admin/discussions)
