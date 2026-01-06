# 数据分析服务 (Data Analysis Service)

## 概述

数据分析服务是直播打赏系统的核心分析模块，提供全方位的数据分析能力，包括主播收入分析、观众画像、标签热力图、流水趋势、排行榜等功能。

## 功能特性

### 1. 主播收入分析
- ✅ 日/周/月收入趋势分析
- ✅ 收入稳定性评估（变异系数CV）
- ✅ 7日/30日移动平均计算
- ✅ 环比增长率分析
- ✅ 多维度雷达图评估（收入能力、粉丝质量、互动能力等）
- ✅ 主播对比分析

### 2. 观众消费分析
- ✅ RFM模型用户分层（Recency、Frequency、Monetary）
- ✅ 观众画像生成
- ✅ LTV（生命周期价值）预测
- ✅ 消费分位数计算
- ✅ 留存分析（次日/7日/30日留存率）
- ✅ 流失预警（基于逻辑回归预测）

### 3. 标签热力图
- ✅ 标签关联度计算（Jaccard相似度）
- ✅ 标签共现频率分析
- ✅ 热力图矩阵生成
- ✅ 关联标签推荐
- ✅ 热门标签排行

### 4. 流水趋势分析
- ✅ GMV趋势（平台总流水）
- ✅ 移动平均（MA）和指数移动平均（EMA）
- ✅ 趋势强度分析
- ✅ 波动率计算
- ✅ 环比增长率
- ✅ 时段热力图（一周内每小时活跃度）

### 5. 排行榜
- ✅ 主播收入排行榜（日/周/月）
- ✅ 观众消费排行榜
- ✅ 主播粉丝数排行榜
- ✅ 直播间热度排行榜

### 6. 异步分析任务
- ✅ 小时统计任务
- ✅ 观众画像计算任务
- ✅ 标签关联度计算任务
- ✅ 留存分析任务

### 7. 手动数据分析 🆕
- ✅ 自定义SQL查询（安全限制）
- ✅ 全量/增量数据同步
- ✅ 自定义时间范围分析
- ✅ 时间段对比分析
- ✅ 用户群体分析（Cohort Analysis）
- ✅ 主播分组对比
- ✅ 漏斗分析（转化率）
- ✅ 异常检测（基于3σ原则）
- ✅ 相关性分析（Pearson相关系数）
- ✅ CSV数据导入分析
- ✅ 自定义报表生成
- ✅ 预测分析（线性回归）
- ✅ 缓存管理
- ✅ 数据质量检查
- ✅ 手动触发接口

## 技术架构

### 数据源
- **DB1 (live_audience_db)**: 观众服务数据库 - 用户、主播、观众、直播间、打赏记录、标签
- **DB2 (live_finance_db)**: 财务分析数据库 - 结算记录、打赏记录、统计数据、观众画像

### 技术栈
- Spring Boot 2.7.x
- Spring Data JPA
- Spring Data Redis
- MySQL 8.0
- Redis 6.0
- Apache Commons Math (数学计算)
- Hutool (工具库)

### 缓存策略
```
收入数据缓存: 1小时
观众画像缓存: 12小时
标签热力图缓存: 7天
排行榜缓存: 30分钟
统计数据缓存: 24小时
```

## API接口

### 基础路径
```
http://localhost:8084/data-analysis/api/v1/analysis
```

### 主播分析接口

#### 1. 查询主播收入分析
```
GET /anchor/{anchorId}/income?period=day&days=30

参数:
- anchorId: 主播ID
- period: 时间周期 (day/week/month)
- days: 查询天数 (默认30天)

返回: AnchorIncomeVO (包含时间序列、统计指标、趋势分析)
```

#### 2. 查询主播雷达图数据
```
GET /anchor/{anchorId}/radar

返回: 5维度雷达图数据
- 收入能力
- 粉丝质量
- 互动能力
- 收入稳定性
- 增长潜力
```

#### 3. 主播对比分析
```
GET /anchor/compare?anchorIds=1,2,3&period=day&days=30

参数:
- anchorIds: 主播ID列表（逗号分隔）
- period: 时间周期
- days: 查询天数
```

