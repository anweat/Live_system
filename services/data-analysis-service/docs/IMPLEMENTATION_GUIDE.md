# 数据分析服务 - 后续实现指南

## 概述

本文档指导如何完成后续的Service实现（财务分析、内容分析、用户分析）。

## 实现模板

所有Service实现都遵循以下模式：

### 1. Service实现类的基本结构

```java
package com.liveroom.analysis.service.impl;

import com.liveroom.analysis.config.CacheConfig;
import common.exception.AnalysisException;
import com.liveroom.analysis.query.QueryServiceAdapter;
import com.liveroom.analysis.service.YourService;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class YourServiceImpl implements YourService {

    private final QueryServiceAdapter queryServiceAdapter;

    @Override
    @Cacheable(
            value = CacheConfig.CacheNames.YOUR_CACHE_NAME,
            key = "'key_' + #param1 + '_' + #param2",
            unless = "#result == null"
    )
    public ResultVO yourMethod(String param1, String param2) {
        // 1. 参数验证
        if (param1 == null) {
            throw new ValidationException("参数不能为空");
        }

        try {
            // 2. 记录日志
            TraceLogger.info("YourService", "yourMethod", "开始处理...");

            // 3. 调用QueryServiceAdapter获取数据
            YourQueryService queryService = queryServiceAdapter.yourQuery();
            var data = queryService.getData(...);

            // 4. 数据处理和转换
            ResultVO result = transformToVO(data);

            // 5. 记录完成日志
            TraceLogger.info("YourService", "yourMethod", "处理完成");
            return result;

        } catch (ValidationException | AnalysisException e) {
            TraceLogger.warn("YourService", "yourMethod", "失败: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            TraceLogger.error("YourService", "yourMethod", "系统错误: " + e.getMessage());
            throw new AnalysisException("处理失败", e);
        }
    }

    // 工具方法
    private ResultVO transformToVO(Object data) {
        // 数据转换逻辑
        return null;
    }
}
```

### 2. Controller的基本结构

```java
package com.liveroom.analysis.controller;

import com.liveroom.analysis.service.YourService;
import com.liveroom.analysis.vo.ResultVO;
import common.bean.ApiResponse;
import common.logger.TraceLogger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/api/v2/analysis/your-module")
@RequiredArgsConstructor
public class YourController {

    private final YourService yourService;

    /**
     * API方法描述
     * @param param1 参数说明
     * @param param2 参数说明
     * @return 返回值说明
     */
    @GetMapping("/endpoint")
    public ApiResponse<ResultVO> yourMethod(
            @RequestParam String param1,
            @RequestParam(required = false) String param2) {
        
        TraceLogger.info("YourController", "yourMethod",
            String.format("处理请求: param1=%s, param2=%s", param1, param2));
        
        ResultVO result = yourService.yourMethod(param1, param2);
        return ApiResponse.success(result);
    }
}
```

---

## 第5阶段：财务分析服务实现指南

### 需要实现的方法

#### 1. getRevenueAnalysis()
```java
// 使用FinancialAnalysisQueryService
public Map<String, Object> getRevenueAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    var financialQuery = queryServiceAdapter.financialAnalysisQuery();
    
    // 获取数据
    var revenueData = financialQuery.getRevenueBreakdown(startTime, endTime);
    
    // 返回格式示例:
    return Map.of(
        "period", startTime + " to " + endTime,
        "totalGmv", revenueData.getTotalGmv(),
        "platformRevenue", revenueData.getPlatformRevenue(),
        "anchorRevenue", revenueData.getAnchorRevenue(),
        "commissionRate", revenueData.getCommissionRate(),
        "trend", calculateTrend(revenueData),
        "breakdown", revenueData.getBreakdown()
    );
}
```

#### 2. getCommissionAnalysis()
```java
// 分析主播的分成情况
public Map<String, Object> getCommissionAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    var aggregationQuery = queryServiceAdapter.aggregationQuery();
    var keyMetrics = aggregationQuery.getKeyMetrics(startTime, endTime);
    
    // 返回格式示例:
    return Map.of(
        "period", startTime + " to " + endTime,
        "totalCommission", keyMetrics.getAnchorRevenue(),
        "avgCommissionPerAnchor", calculateAverage(...),
        "topCommissionAnchor", findTop(...),
        "commissionDistribution", calculateDistribution(...)
    );
}
```

