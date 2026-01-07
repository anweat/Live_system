# 数据分析服务 (Data Analysis Service) - v2.0

## 概述

完全按照Common模块标准架构重构的数据分析服务。所有数据访问通过`DataAccessFacade`进行，集成了异常处理和日志系统，提供强大的数据分析能力。

## 架构设计

### 分层架构

```
┌─────────────────────────────────────────┐
│       REST API / Controller             │
│  StatisticsController                   │
│  RankingAnalysisController              │
│  FinancialAnalysisController (待实现)   │
│  UserAnalysisController (待实现)        │
│  ContentAnalysisController (待实现)     │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│     Analysis Service Layer              │
│  StatisticsService                      │
│  RankingService                         │
│  FinancialAnalysisService (待实现)      │
│  UserAnalysisService (待实现)           │
│  ContentAnalysisService (待实现)        │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│    QueryServiceAdapter (查询适配器)      │
│  统一管理DataAccessFacade的9个查询Service│
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│    DataAccessFacade (Common模块)         │
│  ├─ timeSeriesQuery()                   │
│  ├─ rankingQuery()                      │
│  ├─ aggregationQuery()                  │
│  ├─ tagAnalysisQuery()                  │
│  ├─ financialAnalysisQuery()            │
│  ├─ retentionAnalysisQuery()            │
│  ├─ heatmapAnalysisQuery()              │
│  └─ segmentationQuery()                 │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       Repository Layer (禁止直接调用)     │
│  RechargeRepository                     │
│  AnchorRepository                       │
│  AudienceRepository                     │
│  等其他Repository                       │
└─────────────────────────────────────────┘
```

### 关键设计原则

1. **禁止直接调用Repository** - 所有数据访问通过DataAccessFacade
2. **统一异常处理** - 使用BusinessException、ValidationException、SystemException
3. **集成日志系统** - 使用TraceLogger（业务追踪）和AppLogger（应用级别）
4. **统一缓存管理** - 使用@Cacheable注解和Redis/本地缓存
5. **参数验证** - 在Controller和Service层进行验证
6. **事务管理** - Service层使用@Transactional注解

## 项目结构

```
com.liveroom.analysis/
├── config/
│   ├── AnalysisProperties.java       # 配置属性类
│   ├── CacheConfig.java              # 缓存配置
│   └── QueryServiceAdapterConfig.java # (可选)
├── controller/
│   ├── StatisticsController.java     # 平台统计API
│   ├── RankingAnalysisController.java # 排行榜API
│   ├── FinancialAnalysisController.java # 财务分析API (待实现)
│   ├── UserAnalysisController.java    # 用户分析API (待实现)
│   └── ContentAnalysisController.java # 内容分析API (待实现)
├── service/
│   ├── StatisticsService.java         # 平台统计接口
│   ├── RankingService.java            # 排行榜接口
│   ├── FinancialAnalysisService.java  # 财务分析接口 (待实现)
│   ├── UserAnalysisService.java       # 用户分析接口 (待实现)
│   ├── ContentAnalysisService.java    # 内容分析接口 (待实现)
│   └── impl/
│       ├── StatisticsServiceImpl.java
│       ├── RankingServiceImpl.java
│       └── (其他Service实现类)
├── query/
│   └── QueryServiceAdapter.java       # 查询服务适配器
├── dto/
│   ├── AnchorIncomeDTO.java           # 主播收入
│   ├── AudienceConsumptionDTO.java    # 观众消费
│   └── (其他DTO)
├── vo/
│   ├── CashFlowTrendVO.java           # 现金流趋势
│   ├── KeyMetricsVO.java              # 关键指标
│   ├── TimeHeatmapVO.java             # 时段热力图
│   ├── TopRankingVO.java              # 排行榜
│   ├── AnchorPortraitVO.java          # 主播画像
│   └── AudiencePortraitVO.java        # 观众画像
├── exception/
│   └── AnalysisException.java         # 分析异常
├── handler/
│   └── GlobalExceptionHandler.java    # 全局异常处理
├── util/
│   ├── AnalysisCalculator.java        # 分析计算工具
│   └── CacheKeyBuilder.java           # 缓存键构建
└── DataAnalysisApplication.java       # 启动类
```

## API 接口

### 1. 平台统计分析 - `/api/v2/analysis/statistics`

#### 1.1 GMV趋势
```
GET /gmv-trend
参数:
  startDate: yyyy-MM-dd (必填)
  endDate: yyyy-MM-dd (必填)
  granularity: day/week/month (可选，默认day)

返回值: CashFlowTrendVO
```