### 观众分析接口

#### 4. 查询观众画像
```
GET /audience/{audienceId}/portrait

返回: AudiencePortraitVO
- RFM评分
- 消费分层
- 活跃度等级
- 预测LTV
- 最喜欢的主播
- 偏好分类
```

#### 5. 查询消费分层统计
```
GET /audience/consumption-distribution

返回: 消费分层数据（高/中/低消费人群占比）
```

#### 6. 查询留存分析
```
GET /audience/retention?days=7

参数:
- days: 留存天数 (1/7/30)
```

#### 7. 查询流失预警
```
GET /audience/churn-warning?riskLevel=high&limit=20

参数:
- riskLevel: 风险等级 (high/medium/low)
- limit: 返回数量
```

### 标签分析接口

#### 8. 查询标签热力图
```
GET /tag/heatmap?limit=20

参数:
- limit: 返回标签数量

返回: TagHeatmapVO
- 标签列表
- 关联度矩阵
- 最强关联标签对
```

#### 9. 查询关联标签
```
GET /tag/related?tagName=游戏&limit=10

参数:
- tagName: 标签名称
- limit: 返回数量
```

#### 10. 查询热门标签
```
GET /tag/hot?limit=20
```

### 统计分析接口

#### 11. 查询GMV趋势
```
GET /statistics/gmv-trend?startDate=2026-01-01&endDate=2026-01-31&granularity=day

参数:
- startDate: 开始日期
- endDate: 结束日期
- granularity: 粒度 (hour/day/week/month)

返回: CashFlowTrendVO
- 时间序列数据
- 移动平均
- 趋势分析
```

#### 12. 查询平台关键指标
```
GET /statistics/key-metrics?date=2026-01-02

返回:
- GMV
- ARPU / ARPPU
- 付费率
- 复购率
```

#### 13. 查询时段热力图
```
GET /statistics/time-heatmap

返回: 一周内每小时的打赏活跃度矩阵
```

#### 14. 查询分类效果分析
```
GET /statistics/category-performance?days=30
```

### 排行榜接口

#### 15. 主播收入排行榜
```
GET /ranking/anchor/income?period=month&limit=10
```

#### 16. 观众消费排行榜
```
GET /ranking/audience/consumption?period=month&limit=10
```

#### 17. 主播粉丝数排行榜
```
GET /ranking/anchor/fans?limit=10
```

#### 18. 直播间热度排行榜
```
GET /ranking/live-room/popularity?limit=10
```

### 任务触发接口

#### 19. 触发小时统计任务
```
POST /task/trigger/hourly-statistics
```

#### 20. 触发观众画像计算
```
POST /task/trigger/audience-portrait
```

#### 21. 触发标签关联度计算
```
POST /task/trigger/tag-relation
```

#### 22. 触发留存分析
```
POST /task/trigger/retention-analysis
```

#### 23. 查询任务状态
```
GET /task/status
```

## 核心算法

### 1. RFM模型
```
R (Recency) - 最近一次消费:
  5分: 7天内  4分: 8-14天  3分: 15-30天  2分: 31-60天  1分: 60天以上

F (Frequency) - 消费频次:
  5分: ≥20次  4分: 10-19次  3分: 5-9次  2分: 2-4次  1分: 1次

M (Monetary) - 消费金额:
  5分: ≥P80  4分: P60-P80  3分: P40-P60  2分: P20-P40  1分: <P20

综合得分 = R×0.3 + F×0.3 + M×0.4
```

### 2. 标签关联度（Jaccard相似度）
```
Jaccard(A, B) = |A ∩ B| / |A ∪ B|
关联度评分 = Jaccard × 100
```

### 3. 变异系数（收入稳定性）
```
CV = 标准差 / 平均值
稳定性得分 = (1 - CV) × 10
```

### 4. 移动平均
```
MA_n = (x₁ + x₂ + ... + xₙ) / n
EMA_t = α × x_t + (1 - α) × EMA_(t-1)
```

### 5. LTV预测
```
LTV_简化 = ARPPU × 平均消费频次 × 预期留存月数
```

## 定时任务

