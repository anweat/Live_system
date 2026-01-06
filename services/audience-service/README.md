# 观众服务 (Audience Service) - 实现指南

## 📋 概述

观众服务是直播系统的核心服务之一，负责观众管理、打赏请求处理、数据同步等关键业务。

**服务端口**: 8082  
**上下文路径**: /audience  
**版本**: 1.0.0

---

## 🏗️ 项目结构

```
audience-service/
├── src/main/
│   ├── java/com/liveroom/audience/
│   │   ├── AudienceServiceApplication.java       # 启动类
│   │   ├── config/
│   │   │   ├── SchedulingConfig.java            # 定时任务配置
│   │   │   ├── FeignConfig.java                 # Feign客户端配置
│   │   │   └── WebConfig.java                   # Web配置
│   │   ├── controller/
│   │   │   ├── AudienceController.java          # 观众接口
│   │   │   └── RechargeController.java          # 打赏接口
│   │   ├── service/
│   │   │   ├── AudienceService.java             # 观众业务逻辑
│   │   │   ├── RechargeService.java             # 打赏业务逻辑
│   │   │   └── SyncService.java                 # 数据同步逻辑
│   │   ├── repository/
│   │   │   ├── AudienceRepository.java          # 观众数据访问
│   │   │   ├── RechargeRepository.java          # 打赏数据访问
│   │   │   ├── TagRepository.java               # 标签数据访问
│   │   │   └── SyncProgressRepository.java      # 同步进度数据访问
│   │   ├── dto/
│   │   │   ├── AudienceDTO.java                 # 观众传输对象
│   │   │   ├── RechargeDTO.java                 # 打赏传输对象
│   │   │   └── ConsumptionStatsDTO.java         # 消费统计传输对象
│   │   ├── vo/
│   │   │   ├── Top10AudienceVO.java             # TOP10观众视图
│   │   │   └── ProfileVO.java                   # 用户画像视图
│   │   ├── feign/
│   │   │   ├── FinanceServiceClient.java        # 财务服务客户端
│   │   │   └── FinanceServiceClientFallback.java # 降级处理
│   │   ├── task/
│   │   │   └── RechargeDataSyncTask.java        # 数据同步定时任务
│   │   ├── util/
│   │   │   └── CacheKeyUtil.java                # 缓存键工具
│   │   └── handler/
│   │       └── GlobalExceptionHandler.java      # 全局异常处理
│   └── resources/
│       ├── application.yml                      # 主配置文件
│       ├── application-dev.yml                  # 开发环境配置
│       ├── application-production.yml           # 生产环境配置
│       └── logback-spring.xml                   # 日志配置
└── pom.xml                                      # Maven项目配置
```

---

## 🚀 快速开始

### 前置条件

- Java 11+
- MySQL 5.7+
- Redis 5.0+ (生产环境)
- Maven 3.6+

### 开发环境启动

#### 1. 修改数据库连接

编辑 `application-dev.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/db1
    username: root
    password: root
```

#### 2. 启动应用

```bash
# 使用Maven启动
mvn clean install
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 或使用IDE启动 AudienceServiceApplication
```

#### 3. 验证服务启动

```bash
# 检查服务是否正常启动
curl http://localhost:8082/audience/api/v1/audiences

# 应返回空列表或错误信息（取决于数据库状态）
```

---

## 📚 核心API接口

### 观众管理接口

#### 创建观众

```http
POST /audience/api/v1/audiences
Content-Type: application/json

{
  "nickname": "张三",
  "realName": "张三",
  "gender": 1,
  "birthDate": "1995-01-01",
  "signature": "我是观众"
}
```

#### 查询观众信息

```http
GET /audience/api/v1/audiences/{audienceId}
```

#### 修改观众信息

```http
PUT /audience/api/v1/audiences/{audienceId}
Content-Type: application/json

{
  "nickname": "新昵称",
  "signature": "新签名"
}
```

#### 查询观众列表

```http
GET /audience/api/v1/audiences?page=1&size=20&consumptionLevel=2
```

#### 搜索观众

```http
GET /audience/api/v1/audiences/search?keyword=张三&page=1&size=20
```

#### 获取消费统计

```http
GET /audience/api/v1/audiences/{audienceId}/consumption-stats
```

### 打赏接口

#### 创建打赏记录

```http
POST /audience/api/v1/recharge
Content-Type: application/json

{
  "liveRoomId": 1,
  "anchorId": 1,
  "anchorName": "主播A",
  "audienceId": 123,
  "audienceNickname": "观众B",
  "rechargeAmount": 100.00,
  "rechargeType": 0,
  "message": "加油！",
  "traceId": "trace_xxx"
}
```

