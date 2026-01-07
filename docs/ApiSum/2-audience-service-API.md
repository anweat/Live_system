# 观众服务（Audience Service）API 接口文档

## 服务基本信息
- **服务名称**: audience-service
- **服务端口**: 8082
- **基础路径**: /audience/api/v1
- **版本**: 1.0.0
- **功能描述**: 提供观众账户管理、打赏记录等功能，支持与财务服务的数据同步

---

## 1. 观众管理（Audience Management）

### 1.1 创建观众（用户注册）
- **端点**: `POST /api/v1/audiences`
- **功能**: 创建新的观众账户（正式用户）
- **请求体**: AudienceDTO (nickname, realName, gender等)
- **响应**: BaseResponse<AudienceDTO>
- **幂等性**: 通过nickname + 30秒超时时间实现
- **注解**: @Log("创建观众"), @ValidateParam, @Idempotent

### 1.2 创建游客观众
- **端点**: `POST /api/v1/audiences/guest`
- **功能**: 创建临时游客账户，无需注册信息
- **请求体**: AudienceDTO (可选，为空则自动生成)
- **响应**: BaseResponse<AudienceDTO>

### 1.3 获取观众信息
- **端点**: `GET /api/v1/audiences/{audienceId}`
- **功能**: 查询单个观众的详细信息
- **路径参数**: audienceId (Long)
- **响应**: BaseResponse<AudienceDTO>
- **验证**: audienceId > 0

### 1.4 修改观众信息
- **端点**: `PUT /api/v1/audiences/{audienceId}`
- **功能**: 更新观众的基本信息
- **路径参数**: audienceId (Long)
- **请求体**: AudienceDTO (部分字段)
- **响应**: BaseResponse<AudienceDTO>
- **验证**: audienceId > 0, audienceDTO不为空

### 1.5 查询观众列表（分页）
- **端点**: `GET /api/v1/audiences`
- **功能**: 分页查询观众列表，支持按消费等级筛选
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
  - `consumptionLevel`: Integer (可选，0/1/2表示不同消费等级)
- **响应**: BaseResponse<PageResponse<AudienceDTO>>
- **验证**: 消费等级取值范围0-2

### 1.6 搜索观众
- **端点**: `GET /api/v1/audiences/search`
- **功能**: 按昵称或其他关键词搜索观众
- **查询参数**:
  - `keyword`: String (必填，搜索关键词，最长50字符)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<AudienceDTO>>
- **验证**: keyword不能为空，长度限制

### 1.7 获取观众消费统计
- **端点**: `GET /api/v1/audiences/{audienceId}/consumption-stats`
- **功能**: 查询观众的消费统计数据（打赏总额、打赏次数等）
- **路径参数**: audienceId (Long)
- **响应**: BaseResponse<ConsumptionStatsDTO>
- **验证**: audienceId > 0

### 1.8 禁用观众账户
- **端点**: `PUT /api/v1/audiences/{audienceId}/disable`
- **功能**: 禁用违规观众账户
- **路径参数**: audienceId (Long)
- **查询参数**:
  - `reason`: String (可选，禁用原因，最长200字符)
- **响应**: BaseResponse<Void>
- **权限**: 需要管理员权限

### 1.9 启用观众账户
- **端点**: `PUT /api/v1/audiences/{audienceId}/enable`
- **功能**: 启用被禁用的观众账户
- **路径参数**: audienceId (Long)
- **响应**: BaseResponse<Void>
- **权限**: 需要管理员权限

---

## 2. 打赏管理（Recharge Management）

### 2.1 创建打赏记录
- **端点**: `POST /api/v1/recharge`
- **功能**: 创建新的打赏记录（观众给主播的打赏）
- **请求体**: RechargeDTO (audienceId, anchorId, amount, traceId等)
- **响应**: BaseResponse<RechargeDTO>
- **幂等性**: 通过traceId + 60秒超时时间实现（防止重复打赏）
- **注解**: @Log("创建打赏记录"), @ValidateParam, @Idempotent

### 2.2 获取打赏记录详情
- **端点**: `GET /api/v1/recharge/{rechargeId}`
- **功能**: 查询单条打赏记录的详细信息
- **路径参数**: rechargeId (Long)
- **响应**: BaseResponse<RechargeDTO>
- **验证**: rechargeId > 0

### 2.3 按traceId查询打赏记录
- **端点**: `GET /api/v1/recharge/by-trace-id/{traceId}`
- **功能**: 根据唯一标识traceId查询打赏记录（用于幂等检查）
- **路径参数**: traceId (String，最长64字符)
- **响应**: BaseResponse<RechargeDTO>
- **用途**: 检查重复打赏、查询前次操作结果

### 2.4 查询主播的打赏列表
- **端点**: `GET /api/v1/recharge/anchor/{anchorId}`
- **功能**: 分页查询指定主播收到的所有打赏记录
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<RechargeDTO>>
- **验证**: anchorId > 0, page >= 1, 1 <= size <= 100

