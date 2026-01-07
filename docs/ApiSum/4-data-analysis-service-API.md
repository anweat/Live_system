# 数据分析服务（Data Analysis Service）API 接口文档

## 服务基本信息
- **服务名称**: data-analysis-service
- **服务端口**: 8084
- **基础路径**: /analysis/api/v1 (部分端点使用 /api/analysis)
- **版本**: 1.0.0
- **功能描述**: 提供平台数据分析、统计、排行榜、用户洞察等数据分析功能

---

## 1. 聚合统计（Aggregation Analytics）

### 1.1 获取关键指标
- **端点**: `GET /api/analysis/aggregation/metrics`
- **功能**: 获取平台的核心关键指标（总用户、总收入、活跃用户等）
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间，ISO 8601格式)
  - `endTime`: LocalDateTime (必填，结束时间，ISO 8601格式)
- **响应**: BaseResponse<KeyMetricsDTO>
  - `totalUsers`: Long (总用户数)
  - `activeUsers`: Long (活跃用户数)
  - `totalRevenue`: BigDecimal (总收入)
  - `totalRecharges`: Long (总打赏笔数)
  - `anchorCount`: Long (主播总数)
  - `liveRoomCount`: Long (直播间总数)
  - `otherMetrics`: Map (其他关键指标)

---

## 2. 主播分析（Anchor Analysis）

### 2.1 获取主播收入分析
- **端点**: `GET /api/v1/analysis/anchor/income/{anchorId}`
- **功能**: 获取指定主播的详细收入分析
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<AnchorIncomeAnalysisDTO>
  - `anchorId`: Long
  - `totalIncome`: BigDecimal (总收入)
  - `settledIncome`: BigDecimal (已结算收入)
  - `avgIncome`: BigDecimal (平均每场直播收入)
  - `audienceCount`: Long (观众总数)
  - `rechargeCount`: Long (打赏笔数)
  - `topAudiences`: List (TOP打赏观众)
  - `timeline`: List (时间线数据)

### 2.2 获取主播收入排行榜
- **端点**: `GET /api/v1/analysis/anchor/income-ranking`
- **功能**: 获取指定时间内收入最高的主播排行榜
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `limit`: Integer (默认10，范围1-100，查询前N名)
- **响应**: BaseResponse<List<AnchorIncomeAnalysisDTO>>

---

## 3. 排行榜分析（Ranking Analytics）

### 3.1 获取主播TOP消费者排行榜
- **端点**: `GET /api/analysis/ranking/toppayers/{anchorId}`
- **功能**: 获取指定主播的TOP消费者排行榜
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `limit`: Integer (默认10，范围1-100)
- **响应**: BaseResponse<List<RankingItemDTO>>
  - 列表中每项包含：
    - `rank`: Integer (排名)
    - `audienceId`: Long
    - `nickname`: String (观众昵称)
    - `totalAmount`: BigDecimal (打赏总金额)
    - `rechargeCount`: Integer (打赏笔数)

---

## 4. 时间序列分析（Time Series Analytics）

### 4.1 获取每日时间序列数据
- **端点**: `GET /api/analysis/timeseries/daily`
- **功能**: 获取指定时间范围的每日统计数据（用于绘制图表）
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<TimeSeriesDataDTO>
  - `dates`: List<String> (日期列表)
  - `revenues`: List<BigDecimal> (每日收入)
  - `rechargeCount`: List<Integer> (每日打赏笔数)
  - `activeUsers`: List<Long> (每日活跃用户)
  - `onlineAnchors`: List<Integer> (每日在线主播数)

### 4.2 获取每小时时间序列数据
- **端点**: `GET /api/analysis/timeseries/hourly`
- **功能**: 获取指定时间范围的每小时统计数据（细粒度分析）
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<TimeSeriesDataDTO>
  - `hours`: List<String> (小时列表)
  - `revenues`: List<BigDecimal> (每小时收入)
  - 其他同daily格式

---

## 5. 用户画像分析（User Portrait Analytics）

### 5.1 获取用户画像
- **端点**: `GET /api/v1/analysis/user/{userId}`
- **功能**: 获取用户的详细画像和行为分析
- **路径参数**: userId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (可选，分析开始时间)
  - `endTime`: LocalDateTime (可选，分析结束时间)
- **响应**: BaseResponse<UserPortraitDTO>
  - `userId`: Long
  - `userType`: String (用户类型: anchor/audience)
  - `profile`: Map (基本信息)
  - `behaviors`: List (用户行为)
  - `interests`: List (兴趣标签)
  - `consumption`: Map (消费统计)

---

## 6. 保留率分析（Retention Analysis）

### 6.1 获取保留率数据
- **端点**: `GET /api/v1/analysis/retention`
- **功能**: 获取用户保留率分析（第1日、7日、30日保留率）
- **查询参数**:
  - `startTime`: LocalDateTime (必填，分析开始时间)
  - `endTime`: LocalDateTime (必填，分析结束时间)
  - `period`: String (可选，day/week/month)
