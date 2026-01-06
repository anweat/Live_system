# MyBatis 数据访问层集成指南

## 概述

本项目使用 MyBatis 框架进行数据库操作，采用**注解方式 + XML 补充**的模式，支持单条和批量操作。

## 项目结构

```
services/
├── common/
│   ├── src/main/java/
│   │   └── common/
│   │       ├── bean/              # 数据模型类
│   │       │   ├── Recharge.java
│   │       │   ├── CommissionRate.java
│   │       │   ├── Withdrawal.java
│   │       │   ├── user/
│   │       │   │   ├── User.java
│   │       │   │   ├── Anchor.java
│   │       │   │   └── Audience.java
│   │       │   └── liveroom/
│   │       │       ├── LiveRoom.java
│   │       │       ├── LiveRoomRealtime.java
│   │       │       ├── LiveSessionAudience.java
│   │       │       └── Message.java
│   │       ├── mapper/            # MyBatis Mapper接口
│   │       │   ├── AudienceMapper.java
│   │       │   ├── LiveRoomMapper.java
│   │       │   ├── RechargeMapper.java
│   │       │   ├── UserMapper.java
│   │       │   ├── AnchorMapper.java
│   │       │   ├── CommissionRateMapper.java
│   │       │   └── WithdrawalMapper.java
│   │       └── service/           # 业务逻辑层
│   └── src/main/resources/
│       └── mybatis/
│           └── mapper-config.xml  # MyBatis配置文件
└── db-service/
    └── sql/
        ├── 01-init-db1-audience-service.sql
        └── 02-init-db2-finance-service.sql
```

## MyBatis Mapper 接口说明

### 1. AudienceMapper - 观众管理

**关键特性**：

- ✅ 单条和批量插入
- ✅ 按消费等级查询（高/中/低消费）
- ✅ 批量更新消费等级
- ✅ 累计打赏金额统计

**核心方法**：

```java
// 单条插入
int insert(Audience audience);

// 批量插入 - 支持大量数据快速写入
int batchInsert(List<Audience> audiences);

// 按消费等级查询
List<Audience> selectByConsumptionLevel(Integer consumptionLevel);

// 更新消费等级
int updateConsumptionLevel(Long userId, Integer consumptionLevel);

// 增加打赏金额（用于实时更新）
int incrementRechargeAmount(Long userId, BigDecimal amount);

// 批量增加打赏金额
int batchIncrementRechargeAmount(List<Map<String, Object>> amounts);
```

**使用示例**：

```java
@Service
public class AudienceService {
    @Autowired
    private AudienceMapper audienceMapper;

    // 批量创建观众
    public void batchCreateAudiences(List<Audience> audiences) {
        int count = audienceMapper.batchInsert(audiences);
        log.info("批量创建观众成功，共{}条", count);
    }

    // 更新观众消费等级
    public void updateConsumptionLevels(List<Long> userIds, Integer level) {
        for (Long userId : userIds) {
            audienceMapper.updateConsumptionLevel(userId, level);
        }
    }

    // 实时更新观众打赏金额
    public void addRechargeAmount(Long audienceId, BigDecimal amount) {
        audienceMapper.incrementRechargeAmount(audienceId, amount);
    }
}
```

---

### 2. LiveRoomMapper - 直播间管理

**关键特性**：

- ✅ 单条和批量插入（模拟服务初始化）
- ✅ 批量更新状态（开播、关播、封禁）
- ✅ 实时更新收益和观众数
- ✅ 排行榜查询（按收益、观众数）

**核心方法**：

```java
// 单条插入
int insert(LiveRoom liveRoom);

// 批量插入 - 模拟服务初始化大量直播间
int batchInsert(List<LiveRoom> liveRooms);

// 批量更新状态
int batchUpdateStatus(List<Long> liveRoomIds, Integer status);

// 增加直播间收益
int incrementEarnings(Long liveRoomId, BigDecimal amount);

// 批量增加收益
int batchIncrementEarnings(List<Map<String, Object>> earnings);

// 查询排行榜
List<Map<String, Object>> selectTopByEarnings(int limit);
List<Map<String, Object>> selectTopByViewersCount(int limit);
```