#### 查询TOP10打赏观众

```http
GET /audience/api/v1/recharge/top10?anchorId=1&period=day
```

参数:
- `period`: day(日) | week(周) | month(月) | all(总)

---

## 🔧 Common模块集成

观众服务集成了Common模块，获得以下功能：

### 1. 异常处理

```java
@PostMapping
public BaseResponse<AudienceDTO> createAudience(@RequestBody AudienceDTO dto) {
    // 参数验证失败时自动抛出
    if (dto.getNickname() == null) {
        throw new ValidationException("昵称不能为空");
    }
    
    // 业务异常
    if (audienceRepository.findByNickname(dto.getNickname()).isPresent()) {
        throw new BusinessException(ErrorConstants.USER_ALREADY_EXISTS, "昵称已存在");
    }
    
    // 自动转换为 BaseResponse JSON 响应
    return ResponseUtil.success(audienceDTO, "创建成功");
}
```

### 2. 日志追踪

```java
@Service
public class AudienceService {
    
    public void createAudience(AudienceDTO dto) {
        // 自动包含 traceId 的日志
        TraceLogger.info("AudienceService", "createAudience", "开始创建观众");
        
        // 业务逻辑...
        
        TraceLogger.info("AudienceService", "createAudience", "观众创建成功: " + audience.getUserId());
    }
}
```

日志输出示例:
```
[2026-01-02 10:30:45] [INFO] [trace-xxx] AudienceService - 观众创建成功: 123456
```

### 3. 防重复提交

```java
@PostMapping
@Idempotent(key = "#audienceDTO.nickname", timeout = 30)
public BaseResponse<AudienceDTO> createAudience(@RequestBody AudienceDTO audienceDTO) {
    // 框架自动检查：同一昵称 30 秒内的重复请求会被拒绝
    return ResponseUtil.success(audienceService.createAudience(audienceDTO));
}
```

### 4. 参数验证

```java
@PostMapping
@ValidateParam
public BaseResponse<AudienceDTO> createAudience(@Valid @RequestBody AudienceDTO audienceDTO) {
    // 框架自动验证标准注解：@NotNull, @Email, @Min 等
    // 验证失败自动抛出 ValidationException
    return ResponseUtil.success(audienceService.createAudience(audienceDTO));
}
```

### 5. 工具类使用

```java
@Service
public class AudienceService {
    
    public AudienceDTO createAudience(AudienceDTO dto) {
        // 生成唯一ID
        Long userId = IdGeneratorUtil.nextId();
        
        // 时间操作
        LocalDateTime now = DateTimeUtil.now();
        
        // 金额操作
        BigDecimal amount = MoneyUtil.dollarsToCents(new BigDecimal("100.50"));
        
        // 对象转换
        Audience audience = BeanUtil.convert(dto, Audience.class);
        
        // 生成TraceId
        String traceId = TraceIdGenerator.generate();
        
        return BeanUtil.convert(audience, AudienceDTO.class);
    }
}
```

---

## 📊 数据库设计

### 关键表结构

#### audience 表