#### 1.2 关键指标
```
GET /key-metrics
参数:
  startTime: yyyy-MM-dd HH:mm:ss (必填)
  endTime: yyyy-MM-dd HH:mm:ss (必填)

返回值: KeyMetricsVO
  {
    "totalGmv": 10000.00,           // 总流水
    "platformRevenue": 3000.00,     // 平台收入
    "anchorRevenue": 7000.00,       // 主播收入
    "transactionCount": 100,        // 交易笔数
    "payingUsers": 50,              // 付费用户
    "uniqueAnchors": 10,            // 独立主播
    "paymentRate": 20.00,           // 支付率(%)
    "arpu": 200.00,                 // 每用户平均收入
    "arppu": 200.00                 // 每付费用户平均收入
  }
```

#### 1.3 时段热力图
```
GET /time-heatmap
参数:
  startDate: yyyy-MM-dd (必填)
  endDate: yyyy-MM-dd (必填)

返回值: TimeHeatmapVO
  {
    "dateLabels": ["Monday", "Tuesday", ...],
    "hourLabels": ["00:00", "01:00", ...],
    "heatmapData": [[...], [...], ...],  // 二维数组
    "maxValue": 1000.00,
    "minValue": 0.00,
    "averageValue": 500.00,
    "unit": "GMV"
  }
```

#### 1.4 分类效果分析
```
GET /category-performance
参数:
  days: 1-365 (可选，默认30)

返回值: Object (分类效果分析数据)
```

### 2. 排行榜分析 - `/api/v2/analysis/ranking`

#### 2.1 主播收入排行
```
GET /top-anchors
参数:
  startTime: yyyy-MM-dd HH:mm:ss (必填)
  endTime: yyyy-MM-dd HH:mm:ss (必填)
  limit: 1-100 (可选，默认10)

返回值: TopRankingVO
```

#### 2.2 观众消费排行
```
GET /top-audience
参数:
  startTime: yyyy-MM-dd HH:mm:ss (必填)
  endTime: yyyy-MM-dd HH:mm:ss (必填)
  limit: 1-100 (可选，默认10)

返回值: TopRankingVO
```

#### 2.3 直播间热度排行
```
GET /hot-rooms
参数:
  startTime: yyyy-MM-dd HH:mm:ss (必填)
  endTime: yyyy-MM-dd HH:mm:ss (必填)
  limit: 1-100 (可选，默认10)

返回值: TopRankingVO
```

#### 2.4 增长率排行
```
GET /growth
参数:
  startTime: yyyy-MM-dd HH:mm:ss (必填)
  endTime: yyyy-MM-dd HH:mm:ss (必填)
  limit: 1-100 (可选，默认10)

返回值: TopRankingVO
```

## 异常处理

### 异常体系

```
BaseException (Common模块)
├── BusinessException       # 业务异常 → HTTP 400
├── ValidationException     # 参数验证异常 → HTTP 422
├── SystemException         # 系统异常 → HTTP 500
└── AnalysisException       # 分析异常 (extends BusinessException)
```

### 使用示例

```java
// 参数验证异常
if (startDate == null) {
    throw new ValidationException("开始日期不能为空");
}

// 业务异常
if (result.isEmpty()) {
    throw new BusinessException("查询结果为空");
}

// 系统异常
try {
    // 业务逻辑
} catch (Exception e) {
    throw new SystemException("系统错误: " + e.getMessage(), e);
}
```

### 异常响应格式

```json
{
  "code": 422,
  "message": "参数验证失败: 开始日期不能为空",
  "success": false,
  "data": null
}
```

## 日志使用

### TraceLogger (业务追踪)

用于追踪请求链路，自动生成traceId。

```java
// 信息级别
TraceLogger.info("StatisticsService", "getGmvTrend", 
    String.format("查询GMV趋势: %s - %s", startDate, endDate));

// 警告级别
TraceLogger.warn("StatisticsService", "getGmvTrend", 
    "缓存未命中，从数据库查询");

// 错误级别
TraceLogger.error("StatisticsService", "getGmvTrend", 
    "查询失败: " + e.getMessage());

// 调试级别
TraceLogger.debug("QueryServiceAdapter", "rankingQuery", 
    "获取排行榜查询Service");
```

### AppLogger (应用级别)

用于应用启动、关闭等系统级日志。

```java
// 启动日志
AppLogger.logStartup("Data Analysis Service", "1.0.0", 8084);

// 服务初始化
AppLogger.logServiceInitialize("AnalysisService");

// 通用日志
AppLogger.info("应用初始化完成");
AppLogger.warn("缓存初始化失败，使用备用缓存");
AppLogger.error("数据库连接失败", exception);
```

## 缓存策略

### 缓存分层

