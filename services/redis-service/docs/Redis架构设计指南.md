# Redis 架构设计指南

## 📊 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                        Nginx (负载均衡)                      │
└──────┬──────────┬──────────┬──────────┬──────────────────────┘
       │          │          │          │
   ┌───▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼──────┐
   │Anchor│  │Audience│ │Finance│ │Data-Analysis
   │Svc   │  │Svc    │ │Svc   │ │Svc
   └───┬──┘  └───┬──┘  └───┬──┘  └───┬──────┘
       │          │          │          │
   ┌───▼──┐  ┌───▼──┐  ┌───▼──┐  ┌───▼──────┐
   │本地   │  │本地   │ │本地   │ │本地
   │Redis1 │  │Redis2 │ │Redis3 │ │Redis4
   │:6379  │  │:6379  │ │:6379  │ │:6379
   └──────┘  └──────┘  └──────┘  └──────────┘
       │          │          │          │
       └──────────┴──────────┴──────────┘
              Redis API 调用
                   │
                   ▼
        ┌──────────────────────────┐
        │  Redis-Service (API网关) │
        │  Port: 8085              │
        │  - 幂等性检查            │
        │  - 分布式锁             │
        │  - 跨服务共享数据       │
        └──────────┬───────────────┘
                   │
              ┌────▼─────────┐
              │ Shared Redis  │
              │ :6379/DB:10   │  (中心化存储)
              └───────────────┘
```

## 🏗️ 各层职责

### 1. **各微服务本地 Redis**

**目的**: 提供本地热点缓存，零网络延迟

**使用场景**:
- 主播基础信息缓存
- 直播间信息缓存  
- 用户信息缓存
- TOP10 榜单缓存
- 打赏数据缓存

**配置**:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0/1/2/3  # 各服务使用不同的DB
    timeout: 2000
    lettuce:
      pool:
        max-active: 16
```

**优势**:
- ✅ 零网络延迟
- ✅ 高并发性能
- ✅ 服务隔离（各自独立的DB）
- ✅ 故障隔离（一个 Redis 宕机不影响其他服务）

### 2. **Redis-Service (API 网关层)**

**目的**: 提供统一的分布式操作接口

**运行端口**: 8085

**专一职责**:
- 幂等性检查（防重复提交）
- 分布式锁（定时任务、事务）
- 跨服务数据共享
- Redis 健康检查

**关键 API**:

#### 幂等性检查
```bash
# 检查是否重复请求
POST /redis/api/v1/lock/check-idempotency
  ?idempotentKey=trace-xxx&ttl=3600

# 响应
{
  "success": true,  // true=首次请求，false=重复请求
  "message": "First request, proceed"
}
```

#### 分布式锁
```bash
# 获取锁
POST /redis/api/v1/lock/try-lock
  ?lockKey=task:top10&lockValue=node1&lockTimeout=30000

# 释放锁
POST /redis/api/v1/lock/release-lock
  ?lockKey=task:top10&lockValue=node1

# 检查锁状态
GET /redis/api/v1/lock/is-locked?lockKey=task:top10
```

#### 缓存操作
```bash
# 设置缓存
POST /redis/api/v1/cache/set
  ?key=user:123&value={"name":"Tom"}&ttl=3600

# 获取缓存
GET /redis/api/v1/cache/get?key=user:123

# 删除缓存
DELETE /redis/api/v1/cache/delete?key=user:123
```

### 3. **Shared Redis (中心存储)**

**目的**: 存储需要分布式共享的数据

**运行端口**: 6379/DB:10

**存储内容**:
- 所有幂等性检查的 Key
- 所有分布式锁
- 跨服务共享数据

**特点**:
- 中心化管理
- 高可用配置（主从 + 哨兵）
- 备份策略完善

## 🔧 部署配置

### Docker Compose 配置示例