```sql
CREATE TABLE audience (
    user_id BIGINT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL UNIQUE,
    consumption_level INT DEFAULT 0,
    total_recharge_amount DECIMAL(15,2) DEFAULT 0,
    total_recharge_count BIGINT DEFAULT 0,
    last_recharge_time DATETIME,
    vip_level INT DEFAULT 0,
    status INT DEFAULT 0,
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    INDEX idx_consumption_level (consumption_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

#### recharge 表

```sql
CREATE TABLE recharge (
    recharge_id BIGINT PRIMARY KEY,
    live_room_id BIGINT NOT NULL,
    anchor_id BIGINT NOT NULL,
    audience_id BIGINT NOT NULL,
    recharge_amount DECIMAL(15,2) NOT NULL,
    recharge_time DATETIME NOT NULL,
    trace_id VARCHAR(64) UNIQUE,
    status INT DEFAULT 0,
    create_time DATETIME NOT NULL,
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_audience_id (audience_id),
    INDEX idx_trace_id (trace_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

---

## 🔄 业务流程

### 打赏流程

```
1. 客户端发送打赏请求 (携带 traceId)
        ↓
2. RechargeController 接收请求
        ↓
3. @ValidateParam 自动验证参数
        ↓
4. @Idempotent 检查幂等性
        ↓
5. RechargeService.createRecharge() 处理业务逻辑
        ├─ 验证参数
        ├─ 检查 traceId 重复
        ├─ 创建 Recharge 记录 (status=0: 已入账)
        ├─ 异步更新观众消费统计
        └─ 返回成功响应 (<500ms)
        ↓
6. 后台数据同步任务
        ├─ 每5分钟执行一次
        ├─ 查询未同步的打赏记录
        ├─ 调用FinanceServiceClient同步数据
        └─ 更新同步进度
```

### 消费等级更新

```
打赏金额   |  消费等级  |  粉丝等级
-----------|----------|----------
< 100      |  0(低)   |  0(普通)
100-1000   |  1(中)   |  1(铁粉) - 需3次以上
1000-5000  |  1(中)   |  2(银粉) - 需10次以上
5000-10000 |  2(高)   |  3(金粉) - 需50次以上
>= 10000   |  2(高)   |  4(超级) - 需100次以上
```

---

## 🔐 安全性设计

### 1. 幂等性控制

使用 `traceId` 防止重复打赏:

```java
// 重复请求检查
Optional<Recharge> existing = rechargeRepository.findByTraceId(traceId);
if (existing.isPresent()) {
    throw new BusinessException(ErrorConstants.DUPLICATE_RECHARGE, "请勿重复提交");
}
```

### 2. 参数验证

```java
@PostMapping
@ValidateParam
public BaseResponse<RechargeDTO> createRecharge(@Valid @RequestBody RechargeDTO dto) {
    // 自动验证：@NotNull, @DecimalMin 等
    // 金额必须在 0.01 - 999999.99 之间
    return ResponseUtil.success(rechargeService.createRecharge(dto));
}
```

### 3. 事务一致性

```java
@Service
public class RechargeService {
    
    @Transactional
    public RechargeDTO createRecharge(RechargeDTO dto) {
        // 创建打赏记录
        Recharge recharge = rechargeRepository.save(...);
        
        // 更新观众消费统计
        audienceService.updateConsumptionStats(...);
        
        // 如果任何操作失败，整个事务回滚
        return BeanUtil.convert(recharge, RechargeDTO.class);
    }
}
```

---

## 📈 性能优化

### 1. 缓存策略

```java
// 缓存观众信息
String cacheKey = CacheKeyUtil.getAudienceCacheKey(audienceId);
Audience cached = redisTemplate.opsForValue().get(cacheKey);
if (cached != null) {
    return BeanUtil.convert(cached, AudienceDTO.class);
}

// 查询数据库并缓存
Audience audience = audienceRepository.findById(audienceId).orElse(null);
if (audience != null) {
    redisTemplate.opsForValue().set(cacheKey, audience, Duration.ofHours(1));
}
```

### 2. 数据库索引

```sql
-- 打赏记录查询优化
CREATE INDEX idx_anchor_id ON recharge(anchor_id);
CREATE INDEX idx_audience_id ON recharge(audience_id);
CREATE INDEX idx_recharge_time ON recharge(recharge_time);
CREATE UNIQUE INDEX idx_trace_id ON recharge(trace_id);
```

### 3. 定时任务优化

- 使用分布式锁防止多节点重复执行
- 支持断点续传，避免重复同步
- 异步处理，不阻塞主业务流程

---

## 🐛 常见问题

### Q: 启动报错 "Cannot find class com.liveroom.common"?

**A**: 确保Common模块已编译并在依赖中：
```bash
cd ../common
mvn clean install
cd ../audience-service
mvn clean install
```

### Q: 打赏请求超时?

**A**: 检查以下几点：
1. 数据库连接是否正常
2. Redis连接是否正常（如果启用）
3. 观众ID和主播ID是否有效
4. 增加Feign超时时间

### Q: 数据同步失败?

**A**: 检查日志输出：
```bash
tail -f logs/audience-service/audience-service.log
```

查看是否是财务服务不可用，检查Feign降级处理。

---

## 📝 环境变量

| 环境变量 | 说明 | 默认值 |
|---------|------|--------|
| DB_HOST | 数据库主机 | localhost |
| DB_PORT | 数据库端口 | 3306 |
| DB_NAME | 数据库名称 | live_system |
| DB_USER | 数据库用户 | root |
| DB_PASS | 数据库密码 | 123456 |
| REDIS_ENABLED | Redis是否启用 | false |
| REDIS_HOST | Redis主机 | localhost |
| REDIS_PORT | Redis端口 | 6379 |
| REDIS_PASS | Redis密码 | (空) |

---

## 🔗 相关文档

- [观众服务设计文档](docs/设计文档.md)
- [Common模块使用指南](../../common/docs/使用指南.md)
- [Common模块快速查询表](../../common/docs/快速查询表.md)
- [系统整体设计](../../requirements/系统设计文档.md)

---

**版本**: 1.0.0  
**最后更新**: 2026-01-02  
**维护者**: Team