#### 3. getArpuAnalysis()
```java
// ARPU分析
public Map<String, Object> getArpuAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    var aggregationQuery = queryServiceAdapter.aggregationQuery();
    var keyMetrics = aggregationQuery.getKeyMetrics(startTime, endTime);
    
    return Map.of(
        "arpu", keyMetrics.getArpu(),
        "arppu", keyMetrics.getArppu(),
        "growth", calculateGrowth(...),
        "segmentation", segmentByValue(...)
    );
}
```

#### 4. getPaymentRateAnalysis()
```java
// 支付率和复购率分析
public Map<String, Object> getPaymentRateAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    // 需要计算：
    // 1. 支付率 = 付费用户 / 总用户
    // 2. 复购率 = 复购用户 / 付费用户
    // 3. 客单价 = 总收入 / 付费用户
    
    return Map.of(
        "paymentRate", calculatePaymentRate(...),
        "repeatPurchaseRate", calculateRepeatRate(...),
        "avgTransactionAmount", calculateAverage(...),
        "trend", calculateTrend(...)
    );
}
```

### 实现步骤

1. 创建 `FinancialAnalysisServiceImpl.java`
2. 在service/impl目录中实现4个方法
3. 创建 `FinancialAnalysisController.java`
4. 配置4个API端点
5. 编写单元测试
6. 验证缓存功能

---

## 第6阶段：内容分析服务实现指南

### 需要实现的方法

#### 1. getTagHeatmap()
```java
// 标签热力图（标签关联度）
public Map<String, Object> getTagHeatmap(LocalDateTime startTime, LocalDateTime endTime) {
    var tagAnalysisQuery = queryServiceAdapter.tagAnalysisQuery();
    var heatmapQuery = queryServiceAdapter.heatmapAnalysisQuery();
    
    // 获取标签关联数据
    var tagData = tagAnalysisQuery.getTagAssociation(startTime, endTime);
    
    // 返回热力矩阵
    return Map.of(
        "tags", extractTags(tagData),
        "heatmapMatrix", buildMatrix(tagData),
        "topAssociations", findTopAssociations(tagData),
        "recommendations", generateRecommendations(tagData)
    );
}
```

#### 2. getLiveRoomQuality()
```java
// 直播间质量分析
public Map<String, Object> getLiveRoomQuality(LocalDateTime startTime, LocalDateTime endTime) {
    var aggregationQuery = queryServiceAdapter.aggregationQuery();
    
    // 计算指标：观众数、平均停留时间、评分等
    return Map.of(
        "totalRooms", countRooms(...),
        "activeRooms", countActiveRooms(...),
        "avgViewers", calculateAvgViewers(...),
        "qualityRanking", rankRoomsByQuality(...)
    );
}
```

#### 3. getCategoryEffectiveness()
```java
// 分类效果分析
public Map<String, Object> getCategoryEffectiveness(LocalDateTime startTime, LocalDateTime endTime) {
    var aggregationQuery = queryServiceAdapter.aggregationQuery();
    
    return Map.of(
        "categories", listCategories(),
        "performance", calculatePerformance(...),
        "ranking", rankCategories(...),
        "trend", analyzeTrend(...)
    );
}
```

#### 4. getTimePeriodAnalysis()
```java
// 最佳直播时段分析
public Map<String, Object> getTimePeriodAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    var timeSeriesQuery = queryServiceAdapter.timeSeriesQuery();
    
    return Map.of(
        "hourlyDistribution", getHourlyDistribution(...),
        "peakHours", findPeakHours(...),
        "recommendations", recommendBestTimes(...)
    );
}
```

### 实现步骤

1. 创建 `ContentAnalysisServiceImpl.java`
2. 实现4个方法
3. 创建 `ContentAnalysisController.java`
4. 配置API端点
5. 编写测试

---

## 第7阶段：用户分析服务实现指南

### 需要实现的方法

