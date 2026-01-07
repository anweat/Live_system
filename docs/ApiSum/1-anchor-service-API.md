# 主播服务（Anchor Service）API 接口文档

## 服务基本信息
- **服务名称**: anchor-service
- **服务端口**: 8081
- **基础路径**: /anchor/api/v1
- **版本**: 1.0.0
- **功能描述**: 提供主播、直播间、打赏记录、提现申请等核心功能

---

## 1. 主播管理（Anchor Management）

### 1.1 创建主播
- **端点**: `POST /api/v1/anchors`
- **功能**: 创建新主播账户，自动创建对应的直播间
- **请求体**: AnchorDTO (nickname, realName, gender, signature等)
- **响应**: BaseResponse<AnchorDTO>
- **注解**: @Log("创建主播")

### 1.2 查询主播信息
- **端点**: `GET /api/v1/anchors/{anchorId}`
- **功能**: 根据ID查询主播详细信息
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<AnchorDTO>
- **验证**: anchorId > 0

### 1.3 更新主播信息
- **端点**: `PUT /api/v1/anchors/{anchorId}`
- **功能**: 更新主播的基本信息
- **路径参数**: anchorId (Long)
- **请求体**: AnchorDTO (部分字段)
- **响应**: BaseResponse<AnchorDTO>

### 1.4 查询主播列表（分页）
- **端点**: `GET /api/v1/anchors`
- **功能**: 分页查询所有主播，支持排序
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
  - `orderBy`: String (排序字段，可选，如: fanCount, totalEarnings)
  - `direction`: String (排序方向，默认desc: asc/desc)
- **响应**: BaseResponse<List<AnchorDTO>>

### 1.5 查询TOP主播（按粉丝数）
- **端点**: `GET /api/v1/anchors/top/fans`
- **功能**: 查询粉丝数最多的主播排行榜
- **查询参数**:
  - `limit`: Integer (默认10，最小1)
- **响应**: BaseResponse<List<AnchorDTO>>

### 1.6 查询TOP主播（按收益）
- **端点**: `GET /api/v1/anchors/top/earnings`
- **功能**: 查询收益最高的主播排行榜
- **查询参数**:
  - `limit`: Integer (默认10，最小1)
- **响应**: BaseResponse<List<AnchorDTO>>

### 1.7 更新主播统计数据
- **端点**: `PATCH /api/v1/anchors/{anchorId}/stats`
- **功能**: 增量更新主播的粉丝数、点赞数等统计数据
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `fanCountDelta`: Long (可选，粉丝数增量)
  - `likeCountDelta`: Long (可选，点赞数增量)
- **响应**: BaseResponse<Void>

### 1.8 更新主播累计收益
- **端点**: `PATCH /api/v1/anchors/{anchorId}/earnings`
- **功能**: 增量更新主播的累计收益金额
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `amount`: BigDecimal (必填，收益增量，不能为0)
- **响应**: BaseResponse<Void>

### 1.9 查询主播可提取余额
- **端点**: `GET /api/v1/anchors/{anchorId}/available-amount`
- **功能**: 查询主播当前可提取的金额
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<BigDecimal>

### 1.10 更新主播可提取余额
- **端点**: `PATCH /api/v1/anchors/{anchorId}/available-amount`
- **功能**: 增量更新主播的可提取余额
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `amount`: BigDecimal (必填，金额增量，不能为0)
- **响应**: BaseResponse<Void>

---

## 2. 直播间管理（Live Room Management）

### 2.1 查询直播间信息
- **端点**: `GET /api/v1/live-rooms/{liveRoomId}`
- **功能**: 查询直播间详细信息，包括实时在线人数
- **路径参数**: liveRoomId (Long)
- **响应**: BaseResponse<LiveRoomDTO>

### 2.2 根据主播ID查询直播间
- **端点**: `GET /api/v1/live-rooms/anchor/{anchorId}`
- **功能**: 通过主播ID查询其对应的直播间
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<LiveRoomDTO>

