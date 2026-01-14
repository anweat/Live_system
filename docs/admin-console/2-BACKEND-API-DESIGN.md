# 后端API设计文档

## 服务信息

- **服务名称**: admin-console-service
- **服务端口**: 8090
- **基础路径**: /admin/api/v1
- **版本**: 1.0.0

## API设计原则

1. **RESTful风格**: 使用标准HTTP方法
2. **统一响应**: BaseResponse封装
3. **API聚合**: 调用各业务服务API
4. **错误处理**: 统一异常处理
5. **日志记录**: 所有操作记录日志

## 响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": "2026-01-07T10:00:00"
}
```

## API接口列表

### 1. 主播管理 (Anchor Management)

#### 1.1 主播列表
```
GET /api/v1/anchor/list
Query: page, size, orderBy, direction
Response: BaseResponse<PageResponse<AnchorVO>>
```

#### 1.2 创建主播
```
POST /api/v1/anchor
Body: AnchorCreateRequest
Response: BaseResponse<AnchorVO>
调用: anchor-service POST /api/v1/anchors
```

#### 1.3 主播详情
```
GET /api/v1/anchor/{anchorId}
Response: BaseResponse<AnchorVO>
调用: anchor-service GET /api/v1/anchors/{anchorId}
```

#### 1.4 更新主播
```
PUT /api/v1/anchor/{anchorId}
Body: AnchorUpdateRequest
Response: BaseResponse<AnchorVO>
调用: anchor-service PUT /api/v1/anchors/{anchorId}
```

#### 1.5 TOP主播（粉丝）
```
GET /api/v1/anchor/top/fans
Query: limit
Response: BaseResponse<List<AnchorVO>>
调用: anchor-service GET /api/v1/anchors/top/fans
```

#### 1.6 TOP主播（收益）
```
GET /api/v1/anchor/top/earnings
Query: limit
Response: BaseResponse<List<AnchorVO>>
调用: anchor-service GET /api/v1/anchors/top/earnings
```

---

### 2. 直播间管理 (Live Room Management)

#### 2.1 直播间详情
```
GET /api/v1/liveroom/{liveRoomId}
Response: BaseResponse<LiveRoomVO>
调用: anchor-service GET /api/v1/live-rooms/{liveRoomId}
```

#### 2.2 开启直播
```
POST /api/v1/liveroom/{liveRoomId}/start
Response: BaseResponse<LiveRoomVO>
调用: anchor-service POST /api/v1/live-rooms/{liveRoomId}/start
```

#### 2.3 结束直播
```
POST /api/v1/liveroom/{liveRoomId}/end
Response: BaseResponse<LiveRoomVO>
调用: anchor-service POST /api/v1/live-rooms/{liveRoomId}/end
```

#### 2.4 直播间实时数据
```
GET /api/v1/liveroom/{liveRoomId}/realtime
Response: BaseResponse<LiveRoomRealtimeVO>
调用: anchor-service GET /api/v1/live-rooms/{liveRoomId}/realtime
```

#### 2.5 主播打赏记录
```
GET /api/v1/liveroom/{liveRoomId}/recharges
Query: page, size
Response: BaseResponse<PageResponse<RechargeVO>>
调用: anchor-service GET /api/v1/recharges/live-room/{liveRoomId}
```

---

### 3. 观众管理 (Audience Management)

#### 3.1 观众列表
```
GET /api/v1/audience/list
Query: page, size, consumptionLevel
Response: BaseResponse<PageResponse<AudienceVO>>
调用: audience-service GET /api/v1/audiences
```

#### 3.2 创建观众
```
POST /api/v1/audience
Body: AudienceCreateRequest
Response: BaseResponse<AudienceVO>
调用: audience-service POST /api/v1/audiences
```

#### 3.3 观众详情
```
GET /api/v1/audience/{audienceId}
Response: BaseResponse<AudienceVO>
调用: audience-service GET /api/v1/audiences/{audienceId}
```

#### 3.4 观众搜索
```
GET /api/v1/audience/search
Query: keyword, page, size
Response: BaseResponse<PageResponse<AudienceVO>>
调用: audience-service GET /api/v1/audiences/search
```

#### 3.5 消费统计
```
GET /api/v1/audience/{audienceId}/consumption
Response: BaseResponse<ConsumptionStatsVO>
调用: audience-service GET /api/v1/audiences/{audienceId}/consumption-stats
```

#### 3.6 禁用/启用观众
```
PUT /api/v1/audience/{audienceId}/disable
PUT /api/v1/audience/{audienceId}/enable
Query: reason (可选)
Response: BaseResponse<Void>
调用: audience-service PUT /api/v1/audiences/{audienceId}/disable|enable
```

---

### 4. 打赏管理 (Recharge Management)

#### 4.1 创建打赏
```
POST /api/v1/recharge
Body: RechargeCreateRequest
Response: BaseResponse<RechargeVO>
调用: audience-service POST /api/v1/recharge
```

#### 4.2 主播打赏列表
```
GET /api/v1/recharge/anchor/{anchorId}
Query: page, size, startTime, endTime
Response: BaseResponse<PageResponse<RechargeVO>>
调用: audience-service GET /api/v1/recharge/anchor/{anchorId}
```

#### 4.3 观众打赏列表
```
GET /api/v1/recharge/audience/{audienceId}
Query: page, size
Response: BaseResponse<PageResponse<RechargeVO>>
调用: audience-service GET /api/v1/recharge/audience/{audienceId}
```

---

### 5. 财务管理 (Finance Management)

#### 5.1 提现申请列表
```
GET /api/v1/finance/withdrawal/{anchorId}
Query: status, page, size
Response: BaseResponse<PageResponse<WithdrawalVO>>
调用: finance-service GET /api/v1/withdrawal/{anchorId}
```

#### 5.2 提现审核（批准）
```
PUT /api/v1/finance/withdrawal/{withdrawalId}/approve
Response: BaseResponse<Void>
调用: finance-service PUT /api/v1/withdrawal/{withdrawalId}/approve
```

#### 5.3 提现审核（拒绝）
```
PUT /api/v1/finance/withdrawal/{withdrawalId}/reject
Query: reason
Response: BaseResponse<Void>
调用: finance-service PUT /api/v1/withdrawal/{withdrawalId}/reject
```

#### 5.4 主播余额查询
```
GET /api/v1/finance/balance/{anchorId}
Response: BaseResponse<BalanceVO>
调用: finance-service GET /api/v1/settlement/{anchorId}/balance
```

#### 5.5 结算明细
```
GET /api/v1/finance/settlement/{anchorId}
Query: page, size
Response: BaseResponse<PageResponse<SettlementDetailVO>>
调用: finance-service GET /api/v1/settlement/{anchorId}/details
```

#### 5.6 分成比例查询
```
GET /api/v1/finance/commission/{anchorId}
Response: BaseResponse<CommissionRateVO>
调用: finance-service GET /api/v1/commission-rate/{anchorId}
```

#### 5.7 更新分成比例
```
PUT /api/v1/finance/commission/{anchorId}
Body: CommissionRateUpdateRequest
Response: BaseResponse<CommissionRateVO>
调用: finance-service PUT /api/v1/commission-rate/{anchorId}
```

---

### 6. 数据分析 (Data Analysis)

#### 6.1 关键指标
```
GET /api/v1/analysis/metrics
Query: startTime, endTime
Response: BaseResponse<KeyMetricsVO>
调用: data-analysis-service GET /api/analysis/aggregation/metrics
```

#### 6.2 主播收入分析
```
GET /api/v1/analysis/anchor/income/{anchorId}
Query: startTime, endTime
Response: BaseResponse<AnchorIncomeAnalysisVO>
调用: data-analysis-service GET /api/v1/analysis/anchor/income/{anchorId}
```

#### 6.3 收入排行榜
```
GET /api/v1/analysis/ranking/income
Query: startTime, endTime, limit
Response: BaseResponse<List<AnchorIncomeAnalysisVO>>
调用: data-analysis-service GET /api/v1/analysis/anchor/income-ranking
```

#### 6.4 TOP消费者
```
GET /api/v1/analysis/ranking/toppayers/{anchorId}
Query: startTime, endTime, limit
Response: BaseResponse<List<RankingItemVO>>
调用: data-analysis-service GET /api/analysis/ranking/toppayers/{anchorId}
```

#### 6.5 每小时统计
```
GET /api/v1/analysis/hourly
Query: date
Response: BaseResponse<List<HourlyStatisticsVO>>
调用: data-analysis-service GET /api/analysis/aggregation/hourly
```

---

### 7. 模拟服务 (Mock Service)

#### 7.1 模拟数据统计
```
GET /api/v1/mock/statistics
Response: BaseResponse<MockDataStatistics>
调用: mock-service GET /api/v1/mock/data/statistics
```

#### 7.2 批次列表
```
GET /api/v1/mock/batches
Response: BaseResponse<List<MockBatchInfo>>
调用: mock-service GET /api/v1/mock/data/batches
```

#### 7.3 创建主播（单个）
```
POST /api/v1/mock/anchor
Body: CreateMockAnchorRequest
Response: BaseResponse<MockAnchorResult>
调用: mock-service POST /api/v1/mock/anchor/create
```

#### 7.4 批量生成主播
```
POST /api/v1/mock/anchor/batch
Body: BatchMockAnchorRequest (count)
Response: BaseResponse<BatchMockResult>
调用: mock-service POST /api/v1/mock/anchor/batch
```

#### 7.5 批量生成观众
```
POST /api/v1/mock/audience/batch
Body: BatchMockAudienceRequest (count)
Response: BaseResponse<BatchMockResult>
调用: mock-service POST /api/v1/mock/audience/batch
```

#### 7.6 生成打赏记录
```
POST /api/v1/mock/recharge/generate
Body: MockRechargeRequest
Response: BaseResponse<MockRechargeResult>
调用: mock-service POST /api/v1/mock/recharge/generate
```

#### 7.7 模拟直播活动
```
POST /api/v1/mock/activity/simulate
Body: SimulateActivityRequest
Response: BaseResponse<ActivitySimulationResult>
调用: mock-service POST /api/v1/mock/activity/simulate
```

#### 7.8 清空模拟数据
```
DELETE /api/v1/mock/data/clear
Response: BaseResponse<Void>
调用: mock-service DELETE /api/v1/mock/data/clear
```

---

## 数据模型 (VO)

### AnchorVO
```typescript
{
  anchorId: number
  nickname: string
  realName: string
  gender: string
  signature: string
  fanCount: number
  likeCount: number
  totalEarnings: number
  status: number
  liveRoomId: number
  createdAt: string
}
```

### LiveRoomVO
```typescript
{
  liveRoomId: number
  anchorId: number
  roomTitle: string
  category: string
  status: number  // 0-未开播, 1-直播中, 2-已结束
  startTime: string
  endTime: string
  totalAudience: number
  totalIncome: number
}
```

### AudienceVO
```typescript
{
  audienceId: number
  nickname: string
  realName: string
  gender: string
  consumptionLevel: number
  totalConsumption: number
  status: number
  isGuest: boolean
  createdAt: string
}
```

### WithdrawalVO
```typescript
{
  withdrawalId: number
  anchorId: number
  amount: number
  withdrawalType: number
  status: number  // 0-待审批, 1-已批准, 2-已拒绝, 3-已打款
  applyTime: string
  approveTime: string
  rejectReason: string
  traceId: string
}
```

### BalanceVO
```typescript
{
  anchorId: number
  totalEarnings: number
  settledAmount: number
  availableAmount: number
  pendingAmount: number
  percentage: number
}
```

### KeyMetricsVO
```typescript
{
  totalUsers: number
  activeUsers: number
  totalRevenue: number
  totalRecharges: number
  anchorCount: number
  liveRoomCount: number
  otherMetrics: Record<string, any>
}
```

---

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

## 开发注意事项

1. **API聚合**: 所有API调用业务服务，统一处理异常
2. **超时设置**: HTTP请求设置合理超时时间
3. **熔断降级**: 使用Resilience4j进行熔断保护
4. **请求日志**: 记录所有API调用日志
5. **参数校验**: 使用@Valid进行参数验证
