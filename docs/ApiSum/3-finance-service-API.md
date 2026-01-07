# 财务服务（Finance Service）API 接口文档

## 服务基本信息
- **服务名称**: finance-service
- **服务端口**: 8083
- **基础路径**: /finance/api/v1
- **版本**: 1.0.0
- **功能描述**: 提供财务管理、结算统计、提现审核、分成比例管理等核心财务功能

---

## 1. 提现管理（Withdrawal Management）

### 1.1 主播申请提现
- **端点**: `POST /api/v1/withdrawal`
- **功能**: 处理主播的提现申请，支持与anchor-service端口的申请同步
- **请求体**: WithdrawalRequestDTO
  - `anchorId`: Long (主播ID)
  - `amount`: BigDecimal (提现金额)
  - `withdrawalType`: Integer (0=银行卡, 1=支付宝等)
  - `traceId`: String (唯一标识，防止重复)
  - 其他字段...
- **响应**: BaseResponse<WithdrawalDTO>
- **幂等性**: 通过traceId + 60秒超时实现
- **注解**: @Log("申请提现"), @ValidateParam, @Idempotent

### 1.2 查询提现记录
- **端点**: `GET /api/v1/withdrawal/{anchorId}`
- **功能**: 分页查询主播的提现申请记录
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `status`: Integer (可选，提现状态：0=待审批, 1=已批准, 2=已拒绝, 3=已打款)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<WithdrawalDTO>>
- **验证**: anchorId > 0, page >= 1, 1 <= size <= 100

### 1.3 按traceId查询提现记录
- **端点**: `GET /api/v1/withdrawal/by-trace-id/{traceId}`
- **功能**: 根据唯一标识查询单条提现申请
- **路径参数**: traceId (String)
- **响应**: BaseResponse<WithdrawalDTO>
- **用途**: 幂等检查、查询前次操作结果

### 1.4 审核通过提现申请
- **端点**: `PUT /api/v1/withdrawal/{withdrawalId}/approve`
- **功能**: 财务管理员审批通过提现申请
- **路径参数**: withdrawalId (Long)
- **响应**: BaseResponse<Void>
- **权限**: 需要财务管理员权限
- **效果**: 提现状态更新为已批准，后续异步处理打款

### 1.5 拒绝提现申请
- **端点**: `PUT /api/v1/withdrawal/{withdrawalId}/reject`
- **功能**: 财务管理员拒绝提现申请
- **路径参数**: withdrawalId (Long)
- **查询参数**:
  - `reason`: String (必填，拒绝原因)
- **响应**: BaseResponse<Void>
- **权限**: 需要财务管理员权限
- **效果**: 资金返回到主播账户，发送通知

---

## 2. 结算管理（Settlement Management）

### 2.1 查询主播可提取金额
- **端点**: `GET /api/v1/settlement/{anchorId}/balance`
- **功能**: 查询主播当前的可结算和可提取余额
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<BalanceVO>
  - `totalEarnings`: BigDecimal (累计收入)
  - `settledAmount`: BigDecimal (已结算金额)
  - `availableAmount`: BigDecimal (可提取金额)
  - `pendingAmount`: BigDecimal (待结算金额)
  - `percentage`: BigDecimal (分成比例)
- **验证**: anchorId > 0