### 2.3 开启直播
- **端点**: `POST /api/v1/live-rooms/{liveRoomId}/start`
- **功能**: 主播开启直播
- **路径参数**: liveRoomId (Long)
- **查询参数**:
  - `streamUrl`: String (必填，直播流URL)
  - `coverUrl`: String (可选，直播间封面URL)
- **响应**: BaseResponse<LiveRoomDTO>

### 2.4 结束直播
- **端点**: `POST /api/v1/live-rooms/{liveRoomId}/end`
- **功能**: 主播结束直播
- **路径参数**: liveRoomId (Long)
- **响应**: BaseResponse<LiveRoomDTO>

### 2.5 更新直播间实时数据
- **端点**: `PATCH /api/v1/live-rooms/{liveRoomId}/realtime`
- **功能**: 增量更新直播间的实时数据（在线人数、收益等）
- **路径参数**: liveRoomId (Long)
- **查询参数**:
  - `viewersDelta`: Long (可选，在线人数增量)
  - `earningsDelta`: BigDecimal (可选，收益增量)
- **响应**: BaseResponse<Void>

### 2.6 查询正在直播的直播间
- **端点**: `GET /api/v1/live-rooms/live`
- **功能**: 分页查询所有正在进行的直播间
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
- **响应**: BaseResponse<List<LiveRoomDTO>>

### 2.7 按分类查询直播间
- **端点**: `GET /api/v1/live-rooms/category/{category}`
- **功能**: 按分类（如：娱乐、游戏、运动等）分页查询直播间
- **路径参数**: category (String)
- **查询参数**:
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
- **响应**: BaseResponse<List<LiveRoomDTO>>

### 2.8 更新直播间信息
- **端点**: `PUT /api/v1/live-rooms/{liveRoomId}`
- **功能**: 更新直播间的基本信息
- **路径参数**: liveRoomId (Long)
- **请求体**: LiveRoomDTO
- **响应**: BaseResponse<LiveRoomDTO>

---

## 3. 打赏记录查询（Recharge Records）

### 3.1 查询主播的打赏记录
- **端点**: `GET /api/v1/recharge/anchor/{anchorId}`
- **功能**: 分页查询指定主播收到的所有打赏记录
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (可选，开始时间)
  - `endTime`: LocalDateTime (可选，结束时间)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
- **响应**: BaseResponse<Object> (分页打赏记录)

### 3.2 查询直播间的打赏记录
- **端点**: `GET /api/v1/recharge/live-room/{liveRoomId}`
- **功能**: 分页查询指定直播间的所有打赏记录
- **路径参数**: liveRoomId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (可选，开始时间)
  - `endTime`: LocalDateTime (可选，结束时间)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
- **响应**: BaseResponse<Object>

### 3.3 按traceId查询打赏记录
- **端点**: `GET /api/v1/recharge/trace/{traceId}`
- **功能**: 根据唯一标识traceId查询单条打赏记录
- **路径参数**: traceId (String)
- **响应**: BaseResponse<RechargeVO>

### 3.4 统计主播的打赏总额
- **端点**: `GET /api/v1/recharge/anchor/{anchorId}/total`
- **功能**: 统计指定时间范围内主播收到的打赏总额
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `startTime`: LocalDateTime (可选，开始时间)
  - `endTime`: LocalDateTime (可选，结束时间)
- **响应**: BaseResponse<Object> (总金额)

### 3.5 查询TOP10打赏观众
- **端点**: `GET /api/v1/recharge/anchor/{anchorId}/top10`
- **功能**: 查询主播的TOP 10打赏观众排行榜
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `period`: String (默认all，可选值: day/week/month/all)
- **响应**: BaseResponse<List<RechargeVO.Top10AudienceVO>>

---

## 4. 提现管理（Withdrawal）

