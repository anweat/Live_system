# Nginx 网关配置与Docker部署指南

## 文档信息
- **最后更新**: 2024年1月7日
- **版本**: 2.0
- **适用环境**: Docker Compose

---

## 一、Nginx 网关概述

Nginx 作为系统的API网关，负责统一的请求路由、负载均衡、反向代理等功能。

### 核心功能

1. **请求路由**: 根据URL前缀将请求分发到对应的后端服务
2. **负载均衡**: 支持多个后端实例的请求分配
3. **反向代理**: 隐藏后端服务细节，对外暴露统一的API
4. **静态文件服务**: 提供前端静态资源
5. **SSL/TLS支持**: HTTPS加密通信（可选）
6. **请求日志**: 记录所有API请求

---

## 二、Nginx 路由配置

### 服务映射表

| 前缀 | 后端服务 | 端口 | 功能 |
|------|---------|------|------|
| `/anchor/` | anchor-service | 8081 | 主播服务 (37接口) |
| `/audience/` | audience-service | 8082 | 观众服务 (21接口) |
| `/finance/` | finance-service | 8083 | 财务服务 (18接口) |
| `/analysis/` | data-analysis-service | 8084 | 数据分析服务 (38接口) |
| `/redis/` | redis-service | 8085 | Redis缓存服务 (15接口) |
| `/` | - | - | 前端静态资源或首页 |

### 请求示例

```bash
# 访问主播服务
curl http://localhost/anchor/api/v1/anchors

# 访问观众服务
curl http://localhost/audience/api/v1/audiences

# 访问财务服务
curl http://localhost/finance/api/v1/withdrawal

# 访问数据分析服务
curl http://localhost/analysis/api/v1/analysis/anchor/income/1

# 访问Redis服务
curl http://localhost/redis/api/v1/cache/health
```

---

## 三、Docker 部署

### 3.1 前置要求

- Docker 20.10+
- Docker Compose 2.0+
- 至少 4GB RAM
- 磁盘空间: 20GB+

### 3.2 目录结构

```
deployment/docker/
├── docker-compose.yml          # Docker Compose配置文件
├── manage.sh                   # 启动脚本（Linux/Mac）
├── manage.bat                  # 启动脚本（Windows）
├── manage.ps1                  # PowerShell启动脚本
├── QUICKSTART.md               # 快速启动指南
├── DOCKER_MIRROR_CONFIG.md     # 镜像配置说明
├── SERVICES_INFO.md            # 服务信息
├── BACKEND_SERVICE_SETUP.md    # 后端服务设置
└── Dockerfile                  # Dockerfile (可选)
```

### 3.3 快速启动

#### Linux/Mac 用户

```bash
# 进入部署目录
cd deployment/docker

# 查看可用命令
./manage.sh help

# 启动所有服务
./manage.sh start

# 检查服务状态
./manage.sh status

# 查看日志
./manage.sh logs

# 停止所有服务
./manage.sh stop

# 清理所有容器和数据
./manage.sh clean
```

#### Windows 用户（PowerShell）

```powershell
# 进入部署目录
cd deployment\docker

# 查看可用命令
.\manage.ps1 -help

# 启动所有服务
.\manage.ps1 -start

# 检查服务状态
.\manage.ps1 -status

# 查看日志
.\manage.ps1 -logs

# 停止所有服务
.\manage.ps1 -stop
```

### 3.4 Docker Compose 基本命令

```bash
# 启动所有容器（后台）
docker-compose up -d

# 查看容器状态
docker-compose ps

# 查看日志
docker-compose logs -f

# 查看特定服务的日志
docker-compose logs -f anchor-service

# 进入容器内部
docker-compose exec anchor-service bash

# 停止所有容器
docker-compose down

# 删除所有容器和数据
docker-compose down -v
```

---

## 四、Docker 配置详解

### 4.1 docker-compose.yml 结构