### 2.2 查询结算明细
- **端点**: `GET /api/v1/settlement/{anchorId}/details`
- **功能**: 查询主播的详细结算记录（每次结算的明细）
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startDate`: LocalDateTime (可选，开始日期)
  - `endDate`: LocalDateTime (可选，结束日期)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<SettlementDetailVO>>
- **验证**: anchorId > 0, page >= 1, 1 <= size <= 100

### 2.3 手动触发结算
- **端点**: `POST /api/v1/settlement/trigger`
- **功能**: 管理员手动触发指定主播的结算（通常由定时任务自动执行）
- **查询参数**:
  - `anchorId`: Long (必填，要结算的主播ID)
- **响应**: BaseResponse<Void>
- **权限**: 需要财务管理员权限
- **注意**: 生产环境应谨慎使用，可能影响结算周期

---

## 3. 统计分析（Statistics & Analytics）

### 3.1 查询主播收入统计
- **端点**: `GET /api/v1/statistics/anchor/revenue/{anchorId}`
- **功能**: 查询主播在指定时间范围内的收入统计
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间，ISO 8601格式)
  - `endTime`: LocalDateTime (必填，结束时间，ISO 8601格式)
- **响应**: BaseResponse<AnchorRevenueVO>
  - `anchorId`: Long
  - `totalRevenue`: BigDecimal (总打赏金额)
  - `settledRevenue`: BigDecimal (已结算金额)
  - `commissionRate`: BigDecimal (分成比例)
  - `actualRevenue`: BigDecimal (实际收入 = 总打赏 * 分成比例)
  - `statistics`: Map (其他统计数据)
- **验证**: anchorId > 0, startTime < endTime

### 3.2 查询主播每小时统计
- **端点**: `GET /api/v1/statistics/anchor/hourly/{anchorId}`
- **功能**: 查询主播按小时统计的收入数据（用于绘制收入趋势图）
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<List<HourlyStatisticsVO>>
  - 列表中每项包含：
    - `hour`: String (小时标识)
    - `revenue`: BigDecimal (该小时的收入)
    - `rechargeCount`: Integer (该小时的打赏次数)
    - `audienceCount`: Integer (该小时的打赏观众数)

### 3.3 查询主播TOP打赏观众
- **端点**: `GET /api/v1/statistics/anchor/top-audiences/{anchorId}`
- **功能**: 查询主播在指定时间内打赏金额最高的TOP观众
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `topN`: Integer (默认10，范围1-100，查询前N名)
- **响应**: BaseResponse<List<TopAudienceVO>>
  - 列表中每项包含：
    - `audienceId`: Long
    - `nickname`: String
    - `totalAmount`: BigDecimal (打赏总金额)
    - `rechargeCount`: Integer (打赏次数)
    - `lastRechargeTime`: LocalDateTime

### 3.4 批量查询主播收入统计
- **端点**: `POST /api/v1/statistics/anchor/batch-revenue`
- **功能**: 一次性查询多个主播的收入统计
- **请求体**: List<Long> (主播ID列表)
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
- **响应**: BaseResponse<List<AnchorRevenueVO>>
- **用途**: 批量报表生成、数据分析等

### 3.5 查询主播收入排名
- **端点**: `GET /api/v1/statistics/top-anchors`
- **功能**: 查询指定时间内收入最高的主播排行榜
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `topN`: Integer (默认10，范围1-100)
- **响应**: BaseResponse<List<AnchorRevenueVO>>
- **用途**: 平台排行榜展示

---

## 4. 分成比例管理（Commission Rate Management）

### 4.1 创建/更新主播分成比例
- **端点**: `POST /api/v1/commission`
- **功能**: 为主播设置或更新分成比例（可设置多个等级）
- **请求体**: CommissionRateDTO
  - `anchorId`: Long (主播ID)
  - `rate`: BigDecimal (分成比例，如0.5表示50%)
  - `effectiveDate`: LocalDateTime (生效日期)
  - `endDate`: LocalDateTime (结束日期，可选)
  - `description`: String (说明)
- **响应**: BaseResponse<CommissionRateDTO>
- **权限**: 需要财务管理员权限

### 4.2 查询主播当前分成比例
- **端点**: `GET /api/v1/commission/{anchorId}/current`
- **功能**: 查询主播当前生效的分成比例
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<CommissionRateDTO>
- **验证**: anchorId > 0

### 4.3 查询主播分成比例历史
- **端点**: `GET /api/v1/commission/{anchorId}/history`
- **功能**: 查询主播的历史分成比例变化记录
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `page`: Integer (默认1)
  - `size`: Integer (默认20)
- **响应**: BaseResponse<PageResponse<CommissionRateDTO>>

---

## 5. 数据同步接口（Internal Sync - 内部接口）

### 5.1 接收批量打赏数据
- **端点**: `POST /internal/sync/recharges`
- **功能**: 接收观众服务推送的打赏数据（内部接口，不对外开放）
- **请求体**: BatchRechargeDTO
  - `recharges`: List<RechargeDTO> (打赏记录列表)
  - `sourceService`: String (来源服务，如"audience-service")
  - `batchId`: String (批次ID，用于幂等性)
  - `timestamp`: Long (时间戳)
- **响应**: BaseResponse<Void>
- **幂等性**: 通过batchId确保同一批数据只处理一次
- **权限**: 内部服务间调用

### 5.2 查询同步进度
- **端点**: `GET /internal/sync/progress`
- **功能**: 查询指定来源服务的数据同步进度
- **查询参数**:
  - `sourceService`: String (来源服务名称)
- **响应**: BaseResponse<SyncProgress>
  - `lastSyncTime`: LocalDateTime (最后同步时间)
  - `totalSynced`: Long (已同步总数)
  - `pendingCount`: Long (待同步数量)
  - `status`: String (同步状态)
- **权限**: 内部服务调用

---

## 财务状态流转

### 提现申请的生命周期
```
待审批 → 已批准 → 处理中 → 已打款
      ↓
    已拒绝 (资金退回)
```

### 结算流程
```
打赏产生 → 待结算 → 自动结算(每日/周/月) → 已结算 → 可提取
```

---

## 数据验证规则

- **ID参数**: 必须为正整数 (> 0)
- **金额参数**: BigDecimal，精确到两位小数
- **百分比参数**: 0-1之间（如0.5表示50%）
- **时间参数**: ISO 8601格式
- **分页参数**: page >= 1, 1 <= size <= 100
- **topN参数**: 1 <= topN <= 100

---

## 错误处理

所有接口都返回统一的响应格式：

```json
{
  "code": 200,
  "message": "提现申请成功",
  "data": {}
}
```

**常见错误码**:
- 200: 成功
- 400: 请求参数错误（如金额格式错误）
- 401: 未授权
- 403: 禁止访问（权限不足）
- 404: 资源不存在
- 409: 冲突（如重复提现申请）
- 500: 服务器错误

**常见错误场景**:
- 余额不足：返回代码 400，说明可提取金额不足
- 提现申请重复：返回代码 409，提示使用相同traceId的前次操作结果
- 权限不足：返回代码 403，提示需要财务管理员权限
- 时间参数错误：返回代码 400，提示时间格式或范围

---

## 对账与审计

### 核心对账机制
1. **打赏端到端**: 观众服务 → 财务服务 → 结算表
2. **数据一致性**: 通过幂等性和事务保证
3. **审计日志**: 所有财务操作都有完整日志

### 定期检查
- 每日对账：核对前一天的结算数据
- 每周报表：汇总周统计数据
- 月度结算：执行月结算流程

---

## 性能与限制

- **分页查询**: 最多返回100条记录
- **批量查询**: 最多支持500个ID
- **时间跨度**: 单次查询不建议超过90天
- **并发限制**: 同一主播的同步操作串行化（防止竞态）

---

## 注意事项

1. 所有涉及金钱的操作都需要严格的权限控制
2. 提现申请批准后，会启动异步任务处理实际打款
3. 分成比例变更需要提前计划，新比例只对变更后的打赏生效
4. 结算明细可用于财务对账，建议定期导出
5. 统计数据可能存在5分钟延迟（取决于ETL任务）
6. 同步接口只接受来自可信服务的请求，需要配置服务间认证