- **响应**: BaseResponse<RetentionAnalysisDTO>
  - `newUsers`: Long (新增用户)
  - `retention1d`: Double (1日保留率)
  - `retention7d`: Double (7日保留率)
  - `retention30d`: Double (30日保留率)
  - `cohortData`: List (分群数据)

---

## 7. 财务分析（Financial Analysis）

### 7.1 获取财务分析数据
- **端点**: `GET /api/v1/analysis/financial`
- **功能**: 获取平台整体财务分析数据
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<FinancialAnalysisDTO>
  - `totalRevenue`: BigDecimal (总收入)
  - `settledAmount`: BigDecimal (已结算金额)
  - `pendingAmount`: BigDecimal (待结算金额)
  - `withdrawalAmount`: BigDecimal (已提现金额)
  - `platformCost`: BigDecimal (平台成本)
  - `profit`: BigDecimal (平台利润)
  - `profitMargin`: Double (利润率)

---

## 8. 热力图分析（Heatmap Analysis）

### 8.1 获取观众热力数据
- **端点**: `GET /api/v1/analysis/heatmap/audience`
- **功能**: 获取不同时段、主播类别的观众分布热力数据
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `dimension`: String (可选，time/category/anchor)
- **响应**: BaseResponse<HeatmapDataDTO>
  - `heatmap`: Map<String, Long> (热力数据矩阵)
  - `maxValue`: Long (最大值)
  - `minValue`: Long (最小值)

### 8.2 获取收入热力数据
- **端点**: `GET /api/v1/analysis/heatmap/revenue`
- **功能**: 获取不同时段、主播的收入分布热力数据
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `dimension`: String (可选，time/category/anchor)
- **响应**: BaseResponse<HeatmapDataDTO>

---

## 9. 标签关系分析（Tag Relation Analysis）

### 9.1 获取标签关系
- **端点**: `GET /api/v1/analysis/tags/relations`
- **功能**: 获取用户兴趣标签之间的关联关系
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `minSupport`: Integer (可选，最小支持度)
- **响应**: BaseResponse<List<TagRelationDTO>>
  - 列表中每项包含：
    - `tag1`: String (标签1)
    - `tag2`: String (标签2)
    - `correlation`: Double (关联度)
    - `support`: Long (支持度计数)

---

## 10. 任务触发（Task Trigger）

### 10.1 手动触发数据分析任务
- **端点**: `POST /api/v1/analysis/task/trigger`
- **功能**: 手动触发数据分析任务（通常由定时任务自动执行）
- **查询参数**:
  - `taskType`: String (任务类型，如: aggregation/ranking/timeseries等)
  - `parameters`: String (可选，JSON格式的任务参数)
- **响应**: BaseResponse<TaskResultDTO>
  - `taskId`: String (任务ID)
  - `status`: String (状态: pending/running/completed/failed)
  - `startTime`: LocalDateTime (开始时间)
  - `message`: String (说明)
- **权限**: 需要管理员权限

### 10.2 查询任务执行状态
- **端点**: `GET /api/v1/analysis/task/status/{taskId}`
- **功能**: 查询指定数据分析任务的执行状态
- **路径参数**: taskId (String)
- **响应**: BaseResponse<TaskStatusDTO>
  - `taskId`: String
  - `status`: String (pending/running/completed/failed)
  - `progress`: Integer (进度百分比)
  - `errorMessage`: String (错误信息，如果失败)

---

## 数据延迟说明

- **聚合统计**: 实时计算，无延迟
- **排行榜数据**: 5分钟更新一次
- **时间序列**: 1小时数据延迟
- **用户画像**: 每日计算，24小时延迟
- **保留率分析**: 每周计算，7天延迟

---

## 数据验证规则

- **ID参数**: 必须为正整数 (> 0)
- **金额参数**: BigDecimal，精确到两位小数
- **百分比参数**: 0-1之间
- **时间参数**: ISO 8601格式
- **limit参数**: 1 <= limit <= 100
- **period参数**: day/week/month/all

---

## 错误处理

所有接口都返回统一的响应格式：

```json
{
  "code": 200,
  "message": "查询成功",
  "data": {}
}
```

**常见错误码**:
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 500: 服务器错误
- 503: 数据未就绪

---

## 注意事项

1. 分析数据可能存在延迟，建议定期查询而不是实时调用
2. 大时间跨度的查询可能耗时较长，建议限制在90天内
3. 热力图数据的维度选择应根据业务需求调整
4. 排行榜数据默认按降序排列
5. 用户画像数据包含敏感信息，需要严格的权限控制
6. 保留率分析使用的是设备级别的标识，游客账户会单独统计
7. 手动触发任务会影响系统性能，建议在非高峰期执行

