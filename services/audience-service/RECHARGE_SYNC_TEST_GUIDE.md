# 观众服务打赏与同步测试指南

## ⚠️ 重要术语说明

**请注意区分以下概念：**

| 中文 | 英文 | 说明 | 代码中的体现 |
|------|------|------|------------|
| **充值** | Top-up / Deposit | 观众往自己钱包充钱，**增加余额** | `topUpBalance()` |
| **打赏** | Reward / Tip / Gift | 观众给主播送钱，**扣除余额** | `createRecharge()` |

**本项目中的命名：**
- `Recharge` 类 = **打赏记录**（不是充值记录！）
- `recharge` 表 = **打赏明细记录表**
- `rechargeAmount` = **打赏金额**（观众给主播的金额）
- `topUpBalance()` = **充值方法**（给观众钱包充钱）

## 概述

观众服务现已实现：
1. ✅ **模拟支付处理**：虚拟钱包系统，余额检查和扣除
2. ✅ **内存同步队列**：将打赏记录累计到内存队列
3. ✅ **批量同步**：每5分钟自动同步到财务服务
4. ✅ **幂等性保证**：基于traceId的幂等性控制

## 核心功能

### 1. 打赏流程（观众给主播送钱）

```
用户发起打赏请求
    ↓
参数验证 + traceId幂等性检查
    ↓
虚拟钱包余额检查（Redis）
    ↓
余额扣除（模拟支付打赏）
    ↓
保存打赏记录到DB1
    ↓
加入内存同步队列
    ↓
返回success给用户（<200ms）
```

### 2. 批量同步流程

```
定时任务（每5分钟）
    ↓
从内存队列获取打赏记录
    ↓
构建BatchRechargeDTO
    ↓
调用财务服务API
    ↓
财务服务接收并持久化到DB2
    ↓
同步完成
```

### 3. 虚拟钱包系统

- **初始余额**：1000元（首次打赏时自动初始化）
- **存储位置**：Redis
- **余额Key**：`audience:wallet:balance:{audienceId}`
- **虚拟币Key**：`audience:wallet:coin:{audienceId}`

## API测试

### 1. 充值虚拟余额（给观众钱包充钱）

```bash
# 为观众1001充值500元
curl -X POST "http://localhost:8081/api/v1/test/wallet/topup?audienceId=1001&amount=500"

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "audienceId": 1001,
    "topUpAmount": 500,
    "newBalance": 1500,
    "description": "充值成功（给观众钱包充钱）"
  }
}
```

### 2. 查询虚拟余额

```bash
# 查询观众1001的余额
curl "http://localhost:8081/api/v1/test/wallet/balance/1001"

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "audienceId": 1001,
    "balance": 1500,
    "description": "观众钱包余额（可用于打赏主播）"
  }
}
```

### 3. 创建打赏记录（观众给主播送钱）

```bash
# 观众1001给主播2001打赏100元
curl -X POST "http://localhost:8081/api/v1/recharge" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 3001,
    "anchorId": 2001,
    "anchorName": "主播张三",
    "audienceId": 1001,
    "audienceNickname": "观众李四",
    "rechargeAmount": 100.00,
    "rechargeType": 0,
    "message": "支持主播！"
  }'

# 响应示例（成功）
{
  "code": 200,
  "message": "打赏成功",
  "data": {
    "rechargeId": 1234567890,
    "traceId": "TRC-20260102123045-abc123",
    "anchorId": 2001,
    "audienceId": 1001,
    "rechargeAmount": 100.00,
    "status": 0
  }
}

# 响应示例（余额不足）
{
  "code": 40001,
  "message": "余额不足，无法完成打赏"
}
```

### 4. 查询同步队列状态

```bash
# 查询待同步的打赏记录数
curl "http://localhost:8081/api/v1/test/sync/queue/status"

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "queueSize": 15,
    "description": "待同步到财务服务的打赏记录数"
  }
}
```

### 5. 手动触发同步

```bash
# 手动触发同步（批量大小100）
curl -X POST "http://localhost:8081/api/v1/test/sync/trigger?batchSize=100"

# 响应示例
{
  "code": 200,
  "message": "success",
  "data": {
    "beforeQueueSize": 15,
    "afterQueueSize": 0,
    "syncedCount": 15,
    "status": "success"
  }
}
```

## 完整测试场景

### 场景1：单笔打赏测试