**使用示例**：

```java
@Service
public class LiveRoomService {
    @Autowired
    private LiveRoomMapper liveRoomMapper;

    // 模拟服务：批量创建直播间
    public void mockInitializeLiveRooms(List<LiveRoom> liveRooms) {
        int batchSize = 100;
        for (int i = 0; i < liveRooms.size(); i += batchSize) {
            List<LiveRoom> batch = liveRooms.subList(i,
                Math.min(i + batchSize, liveRooms.size()));
            liveRoomMapper.batchInsert(batch);
        }
        log.info("批量创建直播间成功，共{}个", liveRooms.size());
    }

    // 批量更新直播间状态（开播）
    public void startBroadcasts(List<Long> liveRoomIds) {
        liveRoomMapper.batchUpdateStatus(liveRoomIds, 1);
    }

    // 实时更新直播间收益
    public void updateRoomEarnings(Long liveRoomId, BigDecimal earnings) {
        liveRoomMapper.incrementEarnings(liveRoomId, earnings);
    }

    // 获取收益排行TOP 10
    public List<Map<String, Object>> getTopRoomsByEarnings() {
        return liveRoomMapper.selectTopByEarnings(10);
    }
}
```

---

### 3. RechargeMapper - 打赏记录（核心业务）

**关键特性**：

- ✅ 幂等性检查（通过 traceId）
- ✅ 单条和批量插入（高并发支持）
- ✅ 多维度查询（主播、观众、直播间、时间）
- ✅ 分析查询（小时统计、分位数计算）

**核心方法**：

```java
// 单条插入打赏记录
int insert(Recharge recharge);

// 批量插入 - 模拟服务高并发
int batchInsert(List<Recharge> recharges);

// 幂等性检查（防止重复打赏）
Recharge selectByTraceId(String traceId);

// 按主播查询TOP 10打赏观众
List<Map<String, Object>> selectTop10PayersByAnchor(Long anchorId);

// 按小时统计打赏数据
List<Map<String, Object>> selectHourlyStatistics(LocalDateTime start, LocalDateTime end);

// 统计未结算金额
BigDecimal sumUnsettledAmount();
```

**使用示例**：

```java
@Service
public class RechargeService {
    @Autowired
    private RechargeMapper rechargeMapper;

    // 单条打赏（带幂等性检查）
    @Transactional
    public void recharge(Recharge recharge) {
        // 检查是否已处理过该traceId
        Recharge existing = rechargeMapper.selectByTraceId(recharge.getTraceId());
        if (existing != null) {
            throw new DuplicateRechargeException("打赏已处理");
        }

        rechargeMapper.insert(recharge);
        // 更新观众打赏金额、主播收益等
    }

    // 模拟服务：批量插入打赏记录
    public void mockBatchRecharge(List<Recharge> recharges) {
        int batchSize = 500;
        for (int i = 0; i < recharges.size(); i += batchSize) {
            List<Recharge> batch = recharges.subList(i,
                Math.min(i + batchSize, recharges.size()));
            rechargeMapper.batchInsert(batch);
        }
    }

    // 查询主播TOP 10打赏观众
    public List<Map<String, Object>> getTop10Payers(Long anchorId) {
        return rechargeMapper.selectTop10PayersByAnchor(anchorId);
    }

    // 获取小时统计数据
    public List<Map<String, Object>> getHourlyStats(LocalDateTime start, LocalDateTime end) {
        return rechargeMapper.selectHourlyStatistics(start, end);
    }
}
```

---

### 4. UserMapper - 用户基础管理

**核心方法**：

```java
User selectByUserId(Long userId);
User selectByUsername(String username);
User selectByEmail(String email);

int insert(User user);
int batchInsert(List<User> users);

int update(User user);
int batchUpdate(List<User> users);

int updateAccountStatus(Long userId, Integer status);
```

