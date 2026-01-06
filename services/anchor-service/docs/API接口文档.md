# 主播服务 API 接口文档

## 服务信息
- **服务名称**: anchor-service
- **服务端口**: 8081
- **上下文路径**: /anchor
- **基础路径**: /anchor/api/v1
- **版本**: 1.0.0

---

## 1. 主播管理 (Anchor Management)

### 1.1 创建主播
- **接口**: `POST /api/v1/anchors`
- **描述**: 创建新主播，自动创建对应的直播间
- **请求体**: AnchorDTO (nickname, realName, gender, signature等)
- **响应**: 主播信息

### 1.2 查询主播信息
- **接口**: `GET /api/v1/anchors/{anchorId}`
- **描述**: 根据ID查询主播详细信息
- **响应**: 主播完整信息

### 1.3 修改主播信息
- **接口**: `PUT /api/v1/anchors/{anchorId}`
- **描述**: 更新主播基本信息
- **请求体**: AnchorDTO (部分字段)
- **响应**: 更新后的主播信息

### 1.4 查询主播列表（分页）
- **接口**: `GET /api/v1/anchors`
- **描述**: 分页查询主播列表，支持排序
- **参数**:
  - `page`: 页码（默认1）
  - `size`: 每页数量（默认20）
  - `sortBy`: 排序字段（fanCount, likeCount, totalEarnings等）
  - `sortOrder`: 排序方向（asc, desc）
- **响应**: 分页主播列表

### 1.5 按认证状态查询主播
- **接口**: `GET /api/v1/anchors/verification-status/{status}`
- **描述**: 查询指定认证状态的主播列表
- **路径参数**: status (0-未认证, 1-已认证, 2-认证中)
- **查询参数**: page, size, sortBy, sortOrder
- **响应**: 分页主播列表

### 1.6 搜索主播
- **接口**: `GET /api/v1/anchors/search`
- **描述**: 按昵称、等级等条件搜索主播
- **参数**:
  - `nickname`: 昵称关键词
  - `anchorLevel`: 主播等级
  - `verificationStatus`: 认证状态
  - `minFanCount`: 最小粉丝数
  - `page`, `size`
- **响应**: 搜索结果列表

### 1.7 更新主播统计数据
- **接口**: `PATCH /api/v1/anchors/{anchorId}/stats`
- **描述**: 增量更新粉丝数和点赞数
- **参数**:
  - `fanCountDelta`: 粉丝数增量
  - `likeCountDelta`: 点赞数增量
- **响应**: 成功消息

### 1.8 更新主播累计收益
- **接口**: `PATCH /api/v1/anchors/{anchorId}/earnings`
- **描述**: 增量更新主播累计收益
- **参数**: `amount` - 收益增量
- **响应**: 成功消息

### 1.9 查询主播可提取余额
- **接口**: `GET /api/v1/anchors/{anchorId}/available-amount`
- **描述**: 查询主播当前可提取金额
- **响应**: BigDecimal 金额

### 1.10 更新主播可提取余额
- **接口**: `PATCH /api/v1/anchors/{anchorId}/available-amount`
- **描述**: 增量更新可提取余额
- **参数**: `amount` - 金额增量
- **响应**: 成功消息

### 1.11 查询主播总数
- **接口**: `GET /api/v1/anchors/count`
- **描述**: 统计启用状态的主播总数
- **响应**: Long 数量

### 1.12 按认证状态统计主播数量
- **接口**: `GET /api/v1/anchors/count/verification-status/{status}`
- **描述**: 统计指定认证状态的主播数量
- **响应**: Long 数量

---

## 2. 直播间管理 (Live Room Management)

### 2.1 查询直播间信息
- **接口**: `GET /api/v1/live-rooms/{liveRoomId}`
- **描述**: 根据ID查询直播间详细信息
- **响应**: LiveRoomVO (包含实时在线人数)

### 2.2 按主播ID查询直播间
- **接口**: `GET /api/v1/live-rooms/anchor/{anchorId}`
- **描述**: 查询主播对应的直播间
- **响应**: LiveRoomVO

### 2.3 开启直播
- **接口**: `POST /api/v1/live-rooms/{liveRoomId}/start`
- **描述**: 开播，重置实时数据
- **参数**:
  - `streamUrl`: 直播流地址
  - `coverUrl`: 封面图URL（可选）
- **响应**: 直播间信息

### 2.4 结束直播
- **接口**: `POST /api/v1/live-rooms/{liveRoomId}/end`
- **描述**: 关播，累计主播收益
- **响应**: 直播间信息