#### 1. getAnchorPortrait()
```java
// 主播多维度画像
public AnchorPortraitVO getAnchorPortrait(Long anchorId, LocalDateTime startTime, LocalDateTime endTime) {
    // 收集数据：
    var anchor = queryServiceAdapter.anchor().findById(anchorId);
    var incomeData = queryServiceAdapter.analysisQuery().getRechargesByAnchorAndTimeRange(...);
    var followers = anchor.getFollowerCount();
    var retentionData = queryServiceAdapter.retentionAnalysisQuery().getRetention(...);
    
    // 计算指标
    BigDecimal totalIncome = calculateTotalIncome(incomeData);
    BigDecimal stabilityCoefficient = AnalysisCalculator.calculateCoefficientOfVariation(...);
    Map<String, Integer> radarScores = calculateRadarScores(...);
    
    // 组装VO
    return AnchorPortraitVO.builder()
        .anchorId(anchorId)
        .totalIncome(totalIncome)
        .stabilityCoefficient(stabilityCoefficient)
        .radarScores(radarScores)
        .overallScore(calculateOverallScore(radarScores))
        .build();
}
```

#### 2. getAudiencePortrait()
```java
// 观众多维度画像
public AudiencePortraitVO getAudiencePortrait(Long audienceId, LocalDateTime startTime, LocalDateTime endTime) {
    // RFM分析
    var consumptionData = queryServiceAdapter.analysisQuery().getAudienceConsumptionStats(audienceId);
    
    int recencyScore = calculateRecency(consumptionData);
    int frequencyScore = calculateFrequency(consumptionData);
    int monetaryScore = calculateMonetary(consumptionData);
    String rfmSegment = determineSegment(recencyScore, frequencyScore, monetaryScore);
    
    return AudiencePortraitVO.builder()
        .audienceId(audienceId)
        .totalConsumption(consumptionData.getTotalAmount())
        .recencyScore(recencyScore)
        .frequencyScore(frequencyScore)
        .monetaryScore(monetaryScore)
        .rfmSegment(rfmSegment)
        .build();
}
```

#### 3. getRetentionAnalysis()
```java
// 留存分析
public Map<String, Object> getRetentionAnalysis(LocalDateTime startTime, LocalDateTime endTime) {
    var retentionQuery = queryServiceAdapter.retentionAnalysisQuery();
    
    return Map.of(
        "nextDayRetention", retentionQuery.getNextDayRetention(startTime, endTime),
        "day7Retention", retentionQuery.getDay7Retention(startTime, endTime),
        "day30Retention", retentionQuery.getDay30Retention(startTime, endTime),
        "trend", analyzeTrend(...)
    );
}
```

#### 4. getChurnRiskPrediction()
```java
// 流失风险预测
public Map<String, Object> getChurnRiskPrediction(Long userId, Integer dayThreshold) {
    var audience = queryServiceAdapter.audience().findById(userId);
    var consumptionStats = queryServiceAdapter.analysisQuery().getAudienceConsumptionStats(userId);
    
    // 计算流失风险
    long daysSinceLastConsumption = calculateDaysSince(consumptionStats.getLastConsumptionTime());
    String churnRiskLevel = assessChurnRisk(daysSinceLastConsumption, dayThreshold);
    
    return Map.of(
        "userId", userId,
        "churnRiskLevel", churnRiskLevel,
        "daysSinceLastConsumption", daysSinceLastConsumption,
        "recommendation", generateRecommendation(churnRiskLevel)
    );
}
```

### 实现步骤

1. 创建 `UserAnalysisServiceImpl.java`
2. 实现4个方法
3. 创建 `UserAnalysisController.java`
4. 配置API端点
5. 编写测试

---

## 通用实现建议

### 1. 参数验证模式

```java
private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
    if (startTime == null || endTime == null) {
        throw new ValidationException("时间范围不能为空");
    }
    if (startTime.isAfter(endTime)) {
        throw new ValidationException("开始时间不能晚于结束时间");
    }
    if (startTime.isBefore(LocalDateTime.now().minusDays(90))) {
        throw new ValidationException("查询范围不能超过90天");
    }
}
```

### 2. 数据转换模式

