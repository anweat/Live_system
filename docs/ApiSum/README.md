# API 文档索引

## 📚 文档目录

本目录包含直播系统的完整API接口文档，共8个主要文档文件。

---

## 📄 文件清单

### 1. **0-API-SUMMARY.md** ⭐ 开始阅读这个
**摘要**: 系统API总体概览和导航
- 系统架构图
- 7个服务的功能概览
- 通用API规范
- 核心业务流程
- 缓存策略
- 监控与健康检查

**适合**: 快速了解系统整体架构

---

### 2. **1-anchor-service-API.md**
**服务**: 主播服务 (端口: 8081)
**接口数**: 37个
**主要功能**:
- ✅ 主播账户管理
- ✅ 直播间生命周期管理
- ✅ 打赏记录查询
- ✅ 提现申请管理
- ✅ 实时数据处理
- ✅ 分成比例查询

**核心端点**:
```
POST   /api/v1/anchors
GET    /api/v1/anchors/{anchorId}
POST   /api/v1/live-rooms/{liveRoomId}/start
GET    /api/v1/live-rooms/realtime
```

---

### 3. **2-audience-service-API.md**
**服务**: 观众服务 (端口: 8082)
**接口数**: 21个
**主要功能**:
- ✅ 观众账户管理
- ✅ 打赏记录管理
- ✅ 消费统计
- ✅ 幂等性检查（防重复打赏）
- ✅ 与财务服务的数据同步

**核心端点**:
```
POST   /api/v1/audiences
POST   /api/v1/recharge
GET    /api/v1/recharge/anchor/{anchorId}
PATCH  /api/v1/recharge/{rechargeId}/sync
```

---

### 4. **3-finance-service-API.md**
**服务**: 财务服务 (端口: 8083)
**接口数**: 18个
**主要功能**:
- ✅ 提现申请审核
- ✅ 结算管理
- ✅ 收入统计分析
- ✅ 分成比例管理
- ✅ 数据同步接收

**核心端点**:
```
POST   /api/v1/withdrawal
GET    /api/v1/statistics/anchor/revenue/{anchorId}
POST   /api/v1/commission
POST   /internal/sync/recharges
```

---

### 5. **4-data-analysis-service-API.md**
**服务**: 数据分析服务 (端口: 8084)
**接口数**: 38个
**主要功能**:
- ✅ 聚合统计分析
- ✅ 主播收入分析与排行榜
- ✅ 时间序列数据
- ✅ 用户画像与行为分析
- ✅ 保留率分析
- ✅ 热力图分析

**核心端点**:
```
GET    /api/analysis/aggregation/metrics
GET    /api/v1/analysis/anchor/income/{anchorId}
GET    /api/analysis/timeseries/daily
GET    /api/analysis/ranking/toppayers/{anchorId}
```

---

### 6. **5-redis-service-API.md**
**服务**: Redis服务 (端口: 8085)
**接口数**: 15个
**主要功能**:
- ✅ 缓存管理
- ✅ 分布式锁
- ✅ 幂等性检查
- ✅ 计数器操作

**核心端点**:
```
POST   /api/v1/cache/set
GET    /api/v1/cache/get
POST   /api/v1/lock/try-lock
POST   /api/v1/lock/release-lock
```

---

### 7. **6-db-service-API.md**
**服务**: 数据库服务 (端口: 8086)
**接口数**: 4个
**主要功能**:
- ✅ 数据库健康检查
- ✅ 数据库初始化
- ✅ 表完整性检查
- ✅ 服务信息查询

**核心端点**:
```
GET    /api/database/health
POST   /api/database/initialize
GET    /api/database/tables
```

---

### 8. **7-mock-service-API.md**
**服务**: 模拟数据服务 (端口: 8087)
**接口数**: 20+
**主要功能**:
- ✅ 模拟数据生成
- ✅ 批量生成测试数据
- ✅ 直播模拟
- ✅ 数据清理与重置

**核心端点**:
```
POST   /api/v1/mock/anchor/batch-create
POST   /api/v1/mock/recharge/batch-create
DELETE /api/v1/mock/data/clear
GET    /api/v1/mock/data/statistics
```

---

### 9. **8-NGINX-Docker-Setup.md**
**主题**: Nginx网关配置与Docker部署
**内容**:
- Nginx 网关概述与路由配置
- Docker Compose 快速启动指南
- 完整的 docker-compose.yml 说明
- 环境变量配置
- Nginx 配置文件详解
- 常见故障排除
- 性能优化建议
- 生产部署注意事项

**适合**: 系统部署和运维人员

---

## 🎯 快速导航

### 按用途查找

**我是前端开发者，想要调用API**
→ 阅读 [0-API-SUMMARY.md](./0-API-SUMMARY.md) 中的"核心业务流程"
→ 根据需要查看具体服务的API文档

