# Mock Service - 模拟测试服务

## 📋 服务概述

Mock Service 是一个功能强大的模拟测试服务，用于生成测试数据和模拟直播系统中的各种用户行为。

**服务端口**: 8090  
**访问路径**: http://localhost:8090/mock

## ✨ 核心功能

### 1. 主播管理
- ✅ 创建单个模拟主播（支持自定义属性）
- ✅ 批量创建随机主播
- ✅ 自动分配标签和个人简介

### 2. 直播间管理
- ✅ 为主播创建直播间
- ✅ 自动生成直播间标题、描述、分类
- ✅ 支持自定义直播间配置

### 3. 观众管理
- ✅ 创建特定观众（支持自定义属性）
- ✅ 批量创建Bot观众
- ✅ 自动分配随机标签
- ✅ 自动分配消费画像（低/中/高消费等级）
- ✅ 支持性别、年龄分布控制

### 4. 行为模拟
- ✅ 模拟观众进入直播间
- ✅ 模拟观众离开直播间
- ✅ 模拟发送测试弹幕
- ✅ 模拟观众打赏
- ✅ 支持自定义模拟时长
- ✅ 支持调整打赏概率

### 5. 快捷操作
- ✅ 一键创建完整测试场景（主播+直播间+观众+行为模拟）

## 🚀 快速开始

### 启动服务

```bash
cd services/mock-service
mvn spring-boot:run
```

### API示例

#### 1. 创建主播

```bash
POST http://localhost:8090/mock/api/anchor/create
Content-Type: application/json

{
  "anchorName": "美女主播小芳",
  "gender": 0,
  "bio": "喜欢唱歌跳舞的主播~",
  "tags": ["唱歌", "跳舞", "颜值"]
}
```

#### 2. 批量创建Bot观众

```bash
POST http://localhost:8090/mock/api/audience/batch-create-bots
Content-Type: application/json

{
  "count": 50,
  "assignRandomTags": true,
  "assignConsumptionLevel": true,
  "malePercentage": 55,
  "minAge": 18,
  "maxAge": 40
}
```

#### 3. 启动行为模拟

```bash
POST http://localhost:8090/mock/api/simulation/start
Content-Type: application/json

{
  "liveRoomId": 123456,
  "audienceCount": 20,
  "durationSeconds": 300,
  "simulateEnter": true,
  "simulateLeave": true,
  "simulateMessage": true,
  "simulateRecharge": true,
  "rechargeProbability": 20
}
```

#### 4. 一键创建完整场景

```bash
POST http://localhost:8090/mock/api/quick/complete-scenario?anchorName=测试主播&botCount=30&simulationSeconds=300
```

## ⚙️ 配置说明

### application.yml 配置

```yaml
mock:
  # Bot配置
  bot:
    name-prefix: "Bot_"
    default-batch-size: 50
    max-batch-size: 500
  
  # 行为模拟配置
  simulation:
    enabled: true
    enter-interval-min: 1000    # 进入间隔最小值（毫秒）
    enter-interval-max: 5000    # 进入间隔最大值（毫秒）
    message-interval-min: 3000  # 弹幕间隔最小值（毫秒）
    message-interval-max: 10000 # 弹幕间隔最大值（毫秒）
    recharge-interval-min: 10000 # 打赏间隔最小值（毫秒）
    recharge-interval-max: 30000 # 打赏间隔最大值（毫秒）
  
  # 随机数据配置
  random:
    gender-male-rate: 55         # 男性占比（%）
    consumption-low-rate: 60     # 低消费占比（%）
    consumption-medium-rate: 30  # 中消费占比（%）
    consumption-high-rate: 10    # 高消费占比（%）
    recharge-min: 1.0            # 打赏金额最小值
    recharge-max: 1000.0         # 打赏金额最大值
    tag-count-min: 1             # 标签数量最小值
    tag-count-max: 5             # 标签数量最大值
```

## 📊 数据生成规则

### 主播数据
- **名称**: 随机中文名字或自定义
- **性别**: 按配置的比例随机分配
- **头像**: 自动生成随机头像URL
- **标签**: 从预定义标签库中随机选择1-5个

### 观众数据
- **昵称**: Bot前缀 + 随机UUID（Bot观众）或自定义
- **性别**: 可配置男女比例
- **年龄**: 可配置范围（默认18-50）
- **消费等级**: 低(60%)、中(30%)、高(10%)
- **标签**: 自动分配兴趣标签

### 直播间数据
- **标题**: 基于模板自动生成
- **分类**: 娱乐、游戏、音乐、舞蹈等
- **封面**: 随机生成封面图片URL

### 行为模拟
- **进入**: 随机间隔1-5秒
- **弹幕**: 随机间隔3-10秒，80%概率发送
- **打赏**: 随机间隔10-30秒，可配置概率
- **离开**: 模拟结束前随机离开

## 🎯 使用场景

1. **压力测试**: 批量创建观众模拟高并发场景
2. **功能测试**: 快速生成测试数据验证功能
3. **演示环境**: 创建逼真的演示数据
4. **性能调优**: 模拟真实用户行为进行性能分析

## 📝 API文档

完整API文档请访问：http://localhost:8090/mock/swagger-ui.html

## 🔧 扩展开发

### 添加新的弹幕模板

编辑 `RandomDataGenerator.java`:

```java
private static final List<String> MESSAGE_TEMPLATES = Arrays.asList(
    "主播好棒！",
    "666",
    // 添加更多模板...
);
```

### 调整打赏金额分布

修改 `application.yml`:

```yaml
mock:
  random:
    recharge-min: 10.0
    recharge-max: 500.0
```

## ⚠️ 注意事项

1. Bot观众数据仅存储在内存中，服务重启后清空
2. 建议单次创建Bot数量不超过500个
3. 长时间模拟任务会占用线程资源，建议合理设置持续时间
4. 模拟打赏不会真实扣款，仅用于测试

## 🐛 故障排查

### 问题1: 服务无法启动
- 检查端口8090是否被占用
- 确认数据库连接配置正确

### 问题2: 模拟任务无响应
- 检查线程池配置
- 查看日志确认是否有异常

### 问题3: 数据生成不符合预期
- 检查 application.yml 配置参数
- 调整随机数据生成规则
