# Backend Service - 后端服务（占位）

## 📋 概述

后端服务是系统的管理后台，包含：
- **后端 API**: Spring Boot 应用
- **前端界面**: Vue.js 管理后台
- **一体化部署**: 使用 Nginx 提供前端页面和 API 代理

**端口**: 8086 (API) / 80 (前端)

## 🏗️ 架构

```
┌─────────────────────────────────────┐
│         Docker Container            │
├─────────────────────────────────────┤
│  Nginx (Port 80)                    │
│    ├─ /         → 静态前端页面     │
│    └─ /api/*    → 代理到后端       │
├─────────────────────────────────────┤
│  Spring Boot (Port 8086)            │
│    └─ 后端 API 服务                │
└─────────────────────────────────────┘
```

## 📁 目录结构

```
back_end-service/
├── Dockerfile                    # 一体化部署（推荐）
├── Dockerfile.backend            # 仅后端
├── Dockerfile.frontend           # 仅前端
├── docker-entrypoint.sh          # 启动脚本
├── pom.xml                       # Maven 配置（待创建）
├── README.md                     # 本文件
├── src/                          # 后端源码（待创建）
│   └── main/
│       ├── java/
│       └── resources/
└── web/                          # 前端源码（待创建）
    ├── package.json
    ├── src/
    ├── public/
    └── nginx.conf                # Nginx 配置
```

## 🚀 部署方式

### 方式一：一体化部署（推荐）

使用 `Dockerfile` 在一个容器中同时运行前后端：

```bash
# 构建镜像
docker build -t backend-service .

# 运行容器
docker run -d \
  -p 80:80 \
  -p 8086:8086 \
  --name backend-service \
  backend-service
```

访问：
- 前端: http://localhost
- API: http://localhost:8086/api

### 方式二：分离部署

#### 后端服务

```bash
docker build -f Dockerfile.backend -t backend-service-api .
docker run -d -p 8086:8086 --name backend-api backend-service-api
```

#### 前端服务

```bash
docker build -f Dockerfile.frontend -t backend-service-web .
docker run -d -p 80:80 --name backend-web backend-service-web
```

## 📝 开发指南

### 后端开发

1. **创建 Spring Boot 项目**

```xml
<!-- pom.xml -->
<project>
    <parent>
        <groupId>com.liveroom</groupId>
        <artifactId>services</artifactId>
        <version>1.0.0</version>
    </parent>
    
    <artifactId>back_end-service</artifactId>
    <version>1.0.0</version>
    
    <dependencies>
        <dependency>
            <groupId>com.liveroom</groupId>
            <artifactId>common</artifactId>
            <version>1.0.0</version>
        </dependency>
        <!-- 其他依赖 -->
    </dependencies>
</project>
```

2. **应用配置**

```yaml
# src/main/resources/application.yml
spring:
  application:
    name: back-end-service
  datasource:
    url: jdbc:mysql://mysql:3306/live_backend_db
    username: root
    password: root

server:
  port: 8086
```

### 前端开发

1. **创建 Vue 项目**

```bash
cd web
npm init vue@latest
```

2. **配置代理**

```javascript
// vite.config.js
export default {
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8086',
        changeOrigin: true
      }
    }
  }
}
```

3. **Nginx 配置**

```nginx
# web/nginx.conf
server {
    listen 80;
    server_name localhost;

    location / {
        root /usr/share/nginx/html;
        index index.html;
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8086;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 🔧 Docker Compose 配置

```yaml
# 在主 docker-compose.yml 中添加
back-end-service:
  build:
    context: ./services/back_end-service
    dockerfile: Dockerfile
  container_name: back-end-service
  ports:
    - "80:80"
    - "8086:8086"
  environment:
    - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/live_backend_db
    - SPRING_DATASOURCE_USERNAME=root
    - SPRING_DATASOURCE_PASSWORD=root
    - MYSQL_HOST=mysql
    - REDIS_HOST=shared-redis
    - JAVA_OPTS=-Xms512m -Xmx1024m
  depends_on:
    mysql:
      condition: service_healthy
    shared-redis:
      condition: service_healthy
  networks:
    - live-network
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost/"]
    interval: 10s
    timeout: 5s
    retries: 5
  restart: unless-stopped
```

## 📦 构建说明

### 当前状态

所有 Dockerfile 都是**占位文件**，包含：
- ✅ 基础镜像配置
- ✅ 端口暴露
- ✅ 健康检查
- ✅ 环境变量
- ⏳ 实际应用代码（待实现）

### 激活步骤

1. **创建后端应用**
   - 创建 `pom.xml`
   - 创建 Spring Boot 应用代码
   - 编译生成 JAR 文件

2. **创建前端应用**
   - 初始化 Vue 项目
   - 开发前端页面
   - 配置构建脚本

3. **更新 Dockerfile**
   - 取消注释实际的 COPY 和 RUN 命令
   - 移除占位命令

4. **添加到管理脚本**
   - 更新 `deployment/manage.sh`
   - 更新 `deployment/manage.bat`
   - 在服务列表中添加 `back-end-service`

## 🎯 功能规划

### 后端功能
- 🔲 系统管理
- 🔲 用户管理
- 🔲 数据统计
- 🔲 日志查询
- 🔲 配置管理

### 前端功能
- 🔲 管理员登录
- 🔲 Dashboard
- 🔲 数据报表
- 🔲 系统设置
- 🔲 实时监控

## ⚠️ 注意事项

1. **端口冲突**: 确保 80 和 8086 端口未被占用
2. **Nginx 配置**: 前端路由需要配置 `try_files`
3. **跨域问题**: API 代理配置要正确
4. **资源路径**: 前端构建时注意静态资源路径

## 📚 相关文档

- [系统架构文档](../../docs/JavaEE%20架构与应用小组作业.md)
- [部署指南](../../deployment/README.md)
- [服务说明](../../deployment/SERVICES_INFO.md)

---

**状态**: 🔄 占位阶段 - 待开发实现  
**最后更新**: 2026-01-02
