# data-analysis-service 重构报告

**重构日期**: 2026-01-06  
**重构版本**: 2.0.0  
**重构范围**: 完全重构，遵循common模块标准架构

---

## 一、重构概述

### 1.1 重构目标
根据`数据分析模块设计文档`的规范，彻底重构data-analysis-service模块，使其：

1. **完全使用DataAccessFacade进行数据访问** - 禁止直接使用JdbcTemplate或Repository
2. **集成common模块的异常和日志系统** - 使用BusinessException、ValidationException和TraceLogger
3. **统一缓存管理** - 使用Spring @Cacheable注解替代RedisTemplate直接操作
4. **遵循设计文档规范** - 实现文档中定义的所有分析维度和指标

### 1.2 重构内容

#### 删除的文件
- `StatisticsServiceImpl.java` (已替换)
- `AnchorAnalysisServiceImpl.java` (已替换)
- 其他直接使用JdbcTemplate的服务实现

#### 新增的文件
- `common/service/AnalysisQueryService.java` - 分析查询接口
- `analysis/service/impl/v2/` - 新的服务实现目录

#### 改进的文件
- `common/service/DataAccessFacade.java` - 新增analysisQuery()方法

---

## 二、架构设计

### 2.1 分层架构

```
┌─────────────────────────────────────┐
│       REST API / Controller          │
│  (处理HTTP请求，参数验证)           │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│     Analysis Service Layer          │
│  (统计、分析、算法实现)             │
├──────────────────────────────────────┤
│ - StatisticsService                 │
│ - AnchorAnalysisService             │
│ - AudienceAnalysisService           │
│ - TagAnalysisService                │
│ - RankingService                    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│    DataAccessFacade (Facade)        │
│  (统一数据访问入口)                 │
├──────────────────────────────────────┤
│ - recharge()                        │
│ - audience()                        │
│ - anchor()                          │
│ - analysisQuery()  ◄── 新增         │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│  Business Service Layer             │
│  (common模块中的Service)             │
├──────────────────────────────────────┤
│ - RechargeService                   │
│ - AudienceService                   │
│ - AnchorService                     │
│ - AnalysisQueryService  ◄── 新增    │
└──────────────┬──────────────────────┘
               │
┌──────────────▼──────────────────────┐
│       Repository Layer              │
│  (直接数据库访问 - 禁止外部调用)    │
├──────────────────────────────────────┤
│ - RechargeRepository                │
│ - AudienceRepository                │
│ - AnchorRepository                  │
└─────────────────────────────────────┘
```

### 2.2 数据流向

**打赏数据分析流程**
```
原始打赏数据 (recharge 表)
    ↓
DataAccessFacade.analysisQuery().getRechargesByTimeRange()
    ↓
AnalysisQueryService (处理时间范围过滤和聚合)
    ↓
StatisticsService / AnchorAnalysisService (应用分析算法)
    ↓
VO对象 (返回给Controller)
    ↓
JSON响应 (前端展示)
```

---

## 三、关键改进

### 3.1 新增的AnalysisQueryService接口

在common模块中新增`AnalysisQueryService`，提供以下查询方法：

| 方法 | 功能 | 返回值 |
|------|------|--------|
| `getRechargesByTimeRange()` | 时间范围查询 | List<Recharge> |
| `getRechargesByAnchorAndTimeRange()` | 按主播+时间查询 | List<Recharge> |
| `getRechargesByAudienceAndTimeRange()` | 按观众+时间查询 | List<Recharge> |
| `getRechargeStatsByDay()` | 按天统计 | Map<String, RechargeStats> |
| `getRechargeStatsByHour()` | 按小时统计 | Map<String, RechargeStats> |
| `getAnchorRechargeStatsByDay()` | 主播按天统计 | Map<String, RechargeStats> |
| `getTopPayersByAnchor()` | TOP消费者 | List<TopPayerStats> |
| `getAudienceConsumptionStats()` | 观众消费统计 | AudienceConsumptionStats |

### 3.2 异常处理标准化

**异常分类**

| 异常类型 | 场景 | 处理方式 |
|---------|------|---------|
| ValidationException | 参数验证失败 | 400 Bad Request |
| BusinessException | 业务规则违反 | 500 Server Error |
| SystemException | 系统层异常 | 500 Server Error |