---

### 5. AnchorMapper - 主播管理

**核心方法**：

```java
Anchor selectByUserId(Long userId);
Anchor selectByLiveRoomId(Long liveRoomId);

List<Anchor> selectTopByFanCount(int limit);
List<Anchor> selectTopByEarnings(int limit);

int insert(Anchor anchor);
int batchInsert(List<Anchor> anchors);

int updateAvailableAmount(Long userId, BigDecimal amount);
int incrementEarnings(Long userId, BigDecimal amount);
int incrementFanCount(Long userId, long count);
```

---

### 6. CommissionRateMapper - 分成比例管理

**关键特性**：

- ✅ 历史版本追踪
- ✅ 时间点查询（支持计算历史分成）
- ✅ 状态管理（启用、过期）

**核心方法**：

```java
// 查询当前有效的分成比例
CommissionRate selectCurrentRate(Long anchorId);

// 查询某时间点的分成比例（用于结算计算）
CommissionRate selectRateByTime(Long anchorId, LocalDateTime time);

// 查询历史版本
List<CommissionRate> selectHistoryByAnchor(Long anchorId);

int insert(CommissionRate rate);
int updateStatus(Long id, Integer status);
```

**使用示例**：

```java
@Service
public class SettlementService {
    @Autowired
    private CommissionRateMapper rateMapper;

    // 计算结算金额（支持分成比例变化）
    public BigDecimal calculateSettlement(Long anchorId,
                                         LocalDateTime periodStart,
                                         LocalDateTime periodEnd) {
        // 获取该期间内所有打赏记录，按时间查询对应的分成比例
        // 支持分成比例在期间内多次变化的情况
        BigDecimal total = BigDecimal.ZERO;

        // 示例逻辑
        CommissionRate rate = rateMapper.selectRateByTime(anchorId, periodStart);
        // ... 计算逻辑 ...

        return total;
    }
}
```

---

### 7. WithdrawalMapper - 提现管理

**核心方法**：

```java
Withdrawal selectByTraceId(String traceId);
List<Withdrawal> selectByAnchorId(Long anchorId);
List<Withdrawal> selectPendingWithdrawals();

int insert(Withdrawal withdrawal);
int batchInsert(List<Withdrawal> withdrawals);

int updateStatus(Long withdrawalId, Integer status);
int reject(Long withdrawalId, String reason);
int confirmTransfer(Long withdrawalId, String serialNumber);
```

---

## 使用规范

### 1. MyBatis 配置（Spring Boot）

**application.yml**：

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/live_audience_db?serverTimezone=UTC&useSSL=false
    username: root
    password: root
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 600000
      max-lifetime: 1800000

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false