```bash（给观众钱包充钱）
curl -X POST "http://localhost:8081/api/v1/test/wallet/topup?audienceId=1001&amount=1000"

# 2. 发起打赏（观众给主播送钱）
curl -X POST "http://localhost:8081/api/v1/recharge" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 3001,
    "anchorId": 2001,
    "anchorName": "主播A",
    "audienceId": 1001,
    "audienceNickname": "观众B",
    "rechargeAmount": 50.00,
    "rechargeType": 0,
    "message": "666"
  }'

# 3. 检查余额（应该减少50，因为打赏消费了50元）
curl "http://localhost:8081/api/v1/test/wallet/balance/1001"

# 4. 检查同步队列（应该有1条打赏记录）
curl "http://localhost:8081/api/v1/test/sync/queue/status"
```

### 场景2：批量打赏测试

创建测试脚本 `test-batch-recharge.sh`：

```bash
#!/bin/bash

# 给观众1001-1010各充值2000元（给钱包充钱）
for i in {1001..1010}
do
  curl -X POST "http://localhost:8081/api/v1/test/wallet/topup?audienceId=$i&amount=2000"
  echo ""
done

# 模拟10个观众各打赏10次（共100次打赏 - 观众给主播送钱
# 模拟10个观众各打赏10次（共100次打赏）
for audience in {1001..1010}
do
  for j in {1..10}
  do
    curl -X POST "http://localhost:8081/api/v1/recharge" \
      -H "Content-Type: application/json" \
      -d "{
        \"liveRoomId\": 3001,
        \"anchorId\": 2001,
        \"anchorName\": \"主播A\",
        \"audienceId\": $audience,
        \"audienceNickname\": \"观众$audience\",
        \"rechargeAmount\": $((RANDOM % 100 + 10)),
        \"rechargeType\": 0,
        \"message\": \"支持！\"
      }"
    echo ""
    sleep 0.1  # 避免过快
  done
done

# 检查同步队列
curl "http://localhost:8081/api/v1/test/sync/queue/status"
```

运行：
```bash
chmod +x test-batch-recharge.sh
./test-batch-recharge.sh
```

### 场景3：幂等性测试

```bash
# 使用相同的traceId重复提交
TRACE_ID="TEST-IDEMPOTENT-001"

# 第一次提交（应该成功）
curl -X POST "http://localhost:8081/api/v1/recharge" \
  -H "Content-Type: application/json" \
  -d "{
    \"traceId\": \"$TRACE_ID\",
    \"liveRoomId\": 3001,
    \"anchorId\": 2001,
    \"anchorName\": \"主播A\",
    \"audienceId\": 1001,
    \"audienceNickname\": \"观众B\",
    \"rechargeAmount\": 100.00,
    \"rechargeType\": 0
  }"

# 第二次提交（应该被拒绝）
curl -X POST "http://localhost:8081/api/v1/recharge" \
  -H "Content-Type: application/json" \
  -d "{
    \"traceId\": \"$TRACE_ID\",
    \"liveRoomId\": 3001,
    \"anchorId\": 2001,
    \"anchorName\": \"主播A\",
    \"audienceId\": 1001,
    \"audienceNickname\": \"观众B\",
    \"rechargeAmount\": 100.00,
    \"rechargeType\": 0
  }"

# 预期响应：
# {
#   "code": 40002,
#   "message": "该打赏请求已处理，请勿重复提交"
# }
```

### 场景4：余额不足测试

```bash
# 1. 初始化小额余额（只充值10元）
curl -X POST "http://localhost:8081/api/v1/test/wallet/topup?audienceId=9999&amount=10"

# 2. 尝试打赏超过余额的金额（应该失败）
curl -X POST "http://localhost:8081/api/v1/recharge" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 3001,
    "anchorId": 2001,
    "anchorName": "主播A",
    "audienceId": 9999,
    "audienceNickname": "观众X",
    "rechargeAmount": 100.00,
    "rechargeType": 0
  }'

# 预期响应：
# {
#   "code": 40003,
#   "message": "余额不足，无法完成打赏"
# }
```

### 场景5：自动同步测试

```bash
# 1. 创建一些打赏记录
for i in {1..20}
do
  curl -X POST "http://localhost:8081/api/v1/recharge" \
    -H "Content-Type: application/json" \
    -d "{
      \"liveRoomId\": 3001,
      \"anchorId\": 2001,
      \"anchorName\": \"主播A\",
      \"audienceId\": 1001,
      \"audienceNickname\": \"观众B\",
      \"rechargeAmount\": 10.00,
      \"rechargeType\": 0
    }"
  sleep 0.5
done

# 2. 查看同步队列
curl "http://localhost:8081/api/v1/test/sync/queue/status"

# 3. 等待5分钟（定时任务自动同步）或手动触发同步
curl -X POST "http://localhost:8081/api/v1/test/sync/trigger?batchSize=100"

# 4. 再次查看同步队列（应该为0）
curl "http://localhost:8081/api/v1/test/sync/queue/status"

# 5. 在财务服务检查是否收到数据
curl "http://localhost:8083/api/finance/statistics/anchor/revenue/2001?startTime=2026-01-02T00:00:00&endTime=2026-01-02T23:59:59"
```