**我是后端开发者，想要理解某个服务**
→ 查看对应服务的API文档 (如 1-anchor-service-API.md)
→ 按照端点列表调用相应的接口

**我是运维人员，想要部署系统**
→ 阅读 [8-NGINX-Docker-Setup.md](./8-NGINX-Docker-Setup.md)
→ 按照步骤进行Docker部署

**我想要测试API**
→ 参考各服务文档中的"错误处理"和"注意事项"
→ 使用 [7-mock-service-API.md](./7-mock-service-API.md) 生成测试数据

---

### 按服务查找

| 服务 | 文档文件 | 端口 | 接口数 |
|------|---------|------|--------|
| 主播服务 | 1-anchor-service-API.md | 8081 | 37 |
| 观众服务 | 2-audience-service-API.md | 8082 | 21 |
| 财务服务 | 3-finance-service-API.md | 8083 | 18 |
| 数据分析服务 | 4-data-analysis-service-API.md | 8084 | 38 |
| Redis服务 | 5-redis-service-API.md | 8085 | 15 |
| 数据库服务 | 6-db-service-API.md | 8086 | 4 |
| 模拟数据服务 | 7-mock-service-API.md | 8087 | 20+ |
| **总计** | - | - | **153+** |

---

## 📋 文档统计

- **总文档数**: 9
- **总接口数**: 153+
- **总字符数**: ~150,000+
- **总代码示例**: 200+
- **更新日期**: 2024年1月7日

---

## 🚀 快速启动

### 开发环境

```bash
# 1. 进入部署目录
cd deployment/docker

# 2. 启动所有服务
./manage.sh start

# 3. 检查服务状态
./manage.sh status

# 4. 查看日志
./manage.sh logs
```

### 测试API

```bash
# 查询主播信息
curl http://localhost/anchor/api/v1/anchors

# 创建观众
curl -X POST http://localhost/audience/api/v1/audiences \
  -H "Content-Type: application/json" \
  -d '{"nickname":"观众1","realName":"张三"}'

# 查询财务统计
curl http://localhost/finance/api/v1/statistics/top-anchors
```

---

## 🔗 相关文档

- 📘 [项目README](../../README.md)
- 📙 [POM详细解释](../POM_详细解释.md)
- 📕 [Redis配置指南](../REDIS_CONFIGURATION_GUIDE.md)
- 📗 [Docker快速启动](../deployment/docker/QUICKSTART.md)
- 📔 [系统集成总结](../系统集成与打赏流程优化总结.md)

---

## ✅ 文档特点

- ✨ **完整详细**: 每个API都有完整的说明和示例
- 📊 **结构清晰**: 按照模块和功能分类
- 🎯 **易于导航**: 每个文档都有清晰的索引
- 📝 **代码示例**: 包含丰富的请求和响应示例
- ⚠️ **注意事项**: 每个服务都有重要的使用提示
- 🔐 **安全建议**: 包含权限和安全相关的说明

---

## 💡 使用建议

1. **首次接触**: 先阅读 [0-API-SUMMARY.md](./0-API-SUMMARY.md) 了解整体架构
2. **深入学习**: 根据需要阅读具体服务的文档
3. **实际操作**: 参考各文档中的代码示例进行测试
4. **部署上线**: 参考 [8-NGINX-Docker-Setup.md](./8-NGINX-Docker-Setup.md)
5. **持续维护**: 定期查看文档的"注意事项"部分

---

## 📞 支持与反馈

- 📧 技术问题: 联系开发团队
- 🐛 文档错误: 提出Issue或PR
- 💬 功能建议: 在讨论区反馈

---

## 📅 文档历史

| 版本 | 日期 | 说明 |
|------|------|------|
| 1.0 | 2024-01-07 | 初始版本，完成全部7个服务的API文档 |

---

## 📚 学习路径推荐

### 初级开发者
1. 阅读 [0-API-SUMMARY.md](./0-API-SUMMARY.md) - 系统整体认知
2. 阅读 [1-anchor-service-API.md](./1-anchor-service-API.md) - 核心服务
3. 实践: 使用 [7-mock-service-API.md](./7-mock-service-API.md) 生成测试数据

### 中级开发者
1. 深入学习 [3-finance-service-API.md](./3-finance-service-API.md) - 财务流程
2. 学习 [5-redis-service-API.md](./5-redis-service-API.md) - 缓存策略
3. 研究 [4-data-analysis-service-API.md](./4-data-analysis-service-API.md) - 数据分析

### 高级开发者/架构师
1. 深入研究 [8-NGINX-Docker-Setup.md](./8-NGINX-Docker-Setup.md) - 系统架构
2. 优化 [5-redis-service-API.md](./5-redis-service-API.md) 中的性能建议
3. 规划 [3-finance-service-API.md](./3-finance-service-API.md) 的扩展

---

**祝你使用愉快！如有任何问题，请参考相关文档或联系开发团队。**