```yaml
version: '3.8'

services:
  # Nginx网关
  nginx:
    image: nginx:latest
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/nginx.conf:/etc/nginx/nginx.conf
      - ./nginx/certs:/etc/nginx/certs
    depends_on:
      - anchor-service
      - audience-service
      - finance-service
    networks:
      - liveroom

  # 各个微服务
  anchor-service:
    image: anchor-service:1.0.0
    ports:
      - "8081:8081"
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/liveroom
    depends_on:
      - mysql
      - redis
    networks:
      - liveroom

  # ... 其他服务配置

  # MySQL 数据库
  mysql:
    image: mysql:8.0
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: liveroom
    volumes:
      - mysql_data:/var/lib/mysql
    networks:
      - liveroom

  # Redis 缓存
  redis:
    image: redis:latest
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - liveroom

volumes:
  mysql_data:
  redis_data:

networks:
  liveroom:
    driver: bridge
```

### 4.2 环境变量配置

创建 `.env` 文件配置环境变量：

```env
# 数据库配置
MYSQL_ROOT_PASSWORD=root
MYSQL_DATABASE=liveroom
MYSQL_USER=liveroom
MYSQL_PASSWORD=liveroom123

# Redis配置
REDIS_PASSWORD=redis123

# 服务端口
ANCHOR_SERVICE_PORT=8081
AUDIENCE_SERVICE_PORT=8082
FINANCE_SERVICE_PORT=8083
ANALYSIS_SERVICE_PORT=8084
REDIS_SERVICE_PORT=8085

# Nginx配置
NGINX_HTTP_PORT=80
NGINX_HTTPS_PORT=443

# 日志级别
LOG_LEVEL=INFO
```

---

## 五、Nginx 配置文件

### 5.1 核心配置位置

```
services/nginx/
├── nginx.conf              # Nginx主配置文件
├── conf.d/
│   ├── anchor-service.conf    # 主播服务路由
│   ├── audience-service.conf  # 观众服务路由
│   ├── finance-service.conf   # 财务服务路由
│   └── ...
└── certs/                 # HTTPS证书目录
```

### 5.2 简化配置示例

```nginx
# 上游服务器定义
upstream anchor_backend {
    server anchor-service:8081;
}

upstream audience_backend {
    server audience-service:8082;
}

upstream finance_backend {
    server finance-service:8083;
}

server {
    listen 80;
    server_name localhost;

    # 主播服务路由
    location /anchor/ {
        proxy_pass http://anchor_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 观众服务路由
    location /audience/ {
        proxy_pass http://audience_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 财务服务路由
    location /finance/ {
        proxy_pass http://finance_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    # 健康检查端点
    location /health {
        return 200 "OK";
    }
}
```

---

## 六、服务启动顺序

```
1. MySQL 数据库 (必须首先启动)
   ↓
2. Redis 缓存 (必须启动)
   ↓
3. DB-Service (初始化数据库)
   ↓
4. 各微服务 (可并行启动)
   - anchor-service
   - audience-service
   - finance-service
   - data-analysis-service
   - redis-service
   ↓
5. Nginx 网关 (最后启动)
```

### 等待机制

Docker Compose 提供 `depends_on` 参数确保启动顺序，但不保证服务完全就绪。建议使用健康检查：

```yaml
services:
  anchor-service:
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8081/anchor/api/v1/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

---

## 七、常见问题与故障排除

### Q1: 容器启动失败
```bash
# 查看详细错误日志
docker-compose logs anchor-service

# 检查容器状态
docker-compose ps
```

### Q2: 数据库连接错误
```bash
# 确认MySQL正在运行
docker-compose exec mysql mysql -u root -p -e "SELECT 1"

# 检查网络连接
docker-compose exec anchor-service ping mysql
```

### Q3: Nginx 路由失败
```bash
# 进入Nginx容器
docker-compose exec nginx bash

# 测试后端连接
curl -i http://anchor-service:8081/anchor/api/v1/health
```

### Q4: 端口已被占用
```bash
# 修改 docker-compose.yml 中的端口映射
# 或停止占用端口的其他容器
lsof -i :80
kill -9 <PID>
```

### Q5: 内存不足
```bash
# 增加Docker内存限制
docker-compose down
# 编辑 docker-compose.yml 中的 deploy.resources.limits
docker-compose up -d
```

---

## 八、监控与日志

### 8.1 查看服务日志

```bash
# 查看所有日志
docker-compose logs -f

