# Mock Service 大规模测试指南

## 🎯 支持的测试规模

✅ **100+ 直播间**  
✅ **300K+ 并发观众**  
✅ **基于标签的智能观众分配**  
✅ **数据库持久化Bot池**

---

## 📋 测试前准备

### 1. 数据库配置优化

在启动大规模测试前，需要优化MySQL配置：

```sql
-- 增加连接数
SET GLOBAL max_connections = 500;

-- 增加缓冲区
SET GLOBAL innodb_buffer_pool_size = 2147483648;  -- 2GB

-- 批量插入优化
SET GLOBAL innodb_flush_log_at_trx_commit = 2;
```

### 2. 初始化数据库表

```bash
mysql -u root -p live_audience_db < sql/init-mock-tables.sql
```

### 3. JVM参数优化（可选）

在 `start.bat` 或 `start.sh` 中添加：

```bash
java -Xms2g -Xmx4g -XX:+UseG1GC -jar target/mock-service-1.0.0.jar
```

---

## 🚀 测试场景

### 场景1: 预创建Bot池（推荐）

**第一步：批量创建Bot并保存到数据库**

```bash
# 创建10万个Bot
curl -X POST "http://localhost:8090/mock/api/audience/batch-create-and-persist" \
  -H "Content-Type: application/json" \
  -d '{
    "count": 100000,
    "assignRandomTags": true,
    "assignConsumptionLevel": true,
    "malePercentage": 50,
    "minAge": 18,
    "maxAge": 45
  }'
```

**第二步：批量创建直播间并从Bot池分配观众**

```bash
# 100个直播间，每间3000观众 = 30万观众
curl -X POST "http://localhost:8090/mock/api/batch/create-live-rooms" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomCount": 100,
    "audiencePerRoom": 3000,
    "preCreateBots": 0,
    "persistData": true,
    "useTagMatching": true,
    "durationSeconds": 600,
    "simulateBehavior": true,
    "rechargeProbability": 15
  }'
```

---

### 场景2: 一键超大规模测试

直接运行最高规模测试（自动预创建Bot）：

```bash
# 150个直播间 × 2000观众 = 30万观众
curl -X POST "http://localhost:8090/mock/api/quick/massive-test?liveRoomCount=150&audiencePerRoom=2000&preCreateBots=100000"
```

---

### 场景3: 渐进式压力测试

逐步增加负载：

```bash
# 阶段1: 10个直播间，每间1000观众
curl -X POST "http://localhost:8090/mock/api/quick/massive-test?liveRoomCount=10&audiencePerRoom=1000&preCreateBots=10000"

# 阶段2: 50个直播间，每间2000观众
curl -X POST "http://localhost:8090/mock/api/quick/massive-test?liveRoomCount=50&audiencePerRoom=2000&preCreateBots=50000"

# 阶段3: 100个直播间，每间3000观众
curl -X POST "http://localhost:8090/mock/api/quick/massive-test?liveRoomCount=100&audiencePerRoom=3000&preCreateBots=100000"
```

---

## 📊 性能监控

### 查看Bot统计

```bash
curl "http://localhost:8090/mock/api/audience/count-bots"
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "totalBots": 100000,
    "levelStatistics": [
      [0, 60000],  // 低消费：6万
      [1, 30000],  // 中消费：3万
      [2, 10000]   // 高消费：1万
    ]
  }
}
```

### 查询批量测试任务状态

```bash
curl "http://localhost:8090/mock/api/batch/task-status/{taskId}"
```

响应示例：
```json
{
  "code": 200,
  "data": {
    "taskId": "uuid-xxx",
    "status": "RUNNING",
    "totalLiveRooms": 100,
    "createdLiveRooms": 100,
    "totalAudiences": 300000,
    "assignedAudiences": 285000,
    "startTime": "2026-01-02T10:00:00"
  }
}
```

---

## 🎨 标签智能匹配

### 基于标签筛选Bot

```bash
# 查询"游戏"标签的Bot
curl "http://localhost:8090/mock/api/audience/find-by-tag?tag=游戏&limit=5000"
```

### 智能分配观众到直播间

```bash
curl -X POST "http://localhost:8090/mock/api/batch/smart-assign" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 123456,
    "liveRoomTags": ["游戏", "电竞", "娱乐"],
    "targetCount": 5000,
    "matchStrategy": "any",
    "usePersistedBots": true
  }'
```

**匹配策略说明：**
- `exact`: 精确匹配所有标签
- `any`: 匹配任意标签（推荐）
- `random`: 随机分配

---

## ⚙️ 配置调优

### application.yml 高并发配置

```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 100      # 数据库连接池
      minimum-idle: 20
  
  task:
    execution:
      pool:
        core-size: 50             # 线程池核心大小
        max-size: 200             # 线程池最大大小
        queue-capacity: 10000     # 队列容量

mock:
  bot:
    max-batch-size: 5000          # 单次最大Bot创建数
  
  batch:
    executor-pool-size: 50        # 批量处理线程池
    max-live-rooms-per-batch: 500 # 单次最大直播间数
    max-audience-per-room: 50000  # 单个直播间最大观众数
```

---

## 📈 性能基准

### 硬件要求（300K观众测试）

- **CPU**: 8核+
- **内存**: 8GB+
- **数据库**: MySQL 5.7+ / 8.0+
- **磁盘**: SSD推荐

### 性能指标

| 指标 | 数值 |
|------|------|
| Bot创建速度 | ~2000个/秒 |
| 数据库批量插入 | ~5000条/秒 |
| 直播间创建速度 | ~100个/秒 |
| 观众分配速度 | ~10000个/秒 |
| 并发线程数 | 50-200 |
| 内存占用 | 4-6GB（30万观众） |

---

## 🐛 常见问题

### Q1: 数据库连接超时

**解决方案：**
1. 增加连接池大小：`maximum-pool-size: 200`
2. 增加MySQL max_connections：`SET GLOBAL max_connections = 500`

### Q2: 内存不足

**解决方案：**
1. 增加JVM堆内存：`-Xmx8g`
2. 分批创建Bot：每次10万，分多次执行
3. 使用数据库持久化而非内存存储

### Q3: 标签查询慢

**解决方案：**
1. 为tags字段创建索引
2. 使用MySQL全文索引（FULLTEXT）
3. 考虑使用Elasticsearch做标签搜索

### Q4: 批量测试任务阻塞

**解决方案：**
1. 减少每批次的直播间数量
2. 增加线程池大小
3. 分阶段执行测试

---

## 🔧 高级用法

### 自定义观众分布

```bash
curl -X POST "http://localhost:8090/mock/api/audience/batch-create-and-persist" \
  -H "Content-Type: application/json" \
  -d '{
    "count": 50000,
    "assignRandomTags": true,
    "assignConsumptionLevel": true,
    "malePercentage": 70,          # 70%男性
    "minAge": 25,                  # 年龄25-35
    "maxAge": 35
  }'
```

### 模拟不同消费等级的直播间

```bash
# 高消费直播间（娱乐类）
curl -X POST "http://localhost:8090/mock/api/batch/smart-assign" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 1,
    "liveRoomTags": ["颜值", "才艺"],
    "targetCount": 3000,
    "lowConsumptionRate": 20,     # 调整消费分布
    "mediumConsumptionRate": 30,
    "highConsumptionRate": 50
  }'
```

---

## 📞 技术支持

遇到问题？查看：
- 日志文件：`logs/mock-service.log`
- API文档：http://localhost:8090/mock/swagger-ui.html
- 详细文档：`services/mock-service/README.md`
