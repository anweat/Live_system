# 模拟数据服务（Mock Service）API 接口文档

## 服务基本信息
- **服务名称**: mock-service
- **服务端口**: 8087
- **基础路径**: /api/v1/mock 或 /api/v1/health
- **版本**: 2.0
- **功能描述**: 提供模拟数据生成、测试数据管理等功能，支持开发和测试环境的数据准备

---

## 1. 模拟数据管理（Mock Data Management）

### 1.1 查询模拟数据统计
- **端点**: `GET /api/v1/mock/data/statistics`
- **功能**: 获取已生成的模拟数据的统计信息
- **响应**: BaseResponse<MockDataStatistics>
  ```json
  {
    "code": 200,
    "message": "查询成功",
    "data": {
      "totalAnchors": 100,
      "totalAudiences": 1000,
      "totalLiveRooms": 50,
      "totalRecharges": 5000,
      "totalBatches": 10,
      "entityTypeCount": {
        "ANCHOR": 100,
        "AUDIENCE": 1000,
        "LIVE_ROOM": 50,
        "RECHARGE": 5000
      }
    }
  }
  ```
- **用途**: 了解模拟数据的规模

### 1.2 查询所有批次信息
- **端点**: `GET /api/v1/mock/data/batches`
- **功能**: 查询所有已创建的数据批次信息
- **响应**: BaseResponse<List<MockBatchInfo>>
  - 包含字段：batchId, batchType, createTime, status, dataCount等

### 1.3 根据批次ID查询批次信息
- **端点**: `GET /api/v1/mock/data/batch/{batchId}`
- **功能**: 查询指定批次的详细信息
- **路径参数**: batchId (String)
- **响应**: BaseResponse<MockBatchInfo>
  - 404: 批次不存在

### 1.4 根据批次类型查询
- **端点**: `GET /api/v1/mock/data/batches/type/{batchType}`
- **功能**: 查询指定类型的所有批次
- **路径参数**: batchType (String，如：ANCHOR/AUDIENCE/RECHARGE等)
- **响应**: BaseResponse<List<MockBatchInfo>>

---

## 2. 模拟主播生成（Mock Anchor Generator）

### 2.1 创建单个模拟主播
- **端点**: `POST /api/v1/mock/anchor/create`
- **功能**: 创建单个模拟主播账户和对应的直播间
- **请求体**: CreateMockAnchorRequest
  ```json
  {
    "nickname": "主播昵称",
    "realName": "真实姓名",
    "gender": "M/F",
    "signature": "个人签名",
    "category": "主播分类"
  }
  ```
- **响应**: BaseResponse<MockAnchorResult>
  ```json
  {
    "code": 200,
    "message": "创建模拟主播成功",
    "data": {
      "anchorId": 1001,
      "nickname": "主播昵称",
      "liveRoomId": 101,
      "createdTime": "2024-01-07T10:00:00"
    }
  }
  ```

### 2.2 批量创建模拟主播
- **端点**: `POST /api/v1/mock/anchor/batch-create`
- **功能**: 一次性批量创建多个模拟主播（异步任务）
- **查询参数**:
  - `count`: Integer (必填，创建数量，范围1-500)
- **响应**: BaseResponse<MockBatchResult>
  ```json
  {
    "code": 200,
    "message": "批量创建主播任务已启动",
    "data": {
      "batchId": "batch_anchor_202401071000",
      "count": 100,
      "status": "PROCESSING",
      "startTime": "2024-01-07T10:00:00"
    }
  }
  ```
- **用途**: 快速生成大量测试主播数据

---

## 3. 模拟观众生成（Mock Audience Generator）

### 3.1 创建单个模拟观众
- **端点**: `POST /api/v1/mock/audience/create`
- **功能**: 创建单个模拟观众账户
- **请求体**: CreateMockAudienceRequest
  ```json
  {
    "nickname": "观众昵称",
    "realName": "真实姓名",
    "gender": "M/F",
    "consumptionLevel": 0
  }
  ```
