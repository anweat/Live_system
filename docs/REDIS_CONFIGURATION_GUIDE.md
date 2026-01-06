# Redis 灵活开关系统 - 部署指南

## 📋 概述

本项目实现了一个**灵活的 Redis 配置开关系统**，支持三种部署模式：

```
┌─────────────────────────────────────────────────────┐
│         灵活的 Redis 架构（支持三种模式）             │
└─────────────────────────────────────────────────────┘

模式 1: 开发环境（推荐）
├─ 禁用 Redis
├─ 使用内存缓存
├─ 本地快速开发
└─ spring.redis.enabled: false

模式 2: 容器化部署（推荐生产）
├─ 各服务内置 Redis（本地缓存）
├─ 中心 Redis-Service（分布式操作）
├─ 最高性能
└─ spring.redis.enabled: true, REDIS_ENABLED: true

模式 3: 纯分布式（备选）
├─ 禁用本地 Redis
├─ 仅使用 redis-service API
├─ 资源消耗低
└─ spring.redis.enabled: false
```

## 🎯 三种部署模式详解

### 模式 1：开发环境（无 Redis）

**适用场景**:
- 本地开发调试
- 不需要分布式特性
- 快速迭代

**配置**:
```yaml
spring:
  redis:
    enabled: false  # 禁用 Redis
```

**优点**:
- ✅ 无需安装 Redis
- ✅ 开发快速
- ✅ 调试简单

**缺点**:
- ❌ 幂等性检查使用本地内存（进程重启丢失）
- ❌ 无分布式锁能力

### 模式 2：容器化部署（推荐）

**适用场景**:
- Docker 容器化部署
- 生产环境
- 需要高性能

**特点**:
- 各服务内置本地 Redis → 零延迟缓存
- 中心 Redis-Service → 分布式操作
- Nginx 负载均衡

**架构**:
```
Nginx (负载均衡)
  ├─ Anchor Service (内置 Redis:DB0)
  ├─ Audience Service (内置 Redis:DB1)
  └─ Finance Service (内置 Redis:DB2)
       ↓
   Redis-Service (API 网关)
       ↓
  Shared Redis (中心存储)
```

**启动命令**:
```bash
docker-compose up -d
```

**配置**:
```yaml
# application-production.yml
spring:
  redis:
    enabled: true
    host: localhost  # 本地 Redis
    database: 0      # 各服务不同的 DB
```

### 模式 3：纯分布式（备选）

**适用场景**:
- 资源受限的环境
- 只需要分布式操作（无本地缓存）

**配置**:
```yaml
spring:
  redis:
    enabled: false  # 禁用本地 Redis
```

**注意**: 仍需通过 RestTemplate 调用 redis-service

## 🚀 快速开始

### 1. 开发环境（无 Redis）

#### 编译
```bash
cd services
mvn clean package -DskipTests
```

#### 运行
```bash
cd anchor-service
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.redis.enabled=false"
```

### 2. 容器化部署（推荐）

#### 构建所有镜像
```bash
# 构建 anchor-service
cd services/anchor-service
mvn clean package -DskipTests
docker build -t anchor-service:latest .

# 构建 audience-service
cd services/audience-service
mvn clean package -DskipTests
docker build -t audience-service:latest .

# 构建 redis-service
cd services/redis-service
mvn clean package -DskipTests
docker build -t redis-service:latest .
```

#### 使用 docker-compose 启动
```bash
# 在项目根目录
docker-compose up -d

# 验证服务
docker-compose ps

# 查看日志
docker-compose logs -f anchor-service
```

#### 验证部署
```bash
# 检查 Anchor Service
curl http://localhost:8081/anchor/api/v1/anchor/health

# 检查 Redis Service
curl http://localhost:8085/redis/api/v1/cache/health

# 连接本地 Redis（在容器内）
docker exec -it anchor-service redis-cli -p 6379

# 在 Redis 中验证
redis> DBSIZE
redis> KEYS *
redis> QUIT
```

### 3. 非容器化部署

#### Linux 本地安装 Redis

```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# CentOS/RHEL
sudo yum install redis

# 启动 Redis
redis-server --port 6379 --daemonize yes

# 验证
redis-cli ping
# 输出: PONG
```

#### 启动应用

```bash
# 设置环境变量
export SPRING_REDIS_HOST=localhost
export SPRING_REDIS_PORT=6379
export SPRING_REDIS_ENABLED=true

# 启动服务
cd services/anchor-service
java -jar target/anchor-service-1.0.0.jar
```

## 🔧 配置详解

### 配置属性类：RedisProperties

```java
@Component
@ConfigurationProperties(prefix = "spring.redis")
public class RedisProperties {
    
    // 是否启用 Redis（关键开关）
    private Boolean enabled = true;
    
    // 主机名
    private String host = "localhost";
    
    // 端口号
    private Integer port = 6379;
    
    // 数据库编号
    private Integer database = 0;
    
    // 密码
    private String password = "";
    
    // 连接超时
    private Integer timeout = 2000;
    
    // 连接池配置
    private Pool pool = new Pool();
}
```

### application.yml 配置示例

#### 开发环境
```yaml
spring:
  redis:
    enabled: false  # 关闭 Redis
```