```java
private ResultVO transformToVO(QueryResult data) {
    return ResultVO.builder()
        .field1(data.getValue1())
        .field2(data.getValue2())
        .field3(calculateDerived(data))
        .build();
}
```

### 3. 缓存策略

```java
// L1缓存（5分钟）+ L2缓存（30分钟）
@Cacheable(
    value = CacheConfig.CacheNames.YOUR_CACHE,
    key = "CacheKeyBuilder.buildYourKey(...)",
    unless = "#result == null"
)
public ResultVO method(...) {
    // 实现
}
```

### 4. 异常处理模式

```java
try {
    // 业务逻辑
    return result;
} catch (ValidationException | AnalysisException e) {
    TraceLogger.warn("Service", "method", "预期异常: " + e.getMessage());
    throw e;
} catch (Exception e) {
    TraceLogger.error("Service", "method", "系统错误: " + e.getMessage());
    throw new AnalysisException("处理失败", e);
}
```

---

## 测试建议

### 单元测试模板

```java
@SpringBootTest
public class YourServiceTest {

    @Autowired
    private YourService yourService;

    @MockBean
    private QueryServiceAdapter queryServiceAdapter;

    @Test
    public void testYourMethod() {
        // 1. 准备测试数据
        LocalDateTime startTime = LocalDateTime.now().minusDays(30);
        LocalDateTime endTime = LocalDateTime.now();

        // 2. 模拟依赖
        when(queryServiceAdapter.yourQuery().getData(...))
            .thenReturn(mockData());

        // 3. 执行方法
        ResultVO result = yourService.yourMethod(startTime, endTime);

        // 4. 验证结果
        assertNotNull(result);
        assertEquals(expectedValue, result.getValue());
    }

    @Test
    public void testValidationError() {
        assertThrows(ValidationException.class, () -> {
            yourService.yourMethod(null, LocalDateTime.now());
        });
    }
}
```

---

## 常见问题解决

### Q1: 如何调用QueryServiceAdapter?
```java
// 示例：调用timeSeriesQuery
var timeSeriesQuery = queryServiceAdapter.timeSeriesQuery();
var data = timeSeriesQuery.getDailyTimeSeries(startTime, endTime, true, true);
```

### Q2: 如何记录日志?
```java
TraceLogger.info("ServiceName", "methodName", "日志信息");
TraceLogger.debug("ServiceName", "methodName", "调试信息");
TraceLogger.warn("ServiceName", "methodName", "警告信息");
TraceLogger.error("ServiceName", "methodName", "错误信息");
```

### Q3: 如何建立缓存?
```java
// 在CacheConfig中添加缓存名称
public static final String YOUR_CACHE = "your_cache";

// 在方法上使用@Cacheable
@Cacheable(
    value = CacheConfig.CacheNames.YOUR_CACHE,
    key = "CacheKeyBuilder.buildYourKey(...)"
)
```

### Q4: 如何计算指标?
```java
// 使用AnalysisCalculator工具类
BigDecimal cv = AnalysisCalculator.calculateCoefficientOfVariation(values);
BigDecimal growth = AnalysisCalculator.calculateGrowthRate(current, baseline);
BigDecimal correlation = AnalysisCalculator.calculatePearsonCorrelation(x, y);
```

---

## 完成清单

在完成每个Service实现时，请检查以下项目：

- [ ] Service接口实现完成
- [ ] 所有方法都有TraceLogger日志
- [ ] 所有方法都有异常处理
- [ ] 所有方法都有@Cacheable注解（如需缓存）
- [ ] Controller已创建并配置
- [ ] API文档已编写（Javadoc）
- [ ] 参数验证已实现
- [ ] 单元测试已编写
- [ ] 集成测试已通过
- [ ] 性能测试已完成（缓存命中率等）

---

## 参考资源

1. **已完成的实现**
   - StatisticsServiceImpl.java
   - RankingServiceImpl.java

2. **工具类**
   - AnalysisCalculator.java
   - CacheKeyBuilder.java

3. **配置**
   - CacheConfig.java
   - AnalysisProperties.java

4. **异常处理**
   - GlobalExceptionHandler.java
   - AnalysisException.java

---

**最后更新**: 2026-01-07  
**维护者**: 开发团队

