# 数据分析服务 - 快速参考卡 (Quick Reference)

## 🚀 快速启动 (30秒)

```bash
# 1. 编译
mvn clean install

# 2. 运行
mvn spring-boot:run

# 3. 测试 (新建终端)
curl http://localhost:8084/data-analysis/api/v2/analysis/statistics/key-metrics \
  -d '{
    "startTime":"2024-01-01 00:00:00",
    "endTime":"2024-01-31 23:59:59"
  }'
```

---

## 📍 关键位置导航

### 配置文件
- **应用配置**: `src/main/resources/application.yml`
- **缓存配置**: `src/main/java/com/liveroom/analysis/config/CacheConfig.java`
- **属性配置**: `src/main/java/com/liveroom/analysis/config/AnalysisProperties.java`

### API文档
- **平台统计**: `com/liveroom/analysis/controller/StatisticsController.java`
- **排行榜**: `com/liveroom/analysis/controller/RankingAnalysisController.java`

### 业务逻辑
- **统计服务**: `com/liveroom/analysis/service/impl/StatisticsServiceImpl.java`
- **排行服务**: `com/liveroom/analysis/service/impl/RankingServiceImpl.java`

### 工具库
- **计算工具**: `com/liveroom/analysis/util/AnalysisCalculator.java`
- **缓存键**: `com/liveroom/analysis/util/CacheKeyBuilder.java`

### 数据模型
- **响应对象**: `com/liveroom/analysis/vo/` (6个VO)
- **传输对象**: `com/liveroom/analysis/dto/` (2个DTO)

---

## 🔌 API速查表

### 平台统计 (`/api/v2/analysis/statistics`)

| 端点 | 方法 | 说明 |
|------|------|------|
| `/gmv-trend` | GET | GMV趋势 |
| `/key-metrics` | GET | 关键指标 |
| `/time-heatmap` | GET | 时段热力 |
| `/category-performance` | GET | 分类效果 |

**示例**:
```bash
GET /gmv-trend?startDate=2024-01-01&endDate=2024-01-31&granularity=day
GET /key-metrics?startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59
GET /time-heatmap?startDate=2024-01-01&endDate=2024-01-31
GET /category-performance?days=30
```

### 排行榜 (`/api/v2/analysis/ranking`)

| 端点 | 方法 | 说明 |
|------|------|------|
| `/top-anchors` | GET | 主播收入排行 |
| `/top-audience` | GET | 观众消费排行 |
| `/hot-rooms` | GET | 直播间热度排行 |
| `/growth` | GET | 增长率排行 |

**示例**:
```bash
GET /top-anchors?startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59&limit=10
GET /top-audience?startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59&limit=10
GET /hot-rooms?startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59&limit=10
GET /growth?startTime=2024-01-01 00:00:00&endTime=2024-01-31 23:59:59&limit=10
```

---

## 📊 数据模型速查

### KeyMetricsVO (关键指标)
```json
{
  "totalGmv": 10000.00,           // 总流水
  "platformRevenue": 3000.00,     // 平台收入
  "anchorRevenue": 7000.00,       // 主播收入
  "transactionCount": 100,        // 交易笔数
  "payingUsers": 50,              // 付费用户
  "paymentRate": 20.00,           // 支付率(%)
  "arpu": 200.00,                 // ARPU
  "arppu": 200.00                 // ARPPU
}
```

### TopRankingVO (排行榜)
```json
{
  "rankingType": "anchor_income",
  "period": "2024-01-01 to 2024-01-31",
  "rankings": [
    {
      "rank": 1,
      "userId": 123,
      "userName": "主播名称",
      "primaryMetric": 5000.00,    // 主指标(收入/消费)
      "secondaryMetric": 10000.00, // 副指标1
      "tertiaryMetric": 100.00     // 副指标2
    }
  ],
  "totalCount": 10
}
```

---

## 🛠️ 工具库速查

### AnalysisCalculator (计算工具)

```java
// 变异系数 (波动程度)
BigDecimal cv = AnalysisCalculator.calculateCoefficientOfVariation(values);

// 环比增长率
BigDecimal growth = AnalysisCalculator.calculateMoMGrowth(current, previous);

// 占比百分比
BigDecimal percent = AnalysisCalculator.calculatePercentage(part, total);

// Pearson相关系数
BigDecimal correlation = AnalysisCalculator.calculatePearsonCorrelation(x, y);

// 波动率
BigDecimal volatility = AnalysisCalculator.calculateVolatility(values);
```

### CacheKeyBuilder (缓存键生成)

```java
// GMV趋势缓存键
String key1 = CacheKeyBuilder.buildGmvTrendKey("day", "2024-01-01", "2024-01-31");

// 关键指标缓存键
String key2 = CacheKeyBuilder.buildKeyMetricsKey("2024-01-31");

// 排行榜缓存键
String key3 = CacheKeyBuilder.buildRankingKey("anchor_income", 10, "2024-01-31");
```

---

## 📝 日志和异常速查

### TraceLogger (业务追踪)
```java
TraceLogger.info("ServiceName", "methodName", "信息");
TraceLogger.debug("ServiceName", "methodName", "调试");
TraceLogger.warn("ServiceName", "methodName", "警告");
TraceLogger.error("ServiceName", "methodName", "错误");
```

### 异常使用
```java
// 参数验证异常 (HTTP 422)
throw new ValidationException("参数不能为空");

// 业务异常 (HTTP 400)
throw new BusinessException("数据不存在");

// 系统异常 (HTTP 500)
throw new SystemException("处理失败", exception);
```

### 异常响应格式
```json
{
  "code": 422,
  "message": "参数验证失败: 开始日期不能为空",
  "success": false
}
```

---

