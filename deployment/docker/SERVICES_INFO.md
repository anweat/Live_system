# 服务架构说明

## 服务列表

### 主 Docker Compose 管理的服务

以下服务通过根目录的 `docker-compose.yml` 统一管理：

#### 基础设施服务
- **MySQL** (3306) - 主数据库
- **Shared Redis** (6379) - 中央 Redis 缓存
- **Nginx** (80/443) - 反向代理和负载均衡

#### 业务服务
- **Anchor Service** (8081) - 主播服务
  - 位置: `services/anchor-service/`
  - Dockerfile: ✅
  - 功能: 主播信息管理、直播间管理
  
- **Audience Service** (8082) - 观众服务
  - 位置: `services/audience-service/`
  - Dockerfile: ✅
  - 功能: 观众信息管理、打赏处理

- **Finance Service** (8083) - 财务服务
  - 位置: `services/finance-service/`
  - Dockerfile: ✅
  - 功能: 打赏结算、分成管理、提现处理

- **Redis Service** (8085) - Redis 服务
  - 位置: `services/redis-service/`
  - Dockerfile: ✅
  - 功能: 分布式缓存管理、幂等性检查

- **Mock Service** (8090) - 模拟测试服务
  - 位置: `services/mock-service/`
  - Dockerfile: ✅
  - 功能: 生成测试数据、模拟用户行为

### 独立部署服务

以下服务使用独立的 docker-compose.yml 配置：

#### DB Service (8084)
- **位置**: `services/db-service/`
- **配置文件**: `services/db-service/docker-compose.yml`
- **Dockerfile**: ✅
- **功能**: 数据库访问层服务、MyBatis 集成
- **管理方式**: 通过管理脚本菜单选项 [13] 进行独立管理

**为什么独立部署？**
- DB Service 包含独立的 MySQL 实例配置
- 有专门的初始化 SQL 脚本
- 可以独立于主服务进行数据库维护
- 适合开发和测试环境的数据隔离

## 服务依赖关系

```
基础设施层:
├── MySQL (数据存储)
├── Shared Redis (缓存和锁)
└── Nginx (网关)

业务服务层:
├── Anchor Service ──────┐
├── Audience Service ────┼──> 依赖 MySQL + Shared Redis
├── Finance Service ─────┤
├── Redis Service ───────┤
└── Mock Service ────────┘

独立服务:
└── DB Service (独立 MySQL + 数据访问层)
```

## 编译依赖

所有服务都依赖 `common` 模块：

```
services/common/
├── 通用实体类
├── 工具类
├── 常量定义
└── 基础配置

↓ 依赖

services/*/
├── anchor-service
├── audience-service
├── finance-service
├── redis-service
└── mock-service
```

**重要**: 修改 `common` 模块后，必须重新编译所有服务。

## Dockerfile 位置

所有服务都有独立的 Dockerfile：

```
services/
├── anchor-service/Dockerfile
├── audience-service/Dockerfile
├── finance-service/Dockerfile
├── redis-service/Dockerfile
├── mock-service/Dockerfile
└── db-service/Dockerfile
```

## Docker Compose 配置

### 主配置文件
- **位置**: `docker-compose.yml`
- **管理服务**: 所有基础设施 + 5个业务服务
- **启动方式**: `docker-compose up -d`

### 独立配置文件
- **位置**: `services/db-service/docker-compose.yml`
- **管理服务**: db-service + 独立 MySQL
- **启动方式**: 
  - 手动: `cd services/db-service && docker-compose up -d`
  - 脚本: 使用管理脚本菜单选项 [13]

## 网络配置

所有服务运行在同一个 Docker 网络中：

```yaml
networks:
  live-network:
    driver: bridge
```

服务之间可以通过服务名进行通信：
- `mysql` -> MySQL 数据库
- `shared-redis` -> Redis 缓存
- `anchor-service` -> 主播服务
- 等等...

## 数据持久化

### 数据卷

```yaml
volumes:
  mysql_data:     # MySQL 数据
  redis_data:     # Redis 数据
```

### 初始化脚本

- **位置**: `services/db-service/sql/`
- **文件**:
  - `01-init-db1-audience-service.sql` - 观众服务数据库
  - `02-init-db2-finance-service.sql` - 财务服务数据库

## 健康检查

所有服务都配置了健康检查：

- **MySQL**: `mysqladmin ping`
- **Redis**: `redis-cli ping`
- **业务服务**: HTTP 健康检查端点
  - `/actuator/health` (Spring Boot Actuator)
  - 或自定义健康检查端点

## 使用建议

### 开发环境
1. 使用主 docker-compose.yml 启动所有服务
2. Mock Service 用于生成测试数据
3. 通过管理脚本快速重启单个服务

### 测试环境
1. 可以使用独立的 db-service 进行数据隔离
2. 使用 Mock Service 进行压力测试
3. Finance Service 可以独立测试结算逻辑

### 生产环境
1. 考虑将基础设施服务独立部署
2. 业务服务可以根据负载进行水平扩展
3. 使用 Kubernetes 进行编排（参考 deployment/k8s/）

## 故障排查

### 服务启动失败

1. **检查端口占用**
   ```bash
   # Windows
   netstat -ano | findstr "端口号"
   
   # Linux
   lsof -i :端口号
   ```

2. **检查依赖服务**
   - 业务服务依赖 MySQL 和 Redis
   - 确保基础设施服务先启动

3. **查看日志**
   ```bash
   # 使用管理脚本: 选项 [11]
   # 或直接查看
   docker-compose logs -f 服务名
   ```

### 编译问题

1. **Common 模块未安装**
   ```bash
   cd services
   mvn clean install -pl common -am
   ```

2. **依赖冲突**
   - 清理 Maven 缓存
   - 使用管理脚本选项 [2] 清理编译缓存

### 独立服务问题

DB Service 使用独立配置，如果遇到问题：

```bash
# 方式1: 使用管理脚本
选择 [13] -> 查看日志/状态

# 方式2: 手动管理
cd services/db-service
docker-compose logs -f
docker-compose ps
```

## 扩展服务

如果需要添加新服务：

1. **创建服务目录**
   ```
   services/new-service/
   ├── Dockerfile
   ├── pom.xml
   └── src/
   ```

2. **更新主 pom.xml**
   ```xml
   <modules>
       <module>new-service</module>
   </modules>
   ```

3. **更新 docker-compose.yml**
   ```yaml
   new-service:
     build: ./services/new-service
     ports:
       - "端口:端口"
     ...
   ```

4. **更新管理脚本**
   - 在 `BUSINESS_SERVICES` 中添加服务名
   - 更新端口映射说明

## 相关文档

- [管理脚本使用指南](README.md)
- [快速启动指南](QUICKSTART.md)
- [系统架构文档](../docs/JavaEE%20架构与应用小组作业.md)
- [数据库设计](../services/db-service/docs/数据库设计文档.md)
