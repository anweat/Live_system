# Finance Service 快速启动指南

## 📦 项目结构已创建完成

✅ **所有文件已创建**：
- 配置文件（pom.xml, application.yml等）
- 启动类和配置类
- Repository层（数据访问）
- DTO和VO类
- Service层（核心业务逻辑）
- Controller层（API接口）
- 定时任务和Feign客户端
- Dockerfile和启动脚本

## 🔑 核心特性实现

### 1. Redis缓存 ✅
- **余额缓存**：`finance:balance:{anchorId}` - 10分钟过期
- **分成比例缓存**：`finance:commission:{anchorId}` - 24小时过期
- **提现记录缓存**：`finance:withdrawal:trace:{traceId}` - 24小时过期
- **打赏记录缓存**：`finance:recharge:{traceId}` - 7天过期
- **批次缓存**：`finance:batch:{batchId}` - 24小时过期

### 2. 幂等性检验 ✅

#### 打赏数据同步幂等性
```java
// 三重保障
1. Redis缓存检查：快速判断批次是否已处理
2. 数据库检查：existsByBatchId() 查询
3. 分布式锁：防止并发处理同一批次
```

#### 提现申请幂等性
```java
// 四重保障
1. Redis缓存检查：快速判断traceId是否存在
2. 数据库主键约束：UNIQUE KEY uk_trace_id
3. 分布式锁：防止同一主播并发提现
4. 双重数据库检查：锁内再次验证
```

### 3. 分布式锁应用 ✅
- **提现操作锁**：`withdrawal:anchor:{anchorId}` - 30秒超时
- **定时任务锁**：`task:settlement:auto` - 600秒超时
- **批次处理锁**：`sync:batch:{batchId}` - 300秒超时

### 4. 缓存更新策略 ✅
```java
// 查询时
@Cacheable - 自动缓存查询结果

// 更新时
@CacheEvict - 自动清除相关缓存

// 手动管理
redisTemplate.opsForValue().set() - 精确控制缓存
```

## 🚀 启动步骤

### 1. 准备环境
```bash
# 确保已启动
✓ MySQL (端口3306)
✓ Redis (端口6379)
✓ Consul (端口8500)
```

### 2. 构建项目
```bash
cd services/finance-service
mvn clean package -DskipTests
```

### 3. 启动服务
```bash
# 开发环境
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 或使用jar包
java -jar target/finance-service.jar --spring.profiles.active=dev
```

### 4. Docker部署
```bash
# 构建镜像
docker build -t finance-service:1.0.0 .

# 运行容器
docker run -d \
  --name finance-service \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=mysql \
  -e REDIS_HOST=redis \
  -e CONSUL_HOST=consul \
  finance-service:1.0.0
```

## 🧪 测试接口

### 1. 测试数据同步（内部接口）
```bash
curl -X POST http://localhost:8082/internal/sync/recharges \
  -H "Content-Type: application/json" \
  -d '{
    "batchId": "BATCH-20260102-001",
    "sourceService": "audience-service",
    "batchTime": 1735862400000,
    "recharges": [{
      "rechargeId": 1,
      "traceId": "TRACE-001",
      "anchorId": 1,
      "anchorName": "主播A",
      "audienceId": 101,
      "audienceName": "观众A",
      "rechargeAmount": 100.00,
      "rechargeTime": 1735862400000,
      "rechargeType": 0,
      "liveRoomId": 1
    }],
    "totalAmount": 100.00,
    "totalCount": 1
  }'
```

### 2. 查询主播余额
```bash
curl http://localhost:8082/api/v1/settlement/1/balance
```

### 3. 查询当前分成比例
```bash
curl http://localhost:8082/api/v1/commission/1/current
```

### 4. 申请提现（幂等性测试）
```bash
curl -X POST http://localhost:8082/api/v1/withdrawal \
  -H "Content-Type: application/json" \
  -d '{
    "anchorId": 1,
    "anchorName": "主播A",
    "amount": 50.00,
    "withdrawalType": 0,
    "accountNumber": "6222021234567890123",
    "bankName": "中国工商银行",
    "accountHolder": "张三",
    "traceId": "WITHDRAW-001",
    "remark": "测试提现"
  }'

# 重复提交相同traceId，验证幂等性
curl -X POST http://localhost:8082/api/v1/withdrawal \
  -H "Content-Type: application/json" \
  -d '{
    "anchorId": 1,
    "anchorName": "主播A",
    "amount": 50.00,
    "withdrawalType": 0,
    "accountNumber": "6222021234567890123",
    "bankName": "中国工商银行",
    "accountHolder": "张三",
    "traceId": "WITHDRAW-001",
    "remark": "测试提现"
  }'
# 应返回相同结果，不会重复扣款
```