mybatis:
  mapper-locations: classpath:mybatis/*.xml
  type-aliases-package: common.bean
  configuration:
    jdbc-type-for-null: NULL
    map-underscore-to-camel-case: true
    cache-enabled: true
    lazy-loading-enabled: true
```

**pom.xml**：

```xml
<!-- MyBatis Spring Boot Starter -->
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- MySQL Driver -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.28</version>
</dependency>

<!-- HikariCP Connection Pool -->
<dependency>
    <groupId>com.zaxxer</groupId>
    <artifactId>HikariCP</artifactId>
    <version>4.0.3</version>
</dependency>
```

---

### 2. 扫描注解

**启动类**：

```java
@SpringBootApplication
@MapperScan(basePackages = "common.mapper")
public class YourApplication {
    public static void main(String[] args) {
        SpringApplication.run(YourApplication.class, args);
    }
}
```

---

### 3. 批量写入最佳实践

**高效的批量操作**：

```java
@Service
@Transactional
public class BatchOperationService {
    @Autowired
    private AudienceMapper audienceMapper;
    @Autowired
    private LiveRoomMapper liveRoomMapper;
    @Autowired
    private RechargeMapper rechargeMapper;

    // 分批插入，避免一次性插入过多数据
    public void batchInsertWithLimit(List<Audience> audiences) {
        int batchSize = 500; // 每批500条
        for (int i = 0; i < audiences.size(); i += batchSize) {
            int end = Math.min(i + batchSize, audiences.size());
            List<Audience> batch = audiences.subList(i, end);

            audienceMapper.batchInsert(batch);

            log.info("已插入第 {} 至 {} 条记录", i + 1, end);
        }
    }

    // 模拟服务：并发批量插入
    @Async
    public void asyncBatchInsert(List<Recharge> recharges) {
        int batchSize = 1000;
        for (int i = 0; i < recharges.size(); i += batchSize) {
            int end = Math.min(i + batchSize, recharges.size());
            List<Recharge> batch = recharges.subList(i, end);

            rechargeMapper.batchInsert(batch);
        }
    }
}
```

---

### 4. 事务管理

```java
@Service
@Transactional
public class RechargeTransactionService {
    @Autowired
    private RechargeMapper rechargeMapper;
    @Autowired
    private AudienceMapper audienceMapper;
    @Autowired
    private LiveRoomMapper liveRoomMapper;

    // 原子操作：打赏+更新余额
    public void rechargeWithUpdate(Recharge recharge,
                                  Audience audience,
                                  LiveRoom liveRoom) {
        // 插入打赏记录
        rechargeMapper.insert(recharge);

        // 更新观众打赏金额
        audienceMapper.incrementRechargeAmount(
            recharge.getAudienceId(),
            recharge.getRechargeAmount()
        );

        // 更新直播间收益
        liveRoomMapper.incrementEarnings(
            recharge.getLiveRoomId(),
            recharge.getRechargeAmount()
        );

        // 如果任何操作失败，事务自动回滚
    }
}
```

---

## 性能优化建议

### 1. 索引利用

- 所有 Mapper 查询都充分利用了设计的索引
- 避免全表扫描，使用 LIMIT 限制结果集

### 2. 查询优化

- 使用分页查询大数据集
- 避免 N+1 查询问题
- 使用 GROUP BY 进行聚合查询

### 3. 缓存

```java
@Service
@CacheConfig(cacheNames = "commissionRates")
public class CommissionRateService {
    @Autowired
    private CommissionRateMapper mapper;

    @Cacheable(key = "#anchorId")
    public CommissionRate getCurrentRate(Long anchorId) {
        return mapper.selectCurrentRate(anchorId);
    }

    @CacheEvict(key = "#anchorId")
    public void updateRate(Long anchorId, CommissionRate rate) {
        mapper.insert(rate);
    }
}
```

---

## 常见问题

### Q1: 如何处理重复打赏？

**A**: 使用 traceId 的 UNIQUE 约束 + 幂等性检查

```java
Recharge existing = rechargeMapper.selectByTraceId(traceId);
if (existing != null) {
    return existing; // 返回已存在的记录
}
```

### Q2: 如何处理大数据量的批量操作？

**A**: 分批处理 + 异步 + 事务管理

```java
@Async
@Transactional
public void asyncBatchInsert(List<Recharge> data) {
    final int BATCH_SIZE = 1000;
    for (int i = 0; i < data.size(); i += BATCH_SIZE) {
        rechargeMapper.batchInsert(
            data.subList(i, Math.min(i + BATCH_SIZE, data.size()))
        );
    }
}
```

### Q3: 如何支持分成比例的历史追踪？

**A**: CommissionRateMapper 支持时间点查询

```java
CommissionRate rate = mapper.selectRateByTime(anchorId, LocalDateTime.now());
```

---

## 总结

本项目的 MyBatis 集成提供：

- ✅ **完整的 CRUD 操作**：所有表的查询、插入、更新
- ✅ **批量操作支持**：高效的批量写入
- ✅ **复杂查询**：多维度、聚合、排行榜等
- ✅ **幂等性保证**：traceId 防止重复
- ✅ **事务管理**：确保数据一致性
- ✅ **性能优化**：充分利用索引和缓存

更多详情参考具体的 Mapper 接口文档或 SQL 脚本。