### 高频任务（每小时）
```
0 0 * * * *  # 小时统计任务
```

### 中频任务（每天）
```
0 2 * * *  # 日统计任务（凌晨2点）
0 3 * * *  # 留存分析任务（凌晨3点）
0 4 * * *  # 标签关联度计算（凌晨4点）
```

### 低频任务（每周/月）
```
0 5 * * 1  # 周统计任务（周一凌晨5点）
0 6 1 * *  # 月统计任务（每月1日凌晨6点）
```

## 部署说明

### 1. 配置数据库连接
编辑 `application.yml`:
```yaml
spring:
  datasource:
    db1:
      jdbc-url: jdbc:mysql://localhost:3306/live_audience_db
      username: root
      password: your_password
    db2:
      jdbc-url: jdbc:mysql://localhost:3306/live_finance_db
      username: root
      password: your_password
```

### 2. 配置Redis
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 3
```

### 3. 启动服务
```bash
mvn clean package
java -jar target/data-analysis-service-1.0-SNAPSHOT.jar
```

### 4. 访问接口
```
http://localhost:8084/data-analysis/api/v1/analysis/statistics/key-metrics
```

## 前端集成示例

### 使用Axios调用接口
```javascript
// 查询主播收入趋势
axios.get('/data-analysis/api/v1/analysis/anchor/123/income', {
  params: { period: 'day', days: 30 }
}).then(response => {
  const data = response.data.data;
  // 渲染图表
  renderLineChart(data.timeSeries);
});

// 查询标签热力图
axios.get('/data-analysis/api/v1/analysis/tag/heatmap', {
  params: { limit: 20 }
}).then(response => {
  const heatmap = response.data.data;
  // 渲染热力图
  renderHeatmap(heatmap.matrix, heatmap.tags);
});

// 查询排行榜
axios.get('/data-analysis/api/v1/analysis/ranking/anchor/income', {
  params: { period: 'month', limit: 10 }
}).then(response => {
  const ranking = response.data.data;
  // 渲染排行榜
  renderRanking(ranking.rankings);
});
```

### 图表渲染（使用ECharts）
```javascript
// 折线图 - 收入趋势
const option = {
  xAxis: { data: data.timeSeries.map(t => t.timeLabel) },
  yAxis: {},
  series: [
    { name: '收入', type: 'line', data: data.timeSeries.map(t => t.income) },
    { name: 'MA7', type: 'line', data: data.timeSeries.map(t => t.ma7) }
  ]
};

// 热力图 - 标签关联度
const option = {
  xAxis: { data: heatmap.tags },
  yAxis: { data: heatmap.tags },
  series: [{
    type: 'heatmap',
    data: heatmap.matrix.flatMap((row, i) => 
      row.map((value, j) => [i, j, value])
    )
  }]
};
```

## 性能优化

### 1. Redis缓存
- 热数据缓存，减少数据库查询
- 分级TTL策略

### 2. 数据库优化
- 复合索引
- 读写分离
- 物化视图

### 3. 异步处理
- 定时任务异步执行
- Spring @Async支持

### 4. 限流保护
- Redis令牌桶算法
- 分布式锁防并发

## 监控与日志

### 日志级别
```yaml
logging:
  level:
    com.liveroom.analysis: DEBUG
```

### 关键日志
- 接口调用日志
- 任务执行日志
- 异常错误日志
- 性能指标日志

## 常见问题

### Q1: 如何手动触发分析任务？
A: 调用 `/task/trigger/*` 接口

### Q2: 缓存何时更新？
A: 缓存过期后自动更新，也可通过定时任务强制更新

### Q3: 如何扩展新的分析指标？
A: 在Service中添加新方法，在Controller中暴露接口

### Q4: 性能瓶颈如何优化？
A: 增加缓存TTL、优化SQL查询、增加索引、使用物化视图

## 相关文档

- [数据分析模块设计文档](docs/数据分析模块设计文档.md)
- [数据库设计文档](../db-service/docs/数据库设计文档.md)
- [Nginx接口汇总](../nginx/NGINX接口汇总.md)

---

**维护者**: Team  
**最后更新**: 2026-01-02