### 2.5 更新直播间实时数据
- **接口**: `PATCH /api/v1/live-rooms/{liveRoomId}/realtime`
- **描述**: 批量更新实时统计数据
- **参数**:
  - `currentViewerCount`: 当前观众数
  - `totalViewers`: 累计观看人次
  - `totalEarnings`: 累计收益
- **响应**: 成功消息

### 2.6 查询正在直播的直播间
- **接口**: `GET /api/v1/live-rooms/live`
- **描述**: 分页查询所有正在直播的直播间
- **参数**: page, size, sortBy, sortOrder
- **响应**: 分页直播间列表

### 2.7 按分类查询直播间
- **接口**: `GET /api/v1/live-rooms/category/{category}`
- **描述**: 查询指定分类的直播间
- **参数**: category, page, size, sortBy, sortOrder
- **响应**: 分页直播间列表

### 2.8 更新直播间信息
- **接口**: `PUT /api/v1/live-rooms/{liveRoomId}`
- **描述**: 更新直播间基本信息（名称、描述、分类等）
- **请求体**: LiveRoomDTO
- **响应**: 更新后的直播间信息

### 2.9 统计正在直播的直播间数量
- **接口**: `GET /api/v1/live-rooms/count/live`
- **描述**: 统计当前直播中的直播间总数
- **响应**: Long 数量

### 2.10 按分类统计直播间数量
- **接口**: `GET /api/v1/live-rooms/count/category/{category}`
- **描述**: 统计指定分类的直播间数量
- **响应**: Long 数量

---

## 3. 直播间实时数据 (Live Room Realtime)

### 3.1 观众进入直播间
- **接口**: `POST /api/v1/live-rooms/realtime/viewer-enter`
- **描述**: 记录观众进入，递增在线人数和累计观看人次
- **参数**:
  - `liveRoomId`: 直播间ID
  - `audienceId`: 观众ID
- **响应**: 成功消息

### 3.2 观众离开直播间
- **接口**: `POST /api/v1/live-rooms/realtime/viewer-leave`
- **描述**: 记录观众离开，递减在线人数
- **参数**:
  - `liveRoomId`: 直播间ID
  - `audienceId`: 观众ID
- **响应**: 成功消息

### 3.3 观众发送弹幕
- **接口**: `POST /api/v1/live-rooms/realtime/danmaku`
- **描述**: 保存弹幕到message表，递增弹幕计数
- **参数**:
  - `liveRoomId`: 直播间ID
  - `audienceId`: 观众ID
  - `content`: 弹幕内容（最大500字符）
- **响应**: 成功消息

### 3.4 观众打赏
- **接口**: `POST /api/v1/live-rooms/realtime/reward`
- **描述**: 记录打赏，更新直播间收益（不更新主播余额）
- **参数**:
  - `liveRoomId`: 直播间ID
  - `audienceId`: 观众ID
  - `amount`: 打赏金额（0.01-99999.99）
- **响应**: 成功消息

### 3.5 查询直播间实时数据
- **接口**: `GET /api/v1/live-rooms/realtime/{liveRoomId}`
- **描述**: 查询直播间当前实时统计数据
- **响应**: LiveRoomRealtimeVO (当前在线人数、累计观看、累计收益、直播时长等)

---

## 4. 分成比例管理 (Commission Rate)

### 4.1 查询当前分成比例
- **接口**: `GET /api/v1/commission-rate/{anchorId}/current`
- **描述**: 查询主播当前生效的分成比例（从财务服务获取，带缓存）
- **响应**: CommissionRateVO (比例、生效时间、过期时间等)

### 4.2 清除分成比例缓存
- **接口**: `DELETE /api/v1/commission-rate/{anchorId}/cache`
- **描述**: 手动清除主播分成比例缓存
- **响应**: 成功消息

---

## 5. 提现管理 (Withdrawal)

### 5.1 申请提现
- **接口**: `POST /api/v1/withdrawal/apply`
- **描述**: 主播申请提现，调用财务服务处理
- **参数**:
  - `anchorId`: 主播ID
  - `amount`: 提现金额（1.00-99999.99）
  - `withdrawalType`: 提现方式（0-银行卡, 1-支付宝, 2-微信）
  - `bankName`: 开户行（银行卡提现必填）
  - `accountNumber`: 账号
  - `accountHolder`: 账户持有人
- **响应**: WithdrawalVO (提现记录信息)

### 5.2 查询提现记录
- **接口**: `GET /api/v1/withdrawal/list/{anchorId}`
- **描述**: 分页查询主播的提现记录
- **参数**:
  - `status`: 提现状态（可选，0-待审核, 1-处理中, 2-成功, 3-失败）
  - `page`: 页码
  - `size`: 每页数量