### 4.1 主播申请提现
- **端点**: `POST /api/v1/withdrawal/apply`
- **功能**: 主播申请提现，支持银行卡和支付宝两种方式
- **查询参数**:
  - `anchorId`: Long (必填，主播ID)
  - `amount`: BigDecimal (必填，提现金额，范围1.00-99999.99)
  - `withdrawalType`: Integer (必填，提现方式：0=银行卡, 1=支付宝等)
  - `bankName`: String (条件必填，银行卡提现时必填)
  - `accountNumber`: String (必填，账号)
  - `accountHolder`: String (必填，账户持有人)
- **响应**: BaseResponse<WithdrawalVO>

### 4.2 查询提现记录
- **端点**: `GET /api/v1/withdrawal/list/{anchorId}`
- **功能**: 分页查询主播的提现记录
- **路径参数**: anchorId (Long)
- **查询参数**:
  - `status`: Integer (可选，提现状态)
  - `page`: Integer (默认1，最小1)
  - `size`: Integer (默认20，最小1)
- **响应**: BaseResponse<Object>

### 4.3 按traceId查询提现记录
- **端点**: `GET /api/v1/withdrawal/trace/{traceId}`
- **功能**: 根据traceId查询单条提现记录
- **路径参数**: traceId (String)
- **响应**: BaseResponse<WithdrawalVO>

---

## 5. 直播间实时数据（Live Room Realtime）

### 5.1 观众进入直播间
- **端点**: `POST /api/v1/live-rooms/realtime/viewer-enter`
- **功能**: 记录观众进入直播间，更新在线人数
- **查询参数**:
  - `liveRoomId`: Long (必填，直播间ID)
  - `audienceId`: Long (必填，观众ID)
- **响应**: BaseResponse<String>

### 5.2 观众离开直播间
- **端点**: `POST /api/v1/live-rooms/realtime/viewer-leave`
- **功能**: 记录观众离开直播间，更新在线人数
- **查询参数**:
  - `liveRoomId`: Long (必填，直播间ID)
  - `audienceId`: Long (必填，观众ID)
- **响应**: BaseResponse<String>

### 5.3 观众发送弹幕
- **端点**: `POST /api/v1/live-rooms/realtime/danmaku`
- **功能**: 处理观众发送的弹幕消息
- **查询参数**:
  - `liveRoomId`: Long (必填，直播间ID)
  - `audienceId`: Long (必填，观众ID)
  - `content`: String (必填，弹幕内容，最长500字符)
- **响应**: BaseResponse<String>

### 5.4 观众打赏（直播间端）
- **端点**: `POST /api/v1/live-rooms/realtime/reward`
- **功能**: 处理观众打赏，更新直播间实时收益
- **查询参数**:
  - `liveRoomId`: Long (必填，直播间ID)
  - `audienceId`: Long (必填，观众ID)
  - `amount`: BigDecimal (必填，打赏金额，最小0.01)
- **响应**: BaseResponse<String>

---

## 6. 分成比例查询（Commission Rate）

### 6.1 查询主播分成比例
- **端点**: `GET /api/v1/commission/{anchorId}/current`
- **功能**: 查询主播当前生效的分成比例
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<Map<String, Object>> (包含commissionRate等)

### 6.2 清除分成比例缓存
- **端点**: `DELETE /api/v1/commission/{anchorId}/cache`
- **功能**: 清除主播分成比例的缓存（管理接口）
- **路径参数**: anchorId (Long)
- **响应**: BaseResponse<String>
- **权限**: 需要管理员权限

---

## 错误处理

所有接口都返回统一的响应格式：

```json
{
  "code": 200,
  "message": "操作成功",
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

---

## 数据验证规则

- 所有ID参数必须为正整数
- 金额参数使用BigDecimal，保留两位小数
- 时间参数使用ISO 8601格式
- 分页参数: page >= 1, 1 <= size <= 100
- 字符串参数需要验证长度和特殊字符

---

## 注意事项

1. 所有涉及金额的操作需要财务服务的协同
2. 提现申请会通过异步任务处理
3. 统计数据有可能存在5分钟的延迟
4. 某些操作支持幂等性，通过traceId实现
5. 直播间在线人数是近似值，不是精确值