```yaml
version: '3.8'

services:
  # 共享 Redis (中心存储)
  shared-redis:
    image: redis:7-alpine
    container_name: shared-redis
    ports:
      - "6379:6379"
    volumes:
      - shared_redis_data:/data
    command: >
      redis-server
      --appendonly yes
      --requirepass ''
    networks:
      - live-network

  # Redis-Service (API 网关)
  redis-service:
    build:
      context: ./redis-service
      dockerfile: Dockerfile
    container_name: redis-service
    ports:
      - "8085:8085"
    environment:
      - SPRING_REDIS_HOST=shared-redis
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_DATABASE=10
    depends_on:
      - shared-redis
    networks:
      - live-network

  # 主播服务 (本地 Redis)
  anchor-service:
    build:
      context: ./anchor-service
      dockerfile: Dockerfile
    container_name: anchor-service
    ports:
      - "8081:8081"
    environment:
      - SPRING_REDIS_HOST=localhost
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_DATABASE=0
    depends_on:
      - redis-service
    networks:
      - live-network

  # 观众服务 (本地 Redis)
  audience-service:
    build:
      context: ./audience-service
      dockerfile: Dockerfile
    container_name: audience-service
    ports:
      - "8082:8082"
    environment:
      - SPRING_REDIS_HOST=localhost
      - SPRING_REDIS_PORT=6379
      - SPRING_REDIS_DATABASE=1
    depends_on:
      - redis-service
    networks:
      - live-network

  # Nginx (负载均衡)
  nginx:
    image: nginx:alpine
    container_name: nginx
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx/docker/nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - redis-service
      - anchor-service
      - audience-service
    networks:
      - live-network

volumes:
  shared_redis_data:

networks:
  live-network:
    driver: bridge
```

## 📝 使用示例

### 1. 防重复提交（幂等性）

**方式 1: 使用注解（自动）**
```java
@Idempotent(key = "#traceId", timeout = 5, unit = TimeUnit.MINUTES)
public void submitOrder(String traceId, Order order) {
    // 幂等性自动通过 redis-service 检查
    // ...
}
```

**方式 2: 手动调用**
```java
@RestClient
interface RedisServiceClient {
    @PostMapping("/redis/api/v1/lock/check-idempotency")
    BaseResponse<Boolean> checkIdempotency(
        @RequestParam String idempotentKey,
        @RequestParam long ttl
    );
}

// 使用
boolean isFirstRequest = redisClient.checkIdempotency("trace-123", 3600).getData();
if (!isFirstRequest) {
    throw new BusinessException("Duplicate request");
}
```

### 2. 定时任务分布式锁

```java
@Scheduled(cron = "0 0 * * * ?")  // 每小时执行
public void statisticTop10() {
    String lockKey = "task:top10";
    String lockValue = "node-" + InetAddress.getLocalHost().getHostName();
    
    // 尝试获取锁
    if (redisServiceClient.tryLock(lockKey, lockValue, 1800000).getData()) {
        try {
            // 执行统计逻辑
            statisticData();
        } finally {
            // 释放锁
            redisServiceClient.releaseLock(lockKey, lockValue);
        }
    }
}
```

### 3. 本地缓存查询

```java
@Service
public class AnchorService {
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    public Anchor getAnchor(Long anchorId) {
        String cacheKey = "anchor:" + anchorId;
        
        // 查询本地 Redis
        Anchor cached = (Anchor) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 查询数据库
        Anchor anchor = anchorMapper.selectById(anchorId);
        
        // 写入本地 Redis (TTL: 30分钟)
        redisTemplate.opsForValue().set(cacheKey, anchor, 30, TimeUnit.MINUTES);
        
        return anchor;
    }
}
```

## 🎯 最佳实践

1. **本地缓存用于热点数据**: 使用各服务的本地 Redis 缓存频繁访问的数据
2. **分布式操作用 redis-service**: 幂等性、分布式锁等统一通过 API 调用
3. **错误降级**: 当 redis-service 不可用时，应有降级方案（本地缓存、允许执行等）
4. **监控和告警**: 监控 Redis 内存使用、连接数、命中率等指标
5. **备份策略**: 生产环境使用 Redis 主从 + 哨兵，定期备份数据

## ⚙️ 配置参数说明

| 参数 | 说明 | 推荐值 |
|------|------|--------|
| `max-active` | 连接池最大连接数 | 16-32 |
| `max-idle` | 连接池最大空闲数 | 8-16 |
| `min-idle` | 连接池最小空闲数 | 0-8 |
| `timeout` | 连接超时 (ms) | 2000 |
| `TTL` | 缓存过期时间 | 根据场景: 10min-24h |

## 📚 参考文档

- Redis 官方文档: https://redis.io/documentation
- Spring Data Redis: https://spring.io/projects/spring-data-redis
- Redis 命令参考: https://redis.io/commands