## 日志监控

### 关键日志

1. **打赏处理日志**
```
[INFO] RechargeService.createRecharge - 开始处理打赏请求: audienceId=1001, amount=100.00
[INFO] RechargeService.checkAndDeductBalance - 支付成功：audienceId=1001, 扣除金额=100.00, 剩余余额=900.00
[INFO] RechargeService.createRecharge - 打赏记录创建成功: rechargeId=xxx, traceId=xxx, 耗时=45ms
[DEBUG] RechargeService.createRecharge - 打赏记录已加入同步队列，当前队列大小: 15
```

2. **同步处理日志**
```
[INFO] RechargeDataSyncTask.syncRechargeData - 开始同步打赏数据到财务服务
[INFO] SyncService.syncRechargeDataToFinance - 从同步队列获取到 50 条待同步记录
[INFO] SyncService.syncRechargeDataToFinance - 打赏数据同步成功，batchId=BATCH-xxx, 共50条记录，总金额=5000.00
```

3. **财务服务接收日志**
```
[INFO] SyncReceiveService.receiveBatchRecharges - 开始接收批量打赏数据，batchId: BATCH-xxx, count: 50
[INFO] SyncReceiveService.receiveBatchRecharges - 批量插入数据库成功，记录数: 50
[INFO] SyncReceiveService.receiveBatchRecharges - 批量打赏数据接收完成，batchId: BATCH-xxx, 新增: 50, 重复: 0, 总金额: 5000.00, 耗时: 156ms
```

## 性能指标

| 操作 | 目标响应时间 | 说明 |
|------|------------|------|
| 单笔打赏 | < 200ms | 包括余额检查、DB写入、队列操作 |
| 批量同步（100条） | < 2s | 网络调用 + 财务服务处理 |
| 余额查询 | < 50ms | Redis查询 |
| 同步队列查询 | < 10ms | 内存操作 |

## 故障模拟

### 1. 财务服务不可用

```bash
# 停止财务服务
# 然后发起打赏和同步

# 打赏仍然成功（存入DB1和队列）
curl -X POST "http://localhost:8081/api/v1/recharge" ...

# 同步会失败，但记录保留在队列中
curl -X POST "http://localhost:8081/api/v1/test/sync/trigger"

# 预期：返回错误，但队列数据不丢失
```

### 2. Redis不可用

```bash
# 停止Redis服务
# 打赏会失败（无法检查余额）

# 预期：返回"系统错误"，不会产生脏数据
```

## 数据验证

### 验证数据一致性

```sql
-- 在DB1查询打赏记录总数
SELECT COUNT(*) as db1_count, SUM(recharge_amount) as db1_amount
FROM recharge
WHERE anchor_id = 2001
  AND DATE(recharge_time) = '2026-01-02';

-- 在DB2查询财务记录总数
SELECT COUNT(*) as db2_count, SUM(recharge_amount) as db2_amount
FROM recharge_record
WHERE anchor_id = 2001
  AND DATE(recharge_time) = '2026-01-02';

-- 结果应该一致
```

## 常见问题

### Q1: 打赏成功但同步队列没有增加？
A: 检查RechargeService中的`syncQueue.offer()`是否被正常调用，查看日志。

### Q2: 同步一直失败？
A: 
- 检查财务服务是否正常运行
- 检查Consul服务发现是否正常
- 检查网络连接
- 查看FinanceServiceClientFallback的降级日志

### Q3: 余额检查失败？
A: 检查Redis服务是否正常，查看余额Key是否存在。

### Q4: 如何重置测试数据？
A: 
```bash
# 清空Redis中的钱包数据
redis-cli
> KEYS audience:wallet:*
> DEL audience:wallet:balance:1001 audience:wallet:coin:1001
```

## 总结

现在观众服务已经实现了完整的打赏处理流程：
1. ✅ 接收打赏请求并模拟支付（虚拟钱包 - 扣除观众余额）
2. ✅ 立即返回success（用户体验<200ms）
3. ✅ 将打赏累计到内存同步队列
4. ✅ 定时（每5分钟）批量同步到财务服务
5. ✅ 完整的幂等性保证
6. ✅ 详细的日志和监控

**核心概念澄清：**
- **充值（Top-up）**：观众往自己钱包充钱 → `topUpBalance()`
- **打赏（Reward）**：观众给主播送钱 → `createRecharge()`
- **`Recharge` 类**：在本项目中表示"打赏记录"，不是"充值记录"

测试愉快！🎉