**使用示例**
```java
if (startDate == null || endDate == null) {
    throw new ValidationException("日期参数不能为空");
}

if (anchorId == null || anchorId <= 0) {
    throw new ValidationException("主播ID不合法");
}

// 查询失败
try {
    List<Recharge> recharges = dataAccessFacade.analysisQuery()
        .getRechargesByTimeRange(startTime, endTime);
} catch (Exception e) {
    TraceLogger.error("StatisticsService", "getGmvTrend", "查询失败", e);
    throw new BusinessException(500, "查询GMV趋势失败", e);
}
```

### 3.3 日志标准化

**日志规范**
```java
// 方法入口
TraceLogger.info("ServiceName", "methodName", "开始处理: 参数信息");

// 成功完成
TraceLogger.info("ServiceName", "methodName", "处理完成: 结果信息");

// 业务异常
TraceLogger.warn("ServiceName", "methodName", "业务异常: " + e.getMessage());

// 系统异常
TraceLogger.error("ServiceName", "methodName", "系统异常", e);
```

### 3.4 缓存策略

**缓存配置**
```java
@Cacheable(value = "analysis:gmv", key = "#startDate + ':' + #endDate", 
           cacheManager = "cacheManager")
public CashFlowTrendVO getGmvTrend(LocalDate startDate, LocalDate endDate) {
    // ...
}
```

**缓存TTL设置**
- 实时数据（TOP榜单）: 30分钟
- 统计数据（日均、周均）: 1小时
- 用户画像: 12小时
- 标签热力图: 7天

---

## 四、实现计划

### Phase 1: 基础设施 ✅
- [x] 创建AnalysisQueryService
- [x] 集成到DataAccessFacade
- [x] 定义异常和日志规范

### Phase 2: 核心分析服务
- [ ] 重构StatisticsServiceImpl
- [ ] 重构AnchorAnalysisServiceImpl
- [ ] 重构AudienceAnalysisServiceImpl
- [ ] 重构TagAnalysisServiceImpl
- [ ] 重构RankingServiceImpl

### Phase 3: 高级功能
- [ ] 重构ManualAnalysisService
- [ ] 重构AnalysisTaskService
- [ ] 实现异步分析框架

### Phase 4: 测试与验证
- [ ] 单元测试
- [ ] 集成测试
- [ ] 性能测试

---

## 五、性能优化考虑

### 5.1 查询优化
- 使用findByTimeRange()替代逐条查询
- 在内存中进行分组聚合（避免多次数据库查询）
- Redis缓存热点数据

### 5.2 资源管理
- 避免一次加载过多数据（使用分页）
- 及时清理缓存
- 监控内存使用

### 5.3 并发控制
- 使用@Transactional(readOnly=true)的只读事务
- 分布式锁保护关键操作
- 限流保护API

---

## 六、数据一致性保证

### 6.1 字段映射

| VO字段 | Entity字段 | 说明 |
|--------|----------|------|
| rechargeAmount | rechargeAmount | 打赏金额 |
| rechargeTime | rechargeTime | 打赏时间 |
| anchorId | anchorId | 主播ID |
| anchorName | anchorName | 主播名称 |
| audienceId | audienceId | 观众ID |
| audienceNickname | audienceNickname | 观众昵称 |

### 6.2 表关系

```
recharge (打赏表)
├── anchor_id → anchor.anchor_id
├── audience_id → audience.user_id
└── live_room_id → live_room.live_room_id
```

---

## 七、迁移检查清单

- [ ] 所有直接JDBC查询转换为Service调用
- [ ] 所有RedisTemplate直接操作转换为@Cacheable
- [ ] 所有try-catch转换为统一异常
- [ ] 所有System.out.println转换为TraceLogger
- [ ] 所有hard-coded SQL删除
- [ ] pom.xml依赖清理（移除不需要的JdbcTemplate声明）
- [ ] 编译通过，无警告
- [ ] 单元测试全部通过

---

## 八、相关文档

- [`数据分析模块设计文档`](./数据分析模块设计文档.md) - 详细的功能和算法规范
- [`DataAccessFacade使用指南`](../../DATAACCESS_FACADE_GUIDE.md) - 门面模式使用规范

---

**下一步**: 按Phase 2计划逐个重构分析服务实现