# 查看特定服务日志，显示最后100行
docker-compose logs -f --tail=100 anchor-service

# 查看特定时间范围的日志
docker-compose logs --since 10m anchor-service
```

### 8.2 日志级别配置

在 `.env` 或 `docker-compose.yml` 中配置：

```yaml
environment:
  - LOGGING_LEVEL_ROOT=INFO
  - LOGGING_LEVEL_COM_LIVEROOM=DEBUG
```

### 8.3 访问容器内部

```bash
# 进入容器
docker-compose exec anchor-service bash

# 查看应用日志
tail -f logs/application.log

# 检查环境变量
env | grep SPRING
```

---

## 九、性能优化

### 9.1 Nginx 优化

```nginx
# worker进程数
worker_processes auto;

# 连接数
events {
    worker_connections 10000;
}

# 启用gzip压缩
gzip on;
gzip_min_length 1000;
gzip_types text/plain application/json;

# 连接超时
proxy_connect_timeout 60s;
proxy_send_timeout 60s;
proxy_read_timeout 60s;
```

### 9.2 Docker 资源限制

```yaml
services:
  anchor-service:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
        reservations:
          cpus: '1'
          memory: 1G
```

### 9.3 数据库优化

```bash
# 查看MySQL进程数
docker-compose exec mysql mysql -u root -e "SHOW PROCESSLIST;"

# 调整最大连接数
docker-compose exec mysql mysql -u root -e "SET GLOBAL max_connections=1000;"
```

---

## 十、生产部署注意事项

### 10.1 安全性

- [ ] 使用HTTPS/SSL证书
- [ ] 配置防火墙规则
- [ ] 使用环境变量存储敏感信息
- [ ] 定期更新镜像和依赖
- [ ] 设置API速率限制

### 10.2 高可用

- [ ] 部署多个服务副本
- [ ] 使用负载均衡器（云厂商或F5）
- [ ] 配置健康检查和自动恢复
- [ ] 定期备份数据库
- [ ] 配置日志集中存储

### 10.3 监控告警

- [ ] 监控CPU、内存、磁盘使用率
- [ ] 监控API响应时间和错误率
- [ ] 配置告警规则
- [ ] 使用ELK/Prometheus等监控工具

---

## 十一、更新与维护

### 11.1 更新服务镜像

```bash
# 拉取最新镜像
docker pull anchor-service:latest

# 重启服务
docker-compose up -d anchor-service
```

### 11.2 数据库备份

```bash
# 导出数据库
docker-compose exec mysql mysqldump -u root -p liveroom > backup.sql

# 导入数据库
docker-compose exec -T mysql mysql -u root -p liveroom < backup.sql
```

### 11.3 清理旧数据

```bash
# 删除所有容器和数据
docker-compose down -v

# 只删除容器，保留数据
docker-compose down
```

---

## 十二、快速参考

```bash
# 启动/停止
docker-compose up -d           # 启动所有服务
docker-compose down            # 停止所有服务

# 查看状态
docker-compose ps             # 查看容器列表
docker-compose logs -f        # 查看日志

# 进入容器
docker-compose exec anchor-service bash

# 重启服务
docker-compose restart anchor-service

# 清理资源
docker-compose down -v        # 删除容器和卷
docker system prune -a        # 清理未使用的镜像
```

---

## 关联文档

- 📄 [Docker 快速启动](./QUICKSTART.md)
- 📄 [后端服务设置](./BACKEND_SERVICE_SETUP.md)
- 📄 [Docker 镜像配置](./DOCKER_MIRROR_CONFIG.md)
- 📄 [服务信息](./SERVICES_INFO.md)
- 📄 [API 总汇](../docs/ApiSum/0-API-SUMMARY.md)

---

## 技术支持

如有任何问题，请：
1. 查看日志文件
2. 运行 `./manage.sh status` 检查服务状态
3. 参考相关文档
4. 联系开发团队