## 🔄 缓存键命名规范

| 用途 | 格式 | 示例 |
|------|------|------|
| GMV趋势 | `analysis:gmv:{粒度}:{开始}:{结束}` | `analysis:gmv:day:2024-01-01:2024-01-31` |
| 关键指标 | `analysis:key_metrics:{日期}` | `analysis:key_metrics:2024-01-31` |
| 排行榜 | `analysis:ranking:{类型}:{数量}:{日期}` | `analysis:ranking:anchor_income:10:2024-01-31` |
| 主播收入 | `analysis:anchor:income:{ID}:{开始}:{结束}` | `analysis:anchor:income:123:2024-01-01:2024-01-31` |
| 观众消费 | `analysis:audience:consumption:{ID}:{开始}:{结束}` | `analysis:audience:consumption:456:2024-01-01:2024-01-31` |

---

## 📂 目录结构一览

```
data-analysis-service/
├── src/main/java/com/liveroom/analysis/
│   ├── config/                      # 配置类
│   ├── controller/                  # API控制器
│   ├── service/                     # 业务服务
│   │   └── impl/                    # 实现类
│   ├── query/                       # 查询适配器
│   ├── vo/                          # 响应对象
│   ├── dto/                         # 传输对象
│   ├── util/                        # 工具类
│   ├── exception/                   # 异常类
│   ├── handler/                     # 异常处理器
│   └── DataAnalysisApplication.java # 启动类
├── src/main/resources/
│   └── application.yml              # 应用配置
├── pom.xml                          # Maven配置
└── docs/
    ├── ARCHITECTURE_V2.md           # 架构详解
    ├── BUILD_COMPLETION_REPORT.md   # 完成报告
    ├── IMPLEMENTATION_GUIDE.md      # 实现指南
    └── FINAL_DELIVERY_SUMMARY.md    # 交付总结
```

---

## ⚡ 常见开发任务

### 添加新的Service实现

```java
// 1. 创建Service实现类
@Service
@RequiredArgsConstructor
public class YourServiceImpl implements YourService {
    
    private final QueryServiceAdapter queryServiceAdapter;
    
    @Override
    @Cacheable(value = "cache_name", key = "...")
    public ResultVO yourMethod(LocalDateTime startTime, LocalDateTime endTime) {
        try {
            TraceLogger.info("YourService", "yourMethod", "处理中...");
            
            // 调用QueryServiceAdapter
            var queryService = queryServiceAdapter.yourQuery();
            var data = queryService.getData(startTime, endTime);
            
            return transformToVO(data);
            
        } catch (ValidationException | AnalysisException e) {
            TraceLogger.warn("YourService", "yourMethod", e.getMessage());
            throw e;
        } catch (Exception e) {
            TraceLogger.error("YourService", "yourMethod", e.getMessage());
            throw new AnalysisException("处理失败", e);
        }
    }
}

// 2. 创建Controller
@RestController
@RequestMapping("/api/v2/analysis/your-module")
@RequiredArgsConstructor
public class YourController {
    
    private final YourService yourService;
    
    @GetMapping("/endpoint")
    public ApiResponse<ResultVO> yourMethod(
            @RequestParam @DateTimeFormat(...) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(...) LocalDateTime endTime) {
        
        TraceLogger.info("YourController", "yourMethod", "...");
        ResultVO result = yourService.yourMethod(startTime, endTime);
        return ApiResponse.success(result);
    }
}
```

### 添加新的VO类

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YourVO {
    // 字段
    private String field1;
    private BigDecimal field2;
    // ...
}
```

### 添加缓存

```java
// 在application.yml中添加缓存配置
analysis:
  cache:
    l1-time-to-live: 300        # L1 5分钟
    l2-time-to-live: 1800       # L2 30分钟

// 在Service方法上添加注解
@Cacheable(
    value = "cache_name",
    key = "CacheKeyBuilder.buildYourKey(...)",
    unless = "#result == null"
)
public ResultVO yourMethod(...) {
    // ...
}
```

---

## 🔍 常见问题排查

### 问题1: 缓存不生效
**检查清单**:
- [ ] Redis是否运行? `redis-cli ping`
- [ ] application.yml中cache配置是否正确?
- [ ] 方法上是否有@Cacheable注解?
- [ ] 缓存键是否正确生成?

### 问题2: 查询超时
**解决方案**:
- 检查时间范围是否过大(建议不超过90天)
- 使用缓存(默认30分钟)
- 检查数据库连接是否正常

### 问题3: 异常处理不当
**检查清单**:
- [ ] 是否抛出了正确的异常类型?
- [ ] 是否记录了TraceLogger日志?
- [ ] GlobalExceptionHandler是否能捕获?

---

## 📖 重要文档链接

| 文档 | 位置 | 说明 |
|------|------|------|
| 架构详解 | ARCHITECTURE_V2.md | 系统设计和原理 |
| 完成报告 | BUILD_COMPLETION_REPORT.md | 项目进度和统计 |
| 实现指南 | IMPLEMENTATION_GUIDE.md | 后续开发模板 |
| 交付总结 | FINAL_DELIVERY_SUMMARY.md | 完整的交付清单 |

---

## 📞 快速支持

### 常用命令

```bash
# 编译
mvn clean install

# 运行
mvn spring-boot:run

# 测试
mvn test

# 打包
mvn clean package

# 查看日志
tail -f logs/data-analysis-service.log
```

### 验证服务状态

```bash
# 检查服务是否运行
curl http://localhost:8084/data-analysis/api/v2/analysis/statistics/key-metrics

# 查看应用日志
tail logs/data-analysis-service.log | grep "应用启动成功"
```

---

**最后更新**: 2026-01-07  
**维护者**: 数据分析服务开发团队

