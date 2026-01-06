# Redis-Service - 分布式缓存服务

![Redis](https://img.shields.io/badge/Redis-7.0+-red)
![Java](https://img.shields.io/badge/Java-11+-green)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.0-green)
![License](https://img.shields.io/badge/License-MIT-blue)

## 📖 项目简介

**Redis-Service** 是直播打赏系统中的分布式缓存服务，采用**混合缓存架构**：

- **本地缓存**: 各微服务拥有自己的 Redis 实例，用于热点数据缓存
- **分布式服务**: Redis-Service 提供统一的幂等性检查和分布式锁接口

这种设计**结合了本地缓存的高性能和分布式缓存的一致性**。

## 🏗️ 架构特点

```
性能最优
   ↓
[本地 Redis]  ←→  [Redis-Service API]  ←→  [Shared Redis]
  (零延迟)        (幂等性、分布式锁)      (中心存储)
```

| 层级 | 职责 | 特点 |
|------|------|------|
| **本地 Redis** | 热点数据缓存 | 零网络延迟，高性能 |
| **Redis-Service** | 分布式操作 | 统一管理，易于扩展 |
| **Shared Redis** | 中心存储 | 持久化，高可用 |

## 🚀 快速开始

### 编译

```bash
cd services/redis-service
mvn clean package -DskipTests
```

### 本地运行

```bash
# 启动 Redis
redis-server

# 启动 Redis-Service
java -jar target/redis-service-1.0.0.jar \
  --spring.redis.host=localhost \
  --spring.redis.port=6379
```

### Docker 运行

```bash
# 构建镜像
docker build -t redis-service:latest .

# 运行容器
docker run -d \
  --name redis-service \
  -p 8085:8085 \
  -e SPRING_REDIS_HOST=redis \
  redis-service:latest
```

## 📚 API 文档

### 基础 URL
```
http://localhost:8085/redis/api/v1
```

### 缓存操作

#### 设置缓存
```bash
POST /cache/set
?key=user:123&value={"name":"Tom"}&ttl=3600
```

#### 获取缓存
```bash
GET /cache/get?key=user:123
```

#### 删除缓存
```bash
DELETE /cache/delete?key=user:123
```

#### 检查键存在性
```bash
GET /cache/exists?key=user:123
```

### 分布式锁

#### 获取锁
```bash
POST /lock/try-lock
?lockKey=task:demo&lockValue=node1&lockTimeout=30000
```

#### 释放锁
```bash
POST /lock/release-lock
?lockKey=task:demo&lockValue=node1
```

#### 检查幂等性（防重复提交）
```bash
POST /lock/check-idempotency
?idempotentKey=trace-123&ttl=3600

Response: 
{
  "success": true,  // true=首次请求，false=重复请求
  "message": "First request, proceed"
}
```

### 健康检查

```bash
GET /cache/health
```

## 💻 使用示例

### 1. 幂等性防重复提交

```java
@RestController
@RequestMapping("/api/v1/order")
public class OrderController {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @PostMapping("/submit")
    public BaseResponse<OrderVO> submitOrder(
            @RequestHeader("X-Trace-Id") String traceId,
            @RequestBody OrderDTO order) {
        
        // 检查幂等性
        String url = "http://redis-service:8085/redis/api/v1/lock/check-idempotency" +
                "?idempotentKey=" + traceId + "&ttl=3600";
        BaseResponse<Boolean> resp = restTemplate.postForObject(url, null, BaseResponse.class);
        
        if (!resp.getSuccess()) {
            throw new BusinessException("Duplicate request");
        }
        
        // 处理订单逻辑
        OrderVO result = orderService.submit(order);
        return BaseResponse.success(result);
    }
}
```

### 2. 定时任务分布式锁

```java
@Service
public class StatisticService {
    
    @Autowired
    private RestTemplate restTemplate;
    
    @Scheduled(cron = "0 0 * * * ?")  // 每小时执行
    public void statisticTop10() {
        String lockKey = "task:top10:statistic";
        String lockValue = getNodeId();
        
        try {
            // 尝试获取锁
            String url = "http://redis-service:8085/redis/api/v1/lock/try-lock" +
                    "?lockKey=" + lockKey + "&lockValue=" + lockValue + "&lockTimeout=1800000";
            BaseResponse<Boolean> resp = restTemplate.postForObject(url, null, BaseResponse.class);
            
            if (!resp.getSuccess()) {
                return; // 其他节点已获取锁，本节点退出
            }
            
            // 执行统计逻辑
            executeStatistic();
            
        } finally {
            // 释放锁
            String releaseUrl = "http://redis-service:8085/redis/api/v1/lock/release-lock" +
                    "?lockKey=" + lockKey + "&lockValue=" + lockValue;
            restTemplate.postForObject(releaseUrl, null, BaseResponse.class);
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
    
    @Autowired
    private AnchorMapper anchorMapper;
    
    public Anchor getAnchor(Long anchorId) {
        String cacheKey = "anchor:" + anchorId;
        
        // 查询本地 Redis
        Anchor cached = (Anchor) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }
        
        // 查询数据库
        Anchor anchor = anchorMapper.selectById(anchorId);
        if (anchor != null) {
            // 写入本地 Redis (TTL: 30分钟)
            redisTemplate.opsForValue().set(cacheKey, anchor, 30, TimeUnit.MINUTES);
        }
        
        return anchor;
    }
}
```

## 📦 项目结构

```
redis-service/
├── src/main/java/com/liveroom/redisservice/
│   ├── RedisServiceApplication.java          # 启动类
│   ├── config/
│   │   └── RedisConfig.java                 # Redis 配置
│   ├── service/
│   │   ├── RedisCacheService.java           # 缓存服务
│   │   └── RedisDistributedLockService.java # 分布式锁服务
│   └── controller/
│       ├── RedisCacheController.java        # 缓存 REST API
│       └── RedisDistributedLockController.java  # 锁 REST API
├── src/main/resources/
│   └── application.yml                       # 配置文件
├── docs/
│   ├── Redis架构设计指南.md
│   └── 部署指南.md
├── pom.xml
├── Dockerfile
└── README.md
```

## 🔧 配置说明

### application.yml

```yaml
server:
  port: 8085

spring:
  application:
    name: redis-service
  redis:
    host: localhost      # Redis 服务器地址
    port: 6379
    database: 10        # 数据库编号（不同于各微服务的 DB）
    timeout: 2000       # 连接超时（毫秒）
    password:           # Redis 密码（如果有）
    lettuce:
      pool:
        max-active: 32   # 连接池最大连接数
        max-idle: 16     # 最大空闲连接数
        min-idle: 8      # 最小空闲连接数
        max-wait: -1ms   # 最大等待时间（-1表示无限等待）
```

## 🎯 最佳实践

1. **本地 Redis 用于热点缓存**
   - 频繁访问的数据（用户信息、主播信息等）
   - TTL 设置为 10-30 分钟

2. **分布式操作用 redis-service**
   - 幂等性检查（防重复提交）
   - 分布式锁（定时任务、事务）
   - TTL 设置为 1-24 小时

3. **错误处理**
   - Redis-Service 不可用时，有降级方案
   - 捕获异常，允许系统继续运行

4. **监控告警**
   - 监控 Redis 内存使用率
   - 监控连接数和命中率
   - 设置告警阈值

5. **定期备份**
   - 使用 Redis AOF 或 RDB 备份
   - 生产环境使用主从 + 哨兵

## 🔐 生产环境

### 高可用部署

使用 Redis Sentinel（哨兵）：

```yaml
redis-sentinel:
  image: redis:7-alpine
  command: redis-sentinel /etc/redis/sentinel.conf
  volumes:
    - ./sentinel.conf:/etc/redis/sentinel.conf
```

### Redis Cluster

对于超大规模应用，使用 Redis Cluster：

```bash
redis-cli --cluster create 127.0.0.1:7000 127.0.0.1:7001 ...
```

## 📊 性能指标

| 指标 | 值 |
|------|-----|
| 幂等性检查延迟 | < 10ms |
| 分布式锁获取延迟 | < 10ms |
| 本地缓存延迟 | < 1ms |
| 吞吐量 | > 10,000 req/s |

## 🐛 故障排查

### Redis 连接失败

```bash
# 检查 Redis 是否运行
redis-cli ping

# 查看 Redis 日志
tail -f /var/log/redis.log
```

### API 返回错误

检查 Redis-Service 日志：
```bash
docker logs -f redis-service
```

### 内存持续增长

- 检查 Redis 过期策略
- 设置合理的 TTL
- 使用 `redis-cli INFO memory` 查看内存详情

## 📝 更新日志

### v1.0.0 (2026-01-02)
- ✅ 初版发布
- ✅ 实现幂等性检查
- ✅ 实现分布式锁
- ✅ 实现缓存 REST API

## 📄 许可证

MIT License

## 👥 贡献指南

欢迎提交 Issue 和 Pull Request！

## 📞 联系方式

- 📧 Email: team@liveroom.com
- 💬 Issues: https://github.com/liveroom/live-system/issues

---

**Made with ❤️ by Live System Team**