- **响应**: BaseResponse<MockAudienceResult>

### 3.2 批量创建模拟观众
- **端点**: `POST /api/v1/mock/audience/batch-create`
- **功能**: 批量创建模拟观众账户（异步任务）
- **查询参数**:
  - `count`: Integer (必填，创建数量，范围1-500)
- **响应**: BaseResponse<MockBatchResult>

---

## 4. 模拟打赏生成（Mock Recharge Generator）

### 4.1 创建单个模拟打赏
- **端点**: `POST /api/v1/mock/recharge/create`
- **功能**: 创建单个模拟打赏记录
- **请求体**: CreateMockRechargeRequest
  ```json
  {
    "anchorId": 1001,
    "audienceId": 2001,
    "amount": 50.00,
    "liveRoomId": 101
  }
  ```
- **响应**: BaseResponse<MockRechargeResult>

### 4.2 批量创建模拟打赏
- **端点**: `POST /api/v1/mock/recharge/batch-create`
- **功能**: 批量创建模拟打赏记录（异步任务）
- **查询参数**:
  - `count`: Integer (必填，创建数量，范围1-1000)
  - `anchorId`: Long (可选，指定主播)
  - `period`: String (可选，时间范围，如：today/week/month)
- **响应**: BaseResponse<MockBatchResult>

### 4.3 按时间段创建模拟打赏
- **端点**: `POST /api/v1/mock/recharge/batch-create-by-period`
- **功能**: 在指定时间范围内创建打赏记录（模拟历史数据）
- **查询参数**:
  - `startTime`: LocalDateTime (必填，开始时间)
  - `endTime`: LocalDateTime (必填，结束时间)
  - `count`: Integer (必填，总数量)
- **响应**: BaseResponse<MockBatchResult>

---

## 5. 模拟直播生成（Mock Live Room Generator）

### 5.1 创建单个模拟直播
- **端点**: `POST /api/v1/mock/liveroom/create`
- **功能**: 创建单个模拟直播间
- **请求体**: CreateMockLiveRoomRequest
  ```json
  {
    "anchorId": 1001,
    "title": "直播间标题",
    "category": "游戏",
    "description": "直播间描述"
  }
  ```
- **响应**: BaseResponse<MockLiveRoomResult>

### 5.2 模拟直播进行中
- **端点**: `POST /api/v1/mock/liveroom/{liveRoomId}/simulate`
- **功能**: 模拟直播进行中的实时数据更新
- **路径参数**: liveRoomId (Long)
- **查询参数**:
  - `duration`: Integer (直播持续时间，单位分钟)
  - `viewerIncrementRate`: Integer (观众增长速率，人/分钟)
  - `rechargeFrequency`: Integer (打赏频率，次/分钟)
- **响应**: BaseResponse<MockSimulationResult>
  - 开启一个后台任务模拟直播的实时数据

---

## 6. 模拟提现生成（Mock Withdrawal Generator）

### 6.1 创建单个模拟提现
- **端点**: `POST /api/v1/mock/withdrawal/create`
- **功能**: 创建单个模拟提现申请
- **请求体**: CreateMockWithdrawalRequest
  ```json
  {
    "anchorId": 1001,
    "amount": 100.00,
    "withdrawalType": 0,
    "accountNumber": "1234567890"
  }
  ```
- **响应**: BaseResponse<MockWithdrawalResult>

### 6.2 批量创建模拟提现
- **端点**: `POST /api/v1/mock/withdrawal/batch-create`
- **功能**: 批量创建模拟提现申请
- **查询参数**:
  - `count`: Integer (必填，创建数量，范围1-500)
- **响应**: BaseResponse<MockBatchResult>

---

## 7. 数据清理与重置（Data Cleanup）

### 7.1 清空所有模拟数据
- **端点**: `DELETE /api/v1/mock/data/clear`
- **功能**: 删除所有通过mock-service生成的模拟数据
- **查询参数**:
  - `confirm`: Boolean (必填，确认删除，必须为true)