### 2.5 查询观众的打赏历史
- **端点**: `GET /api/v1/recharge/audience/{audienceId}`
- **功能**: 分页查询指定观众的所有打赏历史
- **路径参数**: audienceId (Long)
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<RechargeDTO>>
- **验证**: audienceId > 0

### 2.6 查询直播间的打赏列表
- **端点**: `GET /api/v1/recharge/live-room/{liveRoomId}`
- **功能**: 分页查询指定直播间的所有打赏记录
- **路径参数**: liveRoomId (Long)
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1，最大100)
- **响应**: BaseResponse<PageResponse<RechargeDTO>>
- **验证**: liveRoomId > 0

### 2.7 查询主播的TOP 10打赏观众
- **端点**: `GET /api/v1/recharge/top10`
- **功能**: 查询指定主播打赏金额最高的前10名观众
- **查询参数**:
  - `anchorId`: Long (必填，主播ID)
  - `period`: String (默认all，可选值: day/week/month/all)
- **响应**: BaseResponse<List<Top10AudienceVO>>
- **验证**: anchorId > 0, period必须是指定的值之一

### 2.8 查询未同步的打赏记录
- **端点**: `GET /api/v1/recharge/unsync`
- **功能**: 查询还未同步到财务服务的打赏记录（内部接口）
- **查询参数**:
  - `limit`: Integer (可选，最多查询1000条)
- **响应**: BaseResponse<List<RechargeDTO>>
- **验证**: limit在1-1000之间（如果提供）
- **权限**: 内部服务调用

### 2.9 标记打赏为已同步
- **端点**: `PATCH /api/v1/recharge/{rechargeId}/sync`
- **功能**: 标记打赏记录已同步到财务服务
- **路径参数**: rechargeId (Long)
- **查询参数**:
  - `settlementId`: Long (必填，结算ID)
- **响应**: BaseResponse<Void>
- **验证**: rechargeId > 0, settlementId > 0
- **权限**: 内部服务调用

---

## 3. 测试接口（Test - 测试用）

### 3.1 查询同步队列状态
- **端点**: `GET /api/v1/test/sync/queue/status`
- **功能**: 查询待同步到财务服务的打赏记录数（测试用）
- **响应**: ApiResponse<Map<String, Object>>
  - `queueSize`: 队列中的记录数
  - `description`: 说明文本
- **权限**: 仅测试环境可用

### 3.2 手动触发同步
- **端点**: `POST /api/v1/test/sync/trigger`
- **功能**: 手动触发打赏数据同步到财务服务（测试用）
- **查询参数**:
  - `batchSize`: Integer (默认100，单次同步的批次大小)
- **响应**: ApiResponse<Map<String, Object>>
  - `beforeQueueSize`: 同步前队列大小
  - `afterQueueSize`: 同步后队列大小
  - `syncedCount`: 本次同步的记录数
  - `status`: 同步状态 (success/failed)
- **权限**: 仅测试环境可用

---

## 数据同步说明

### 同步流程
1. 观众服务创建打赏记录 (POST /api/v1/recharge)
2. 记录异步推送到消息队列（RabbitMQ/Kafka）
3. 通过定时任务或事件触发同步
4. 调用财务服务的 `/internal/sync/recharges` 接口
5. 财务服务处理后，观众服务标记为已同步 (PATCH .../sync)

### 同步幂等性
- 通过traceId确保单次打赏只计算一次
- 通过settlementId关联到财务系统的结算记录
- 支持重新同步（已同步记录可重新推送）

---

## 数据验证规则

- **ID参数**: 必须为正整数 (> 0)
- **金额参数**: BigDecimal格式，精确到两位小数
- **字符串参数**: 需验证长度限制
  - nickname: 2-30字符
  - keyword: 1-50字符
  - reason: 0-200字符
  - traceId: 1-64字符
- **分页参数**: page >= 1, 1 <= size <= 100
- **时间参数**: ISO 8601格式

---

## 消费等级说明

| 等级 | 描述 | 打赏范围 |
|------|------|---------|
| 0 | 低消费 | 0-100元 |
| 1 | 中消费 | 100-1000元 |
| 2 | 高消费 | 1000元以上 |

---

## 错误处理

所有接口都返回统一的响应格式：

```json
{
  "code": 200,
  "message": "观众创建成功",
  "data": {}
}
```

**常见错误码**:
- 200: 成功
- 400: 请求参数错误
- 401: 未授权
- 403: 禁止访问
- 404: 资源不存在
- 409: 冲突（如重复操作）
- 500: 服务器错误

---

## 性能优化

1. 打赏记录列表支持分页查询，默认每页20条
2. 搜索功能建议使用Elasticsearch或数据库全文索引
3. 消费统计可以缓存，更新频率5分钟
4. TOP 10查询建议使用Redis缓存
5. 同步队列使用消息中间件确保可靠性

---

## 注意事项

1. 打赏创建支持幂等性，同一traceId在60秒内只计算一次
2. 数据同步到财务服务可能存在5秒延迟
3. 删除观众账户前需要确认无未结算的打赏
4. 禁用账户不影响历史打赏数据的展示
5. 游客观众的数据会定期清理（超过7天的游客账户）