### 5. 查询提现记录
```bash
curl "http://localhost:8082/api/v1/withdrawal/1?page=1&size=10"
```

## 📊 Redis缓存验证

### 连接Redis查看缓存
```bash
redis-cli -h localhost -p 6379

# 查看所有finance相关的key
KEYS finance:*

# 查看批次缓存
GET finance:batch:BATCH-20260102-001

# 查看余额缓存
GET finance:balance:1

# 查看分成比例缓存
GET finance:commission:1

# 查看提现记录缓存
GET finance:withdrawal:trace:WITHDRAW-001
```

## 🔍 幂等性验证

### 验证打赏同步幂等性
```bash
# 1. 第一次提交
curl -X POST http://localhost:8082/internal/sync/recharges -d '...'
# 返回：{"code":0,"message":"数据接收成功"}

# 2. 重复提交相同batchId
curl -X POST http://localhost:8082/internal/sync/recharges -d '...'
# 返回：{"code":0,"message":"数据接收成功"}
# 但不会重复处理，检查数据库和Redis缓存验证

# 3. 验证Redis缓存
redis-cli
GET finance:batch:BATCH-20260102-001
# 应该返回 "processed"
```

### 验证提现幂等性
```bash
# 1. 查询初始余额
curl http://localhost:8082/api/v1/settlement/1/balance
# 记录 availableAmount

# 2. 提交提现申请
curl -X POST http://localhost:8082/api/v1/withdrawal -d '{"traceId":"TEST-001",...}'
# 返回提现记录

# 3. 重复提交相同traceId
curl -X POST http://localhost:8082/api/v1/withdrawal -d '{"traceId":"TEST-001",...}'
# 应返回相同的提现记录，withdrawalId相同

# 4. 验证余额
curl http://localhost:8082/api/v1/settlement/1/balance
# availableAmount 应该只扣减一次
```

## 🔐 分布式锁验证

### 验证提现分布式锁
```bash
# 使用多个终端并发提交不同traceId的提现请求
# Terminal 1
curl -X POST http://localhost:8082/api/v1/withdrawal \
  -d '{"traceId":"CONCURRENT-001", "anchorId":1, "amount":10.00, ...}'

# Terminal 2 (同时执行)
curl -X POST http://localhost:8082/api/v1/withdrawal \
  -d '{"traceId":"CONCURRENT-002", "anchorId":1, "amount":10.00, ...}'

# 验证：只有一个请求能立即处理，另一个会等待或失败
# 检查日志和数据库，余额扣减应该正确
```

## 📝 日志查看

```bash
# 查看实时日志
tail -f logs/finance-service.log

# 查看错误日志
tail -f logs/finance-service-error.log

# 搜索特定traceId的日志
grep "TRACE-001" logs/finance-service.log
```

## ⚠️ 注意事项

1. **幂等性保证** - 所有关键接口都实现了幂等性
2. **分布式锁** - 提现操作使用Redis分布式锁
3. **缓存一致性** - 更新时自动清除相关缓存
4. **事务管理** - 关键操作使用@Transactional
5. **降级处理** - Feign调用失败时有降级逻辑

## 🎯 下一步

1. 启动 audience-service 测试数据同步
2. 启动 anchor-service 测试提现流程
3. 启动 data-analysis-service 完成数据分析集成
4. 进行压力测试验证高并发场景
5. 监控Redis缓存命中率和性能

## 📞 问题排查

### 服务无法启动
- 检查MySQL、Redis、Consul是否启动
- 检查端口8082是否被占用
- 查看启动日志

### 幂等性失效
- 检查Redis连接是否正常
- 查看Redis缓存是否设置成功
- 检查数据库唯一约束

### 缓存不生效
- 检查Redis配置
- 查看@Cacheable注解是否生效
- 验证RedisTemplate是否正常

---

**Finance Service 已完整实现！** 🎉
