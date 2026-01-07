# 数据库服务（Database Service）API 接口文档

## 服务基本信息
- **服务名称**: db-service
- **服务端口**: 8086
- **基础路径**: /api/database
- **版本**: 1.0.0
- **功能描述**: 提供数据库初始化、健康检查、表管理等数据库管理功能

---

## 1. 数据库管理（Database Management）

### 1.1 获取数据库健康状态
- **端点**: `GET /api/database/health`
- **功能**: 检查数据库连接是否正常
- **响应**: ResponseEntity<Map<String, Object>>
  ```json
  {
    "status": "UP",
    "message": "数据库连接正常",
    "timestamp": 1704067200000
  }
  ```
- **成功状态**: HTTP 200
- **失败状态**: HTTP 503
- **用途**: 健康检查探针、监控告警

### 1.2 初始化数据库表
- **端点**: `POST /api/database/initialize`
- **功能**: 执行数据库初始化脚本，创建所有必要的表和索引
- **响应**: ResponseEntity<Map<String, Object>>
  ```json
  {
    "status": "success",
    "message": "数据库初始化成功",
    "timestamp": 1704067200000
  }
  ```
- **成功状态**: HTTP 200
- **失败状态**: HTTP 500
- **用途**: 首次部署、数据库重置
- **权限**: 需要管理员权限
- **注意**: 创建的表包括：
  - anchor (主播表)
  - audience (观众表)
  - live_room (直播间表)
  - recharge (打赏记录表)
  - withdrawal (提现记录表)
  - settlement (结算记录表)
  - commission_rate (分成比例表)
  - 其他业务表...

### 1.3 检查数据库表
- **端点**: `GET /api/database/tables`
- **功能**: 检查所有数据库表是否存在和完整
- **响应**: ResponseEntity<Map<String, Object>>
  ```json
  {
    "status": "success",
    "message": "数据库表检查完成",
    "timestamp": 1704067200000
  }
  ```
- **成功状态**: HTTP 200
- **失败状态**: HTTP 500
- **用途**: 验证数据库完整性、定期检查

### 1.4 获取服务信息
- **端点**: `GET /api/database/info`
- **功能**: 获取db-service的基本信息
- **响应**: ResponseEntity<Map<String, Object>>
  ```json
  {
    "service": "db-service",
    "version": "1.0.0",
    "description": "数据库初始化和管理服务",
    "timestamp": 1704067200000
  }
  ```
- **用途**: 服务发现、版本查询

---

## 数据库表清单

### 核心业务表

| 表名 | 功能 | 关键字段 |
|------|------|---------|
| anchor | 主播信息 | id, nickname, realName, fanCount, totalEarnings |
| audience | 观众信息 | id, nickname, realName, consumptionLevel, status |
| live_room | 直播间 | id, anchorId, title, status, onlineViewers |
| recharge | 打赏记录 | id, anchorId, audienceId, amount, traceId |
| withdrawal | 提现申请 | id, anchorId, amount, status, traceId |
| settlement | 结算记录 | id, anchorId, settledAmount, settleDate |
| commission_rate | 分成比例 | id, anchorId, rate, effectiveDate, endDate |

### 系统表

| 表名 | 功能 |
|------|------|
| audit_log | 审计日志 |
| system_config | 系统配置 |
| data_dict | 数据字典 |

---

## 初始化流程

```
1. 检查数据库连接
2. 执行DDL脚本创建表
3. 创建所有索引
4. 插入初始数据（字典表、配置表）
5. 验证表完整性
6. 记录初始化日志
```

---

## 错误处理

**常见错误场景**:

1. **数据库连接失败**
   - 状态码: 503
   - 信息: "数据库连接失败: ..."
   - 原因: 数据库服务不可用、网络连接问题

2. **初始化脚本执行失败**
   - 状态码: 500
   - 信息: "数据库初始化失败: ..."
   - 原因: SQL语法错误、表已存在、权限不足

3. **表检查失败**
   - 状态码: 500
   - 信息: "数据库表检查失败: ..."
   - 原因: 表不存在、表结构不完整

---

## 监控与日志

所有操作都会记录以下日志：

### AppLogger 应用日志
- "执行数据库健康检查"
- "收到数据库初始化请求"
- "数据库初始化成功/失败"
- "执行数据库表检查"

### TraceLogger 追踪日志
- "database", "health_check_passed/failed"
- "database", "initialize_requested/completed/failed"
- "database", "table_check_requested/completed/failed"

### HealthCheck 健康检查日志
- "database_connection", success/failure, message

---

## 注意事项

1. 初始化操作是幂等的，可以安全地多次执行
2. 生产环境建议定期备份数据库
3. 数据库初始化前应确保没有重要数据
4. 表检查不修改任何数据，是安全的只读操作
5. 建议在应用启动时执行一次健康检查
6. 若长期连接失败，应立即告警并查看数据库日志

---

## 与Docker集成

在Docker容器中使用：

```bash
# 检查数据库健康状态
curl http://localhost:8086/api/database/health

# 初始化数据库
curl -X POST http://localhost:8086/api/database/initialize

# 检查表完整性
curl http://localhost:8086/api/database/tables
```

---

## 与Kubernetes集成

Kubernetes健康检查配置：

```yaml
livenessProbe:
  httpGet:
    path: /api/database/health
    port: 8086
  initialDelaySeconds: 30
  periodSeconds: 10

readinessProbe:
  httpGet:
    path: /api/database/health
    port: 8086
  initialDelaySeconds: 10
  periodSeconds: 5
```