- **响应**: 分页提现记录列表

### 5.3 按traceId查询提现记录
- **接口**: `GET /api/v1/withdrawal/trace/{traceId}`
- **描述**: 根据链路追踪ID查询提现记录
- **响应**: WithdrawalVO

---

## 6. 打赏记录查询 (Recharge Records)

### 6.1 查询主播的打赏记录
- **接口**: `GET /api/v1/recharge/anchor/{anchorId}`
- **描述**: 分页查询主播收到的打赏记录
- **参数**:
  - `startTime`: 开始时间（可选）
  - `endTime`: 结束时间（可选）
  - `page`: 页码
  - `size`: 每页数量
- **响应**: 分页打赏记录列表

### 6.2 查询直播间的打赏记录
- **接口**: `GET /api/v1/recharge/live-room/{liveRoomId}`
- **描述**: 分页查询直播间的打赏记录
- **参数**: startTime, endTime, page, size
- **响应**: 分页打赏记录列表

### 6.3 按traceId查询打赏记录
- **接口**: `GET /api/v1/recharge/trace/{traceId}`
- **描述**: 根据链路追踪ID查询打赏记录
- **响应**: RechargeVO

### 6.4 统计主播打赏总额
- **接口**: `GET /api/v1/recharge/anchor/{anchorId}/total`
- **描述**: 统计主播在指定时间段的打赏总额
- **参数**: startTime, endTime（可选）
- **响应**: RechargeSummaryVO (总额、次数、观众数等)

### 6.5 查询TOP10打赏观众
- **接口**: `GET /api/v1/recharge/anchor/{anchorId}/top10`
- **描述**: 查询主播的TOP10打赏观众（带Redis缓存）
- **参数**: `period` - 统计周期（day, week, month, all）
- **响应**: List<Top10AudienceVO> (排名、观众信息、打赏总额、次数等)

---

## 通用响应格式

### 成功响应
```json
{
  "code": 0,
  "message": "操作成功",
  "data": { ... }
}
```

### 错误响应
```json
{
  "code": 错误码,
  "message": "错误描述",
  "data": null
}
```

### 分页响应
```json
{
  "code": 0,
  "message": "查询成功",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 10,
    "number": 0,
    "size": 10,
    "first": true,
    "last": false
  }
}
```

---

## 错误码说明

| 错误码 | 说明 |
|-------|------|
| 0 | 成功 |
| 400 | 请求参数错误 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 503 | 服务不可用 |

---

## 接口统计

| 模块 | 接口数量 |
|-----|---------|
| 主播管理 | 12 |
| 直播间管理 | 10 |
| 直播间实时数据 | 5 |
| 分成比例管理 | 2 |
| 提现管理 | 3 |
| 打赏记录查询 | 5 |
| **总计** | **37** |

---

## 依赖的外部服务

1. **finance-service (财务服务)**
   - 查询分成比例
   - 提现申请处理
   - 提现记录查询

2. **audience-service (观众服务)**
   - 查询打赏记录
   - 统计打赏数据
   - TOP10观众统计

---

## 缓存策略

| 缓存键 | TTL | 说明 |
|-------|-----|------|
| `anchor:{anchorId}` | 30分钟 | 主播基础信息 |
| `live_room:{liveRoomId}` | 10分钟 | 直播间信息 |
| `anchor:commission:{anchorId}` | 2小时 | 分成比例 |
| `anchor:top10:{anchorId}:day` | 2小时 | 日榜TOP10 |
| `anchor:top10:{anchorId}:week` | 12小时 | 周榜TOP10 |
| `anchor:top10:{anchorId}:month` | 24小时 | 月榜TOP10 |
| `anchor:top10:{anchorId}:all` | 48小时 | 总榜TOP10 |

---

## Redis实时数据键

| 键 | 说明 | 过期时间 |
|---|------|---------|
| `live:room:viewers:{id}` | 当前在线人数 | 24小时 |
| `live:room:total_viewers:{id}` | 累计观看人次（增量） | 24小时 |
| `live:room:earnings:{id}` | 累计收益（增量） | 24小时 |
| `live:room:message_count:{id}` | 弹幕数（增量） | 24小时 |
| `live:room:recharge_count:{id}` | 打赏次数（增量） | 24小时 |
| `live:room:update_counter:{id}` | 操作计数器 | 24小时 |

---

**文档版本**: v1.0  
**最后更新**: 2026-01-02  
**维护者**: Team
