# Mock Service 快速测试指南

## 🎯 快速开始（3分钟）

### 1. 启动服务

Windows:
```bash
cd services\mock-service
start.bat
```

Linux/Mac:
```bash
cd services/mock-service
chmod +x start.sh
./start.sh
```

### 2. 验证服务

访问: http://localhost:8090/mock

### 3. 快速测试场景

#### 场景1: 创建一个完整的测试环境

```bash
curl -X POST "http://localhost:8090/mock/api/quick/complete-scenario?anchorName=测试主播小美&botCount=20&simulationSeconds=180"
```

这将自动：
- ✅ 创建1个主播
- ✅ 创建1个直播间
- ✅ 创建20个Bot观众
- ✅ 模拟180秒的直播行为（进入、弹幕、打赏、离开）

#### 场景2: 批量创建Bot观众测试高并发

```bash
curl -X POST "http://localhost:8090/mock/api/audience/batch-create-bots" \
  -H "Content-Type: application/json" \
  -d '{
    "count": 100,
    "assignRandomTags": true,
    "assignConsumptionLevel": true,
    "malePercentage": 50,
    "minAge": 18,
    "maxAge": 45
  }'
```

#### 场景3: 单独启动行为模拟

```bash
curl -X POST "http://localhost:8090/mock/api/simulation/start" \
  -H "Content-Type: application/json" \
  -d '{
    "liveRoomId": 123456,
    "audienceCount": 30,
    "durationSeconds": 300,
    "simulateEnter": true,
    "simulateLeave": true,
    "simulateMessage": true,
    "simulateRecharge": true,
    "rechargeProbability": 25
  }'
```

## 📊 测试数据说明

### 生成的主播数据示例
```json
{
  "anchorId": 1704182400001,
  "anchorName": "李静",
  "gender": 0,
  "bio": "喜欢唱歌的主播",
  "avatarUrl": "https://api.dicebear.com/7.x/avatars/svg?seed=42",
  "tags": ["唱歌", "跳舞", "颜值"]
}
```

### 生成的Bot观众数据示例
```json
{
  "audienceId": 1704182400002,
  "nickname": "Bot_a3f2e1d4",
  "gender": 1,
  "age": 28,
  "isBot": true,
  "consumptionLevel": 1,
  "tags": ["游戏", "音乐", "美食"]
}
```

### 行为模拟输出示例
```json
{
  "taskId": "uuid-123-456",
  "liveRoomId": 123456,
  "createdBots": 20,
  "totalEnters": 20,
  "totalLeaves": 18,
  "totalMessages": 156,
  "totalRecharges": 12,
  "totalRechargeAmount": 856.50,
  "status": "COMPLETED"
}
```

## 🔧 常用测试组合

### 压力测试场景
```bash
# 创建500个Bot（最大限制）
curl -X POST "http://localhost:8090/mock/api/audience/batch-create-bots" \
  -H "Content-Type: application/json" \
  -d '{"count": 500, "assignRandomTags": true}'

# 模拟100个并发观众行为
curl -X POST "http://localhost:8090/mock/api/simulation/start" \
  -H "Content-Type: application/json" \
  -d '{"liveRoomId": 999, "audienceCount": 100, "durationSeconds": 600}'
```

### 功能测试场景
```bash
# 创建特定属性的观众
curl -X POST "http://localhost:8090/mock/api/audience/create" \
  -H "Content-Type: application/json" \
  -d '{
    "nickname": "测试观众001",
    "gender": 1,
    "age": 25,
    "isBot": false,
    "consumptionLevel": 2,
    "tags": ["游戏", "电竞"]
  }'
```

### 数据清理
```bash
# 停止模拟任务
curl -X POST "http://localhost:8090/mock/api/simulation/stop/{taskId}"
```

## 📈 性能指标

### 单机性能参考
- 创建Bot速度: ~500个/秒
- 并发模拟能力: 100个Bot并发行为
- 内存占用: 每1000个Bot约占用50MB

## 🐛 常见问题

**Q: Bot创建后在哪里查看？**  
A: Bot数据存储在内存中，可通过日志查看创建信息。

**Q: 如何调整打赏金额范围？**  
A: 修改 application.yml 中的 `mock.random.recharge-min` 和 `recharge-max`

**Q: 模拟任务如何停止？**  
A: 使用 `/api/simulation/stop/{taskId}` 接口或等待自动完成

## 📝 日志查看

```bash
tail -f logs/mock-service.log
```

关键日志关键词：
- `创建模拟主播` - 主播创建
- `批量创建Bot观众` - Bot创建
- `启动行为模拟` - 模拟开始
- `进入直播间` / `发送弹幕` / `打赏` - 行为日志

## 🎉 下一步

1. 查看完整API文档: http://localhost:8090/mock/swagger-ui.html
2. 查看详细README: services/mock-service/README.md
3. 集成到其他服务进行端到端测试