| 缓存层 | 存储 | TTL | 用途 |
|------|------|-----|------|
| L1 | 本地内存 | 5分钟 | 高频访问，快速变化 |
| L2 | Redis | 30分钟 | 相对稳定，分布式共享 |
| 预热 | 数据库 | - | 关键指标的定时计算 |

### 使用注解

```java
@Cacheable(
    value = CacheConfig.CacheNames.GMV_TREND,
    key = "'gmv_' + #granularity + '_' + #startDate + '_' + #endDate",
    unless = "#result == null"
)
public CashFlowTrendVO getGmvTrend(LocalDate startDate, LocalDate endDate, String granularity) {
    // 业务逻辑
}

// 清除缓存
@CacheEvict(value = CacheConfig.CacheNames.GMV_TREND, allEntries = true)
public void invalidateGmvCache() {
    TraceLogger.info("AnalysisService", "invalidateGmvCache", "清空GMV缓存");
}
```

### 缓存键命名规范

```
analysis:gmv:day:2024-01-01:2024-01-31
analysis:key_metrics:2024-01-31
analysis:ranking:anchor_income:10:2024-01-31
analysis:anchor:income:123:2024-01-01:2024-01-31
analysis:audience:consumption:456:2024-01-01:2024-01-31
```

## 工具类

### AnalysisCalculator

提供各种统计和数学计算方法。

```java
// 变异系数（数据波动程度）
BigDecimal cv = AnalysisCalculator.calculateCoefficientOfVariation(values);

// 环比增长率
BigDecimal momGrowth = AnalysisCalculator.calculateMoMGrowth(current, previous);

// 同比增长率
BigDecimal yoyGrowth = AnalysisCalculator.calculateYoYGrowth(current, previous);

// 占比百分比
BigDecimal percentage = AnalysisCalculator.calculatePercentage(part, total);

// 加权平均
BigDecimal weightedAvg = AnalysisCalculator.calculateWeightedAverage(values, weights);

// Pearson相关系数
BigDecimal correlation = AnalysisCalculator.calculatePearsonCorrelation(x, y);
```

### CacheKeyBuilder

统一构建缓存键。

```java
String gmvKey = CacheKeyBuilder.buildGmvTrendKey("day", "2024-01-01", "2024-01-31");
String metricsKey = CacheKeyBuilder.buildKeyMetricsKey("2024-01-31");
String rankingKey = CacheKeyBuilder.buildRankingKey("anchor_income", 10, "2024-01-31");
```

## 快速开始

### 1. 环境准备

```bash
# 确保MySQL和Redis正常运行
mysql -u root -p < database_scripts.sql
redis-cli ping
```

### 2. 配置application.yml

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/live_finance_db
    username: root
    password: root
  redis:
    host: localhost
    port: 6379
```

### 3. 启动应用

```bash
mvn clean install
mvn spring-boot:run
```

### 4. 测试API

```bash
# 查询关键指标
curl 'http://localhost:8084/data-analysis/api/v2/analysis/statistics/key-metrics' \
  -H 'Content-Type: application/json' \
  -d '{
    "startTime": "2024-01-01 00:00:00",
    "endTime": "2024-01-31 23:59:59"
  }'

# 查询排行榜
curl 'http://localhost:8084/data-analysis/api/v2/analysis/ranking/top-anchors?startTime=2024-01-01%2000:00:00&endTime=2024-01-31%2023:59:59&limit=10'
```

## 后续实现计划

### 优先级P1 (下一阶段)
- [ ] FinancialAnalysisService - 财务分析
- [ ] UserAnalysisService - 用户分析
- [ ] ContentAnalysisService - 内容分析

### 优先级P2 (可选/延期)
- [ ] 实时数据分析
- [ ] 预测分析模型
- [ ] 异常检测算法
- [ ] 批量导入导出

## 参考资源

- [Common模块文档](../common/README.md)
- [DataAccessFacade使用指南](../common/DATAACCESS_FACADE_GUIDE.md)
- [API响应格式规范](../common/docs/API_RESPONSE_FORMAT.md)

## 常见问题

### Q1: 为什么禁止直接调用Repository?
A: 确保数据访问的一致性，便于统一的缓存策略、权限控制、日志记录等。

### Q2: 如何清除缓存?
A: 使用`@CacheEvict`注解或直接调用CacheManager的`getCache().clear()`方法。

### Q3: 如何自定义缓存键?
A: 使用`CacheKeyBuilder`工具类生成规范的缓存键。

### Q4: 日志如何调试?
A: 使用`TraceLogger`记录traceId，便于分布式系统的日志追踪。

## 作者

数据分析服务开发团队

## 更新日期

2026-01-07