- **响应**: BaseResponse<Map<String, Integer>>
  ```json
  {
    "code": 200,
    "message": "数据已清空",
    "data": {
      "deletedAnchors": 100,
      "deletedAudiences": 1000,
      "deletedRecharges": 5000
    }
  }
  ```
- **权限**: 需要管理员权限
- **警告**: 不可恢复，生产环境禁用

### 7.2 清空指定批次的数据
- **端点**: `DELETE /api/v1/mock/data/batch/{batchId}`
- **功能**: 删除指定批次生成的数据
- **路径参数**: batchId (String)
- **响应**: BaseResponse<Integer>
  - 返回: 删除的记录数

---

## 8. 健康检查（Health Check）

### 8.1 服务健康检查
- **端点**: `GET /api/v1/health`
- **功能**: 检查mock-service是否正常运行
- **响应**: BaseResponse<Map<String, Object>>
  ```json
  {
    "code": 200,
    "message": "服务正常",
    "data": {
      "service": "mock-service",
      "version": "2.0",
      "status": "UP",
      "timestamp": 1704067200000
    }
  }
  ```

---

## 模拟数据的特点

### 主播数据
- 自动生成随机昵称、头像URL
- 随机初始粉丝数、点赞数
- 随机初始余额

### 观众数据
- 自动生成随机昵称
- 随机消费等级（0/1/2）
- 可选游客模式

### 打赏数据
- 随机打赏金额（1-500元）
- 随机打赏时间（支持历史数据模拟）
- 自动关联主播和观众
- 自动生成traceId实现幂等性

### 直播模拟
- 模拟观众进出
- 模拟弹幕发送
- 模拟打赏产生
- 模拟收益积累

---

## 数据生成参数建议

### 开发环境
- 主播数: 10-50
- 观众数: 100-500
- 打赏记录: 1000-5000

### 测试环境
- 主播数: 100-500
- 观众数: 1000-5000
- 打赏记录: 10000-50000

### 性能测试环境
- 主播数: 1000+
- 观众数: 10000+
- 打赏记录: 100000+

---

## 批次任务状态

| 状态 | 描述 |
|------|------|
| PENDING | 待执行 |
| PROCESSING | 处理中 |
| COMPLETED | 已完成 |
| FAILED | 失败 |
| CANCELLED | 已取消 |

---

## 错误处理

**常见错误场景**:

1. **数量超出限制**
   - 代码: 400
   - 信息: "创建数量必须在1-500之间"

2. **资源不存在**
   - 代码: 404
   - 信息: "批次不存在"

3. **权限不足**
   - 代码: 403
   - 信息: "需要管理员权限"

4. **生成失败**
   - 代码: 500
   - 信息: "模拟数据生成失败: ..."

---

## 注意事项

1. 仅用于开发和测试环境，禁止在生产环境使用
2. 生成的数据与真实数据具有相同的结构和约束
3. 批量生成操作是异步的，需要轮询查询状态
4. 生成的模拟数据可能包含重复数据，这是正常的
5. 清空数据时需要显式确认，防止误操作
6. 建议定期清理模拟数据，避免影响真实数据分析
7. 模拟数据生成可能占用大量数据库资源，建议在非高峰期执行

---

## 与Docker集成

在Docker容器中使用：

```bash
# 健康检查
curl http://localhost:8087/api/v1/health

# 查询模拟数据统计
curl http://localhost:8087/api/v1/mock/data/statistics

# 批量创建100个主播
curl -X POST http://localhost:8087/api/v1/mock/anchor/batch-create?count=100
```

---

## 集成测试示例

```
1. 调用mock-service创建测试数据
2. 验证anchor-service能正确查询生成的主播
3. 验证audience-service能正确处理打赏
4. 验证finance-service能正确统计收入
5. 清空模拟数据
```