#### 生产环境
```yaml
spring:
  redis:
    enabled: true
    host: localhost
    port: 6379
    database: 0
    timeout: 2000
    password: your_password
    lettuce:
      pool:
        max-active: 16
        max-idle: 8
        min-idle: 2
```

## 🎨 条件装配实现

### RedisConditionalConfig

使用 Spring 的 `@ConditionalOnProperty` 注解实现条件装配：

```java
@Configuration
public class RedisConditionalConfig {

    // 仅当 spring.redis.enabled=true 时创建
    @Bean
    @ConditionalOnProperty(
        prefix = "spring.redis",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true
    )
    public RedisTemplate<String, Object> redisTemplate(...) {
        // ...
    }
}
```

**优点**:
- ✅ 无缝开关，无需代码改动
- ✅ 支持多环境配置
- ✅ 自动装配管理

## 📝 IdempotentAspect 双模式实现

### 模式切换逻辑

```java
@Aspect
@Component
public class IdempotentAspect {
    
    @Around("@annotation(idempotentAnnotation)")
    public Object idempotentAround(...) {
        boolean redisEnabled = redisProperties.isEnabled();
        
        if (redisEnabled) {
            // 使用 Redis-Service 检查幂等性
            checkIdempotencyWithRedis(...);
        } else {
            // 降级到本地内存
            checkIdempotencyLocal(...);
        }
    }
}
```

### 本地内存降级方案

```java
// 使用 ConcurrentHashMap 存储幂等性检查
private static final Map<String, Long> LOCAL_IDEMPOTENT_CACHE = 
    new ConcurrentHashMap<>();

private boolean checkIdempotencyLocal(String key, long ttl) {
    Long expiryTime = LOCAL_IDEMPOTENT_CACHE.get(key);
    
    if (expiryTime == null) {
        // 首次请求
        LOCAL_IDEMPOTENT_CACHE.put(key, System.currentTimeMillis() + ttl * 1000);
        return true;
    }
    
    if (System.currentTimeMillis() > expiryTime) {
        // 已过期
        LOCAL_IDEMPOTENT_CACHE.remove(key);
        return true;
    }
    
    // 重复请求
    return false;
}
```

## 🐳 Docker 相关

### Dockerfile 结构

```dockerfile
# 使用多阶段构建，包含 Redis
FROM redis:7-alpine AS redis-base
FROM openjdk:11-jre-slim

COPY --from=redis-base /usr/local/bin/redis-server /usr/local/bin/redis-server

# 复制启动脚本
COPY docker-entrypoint.sh /app/docker-entrypoint.sh

ENTRYPOINT ["/app/docker-entrypoint.sh"]
```

### 启动脚本逻辑

```bash
#!/bin/sh

REDIS_ENABLED=${REDIS_ENABLED:-true}

if [ "$REDIS_ENABLED" = "true" ]; then
    redis-server --bind 127.0.0.1 --port 6379 --daemonize yes
fi

# 启动 Spring Boot 应用
exec java ${JAVA_OPTS} -jar /app/app.jar
```

## 📊 部署对比

| 特性 | 开发环境 | 容器化 | 非容器化 |
|------|---------|--------|---------|
| Redis 支持 | ❌ | ✅ | ✅ |
| 幂等性 | 内存 | 分布式 | 分布式 |
| 性能 | 快速 | 最优 | 中等 |
| 部署复杂度 | 低 | 中 | 高 |
| 推荐场景 | 开发 | 生产 | 特殊需求 |

## 🔍 故障排查

### 问题 1: Redis 连接失败

```bash
# 检查 Redis 是否运行
redis-cli ping

# 检查端口
lsof -i :6379

# 查看配置
echo "spring.redis.enabled: $SPRING_REDIS_ENABLED"
```

### 问题 2: 幂等性检查不工作

```bash
# 检查是否启用了 Redis
curl http://localhost:8085/redis/api/v1/cache/health

# 检查应用日志
docker logs anchor-service | grep -i "redis\|idempotent"
```

### 问题 3: Docker 容器启动失败

```bash
# 查看启动脚本输出
docker logs anchor-service

# 进入容器调试
docker run -it --entrypoint /bin/sh anchor-service:latest

# 在容器内检查
redis-cli ping
java -version
```

## 📚 最佳实践

1. **开发环境**: 关闭 Redis（`enabled: false`），快速迭代
2. **生产环境**: 启用 Redis（`enabled: true`），获得最优性能
3. **容器化**: 使用内置 Redis，无需外部依赖
4. **监控**: 监控 Redis 内存、连接数、命中率
5. **备份**: 定期备份 Redis 数据

## 📖 完整示例

### 启动完整系统

```bash
# 1. 编译所有模块
cd services
mvn clean package -DskipTests

# 2. 构建 Docker 镜像
docker-compose build

# 3. 启动所有服务
docker-compose up -d

# 4. 验证部署
docker-compose ps

# 5. 查看日志
docker-compose logs -f

# 6. 运行测试
curl -X POST http://localhost:8085/redis/api/v1/lock/check-idempotency \
  -G --data-urlencode "idempotentKey=test-123" \
  --data-urlencode "ttl=3600"
```

### 停止系统

```bash
# 停止所有容器
docker-compose down

# 清理数据卷
docker-compose down -v

# 只停止特定服务
docker-compose stop anchor-service
```

---

**更新时间**: 2026-01-02  
**版本**: 1.0.0
