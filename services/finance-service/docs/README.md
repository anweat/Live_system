# Finance Service

财务服务 - 负责打赏结算、分成管理、提现处理

## 功能特性

### 核心功能
- ✅ **打赏数据同步** - 从观众服务接收批量打赏数据（幂等性保证）
- ✅ **分成比例管理** - 动态调整主播分成比例，支持历史版本追踪
- ✅ **自动结算** - 定时任务自动结算主播收益
- ✅ **余额查询** - 查询主播可提取金额
- ✅ **提现管理** - 处理主播提现申请（幂等性设计，分布式锁）

### 技术特性
- ✅ **Redis缓存** - 余额、分成比例、提现记录缓存
- ✅ **幂等性保证** - traceId和batchId双重幂等性检查
- ✅ **分布式锁** - Redis分布式锁防止并发提现
- ✅ **定时任务** - 自动结算、缓存清理
- ✅ **服务降级** - Feign降级处理
- ✅ **事务管理** - 关键操作事务保证

## 接口列表

### 内部接口（供观众服务调用）
- `POST /internal/sync/recharges` - 接收批量打赏数据
- `GET /internal/sync/progress` - 查询同步进度

### 分成比例管理
- `POST /api/v1/commission` - 创建/更新分成比例
- `GET /api/v1/commission/{anchorId}/current` - 查询当前分成比例
- `GET /api/v1/commission/{anchorId}/history` - 查询分成比例历史

### 结算查询
- `GET /api/v1/settlement/{anchorId}/balance` - 查询主播余额
- `GET /api/v1/settlement/{anchorId}/details` - 查询结算明细
- `POST /api/v1/settlement/trigger` - 手动触发结算

### 提现管理
- `POST /api/v1/withdrawal` - 申请提现
- `GET /api/v1/withdrawal/{anchorId}` - 查询提现记录
- `GET /api/v1/withdrawal/by-trace-id/{traceId}` - 按traceId查询
- `PUT /api/v1/withdrawal/{withdrawalId}/approve` - 审核通过
- `PUT /api/v1/withdrawal/{withdrawalId}/reject` - 拒绝提现

## 部署说明

### 本地开发
```bash
# 启动服务（开发环境）
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

### Docker部署
```bash
# 构建镜像
docker build -t finance-service:1.0.0 .

# 运行容器
docker run -d \
  --name finance-service \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=production \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e REDIS_HOST=redis \
  -e CONSUL_HOST=consul \
  finance-service:1.0.0
```

## 配置说明

### 环境变量
- `SPRING_PROFILES_ACTIVE` - 运行环境（dev/production）
- `DB_HOST` - 数据库地址
- `DB_PORT` - 数据库端口
- `DB_USERNAME` - 数据库用户名
- `DB_PASSWORD` - 数据库密码
- `REDIS_HOST` - Redis地址
- `REDIS_PORT` - Redis端口
- `CONSUL_HOST` - Consul地址
- `CONSUL_PORT` - Consul端口

### 关键配置
- 结算周期：每10分钟自动结算
- 提现金额限制：1.00-99999.99元
- Redis缓存过期时间：
  - 余额缓存：10分钟
  - 分成比例缓存：24小时
  - 提现记录缓存：24小时

## 幂等性设计

### 打赏数据同步
- **batchId** - 批次ID作为幂等性标识
- **Redis缓存** - 快速幂等性检查
- **数据库检查** - 双重保险

### 提现申请
- **traceId** - 唯一标识提现请求
- **Redis缓存** - 快速幂等性检查
- **数据库唯一约束** - UNIQUE KEY uk_trace_id
- **分布式锁** - 防止并发提现

## Redis缓存策略

### 缓存Key规则
- 余额：`finance:balance:{anchorId}`
- 分成比例：`finance:commission:{anchorId}`
- 提现记录：`finance:withdrawal:trace:{traceId}`
- 打赏记录：`finance:recharge:{traceId}`
- 批次记录：`finance:batch:{batchId}`

### 缓存更新策略
- 查询时自动缓存
- 更新时自动失效
- 定时清理过期缓存

## 数据库表

- `commission_rate` - 分成比例表
- `settlement` - 结算表
- `settlement_detail` - 结算明细表
- `withdrawal` - 提现记录表
- `sync_progress` - 同步进度表

## 监控指标

- 服务健康检查：`/actuator/health`
- 接口调用量、成功率、耗时
- 缓存命中率
- 定时任务执行状态

## 注意事项

1. **幂等性** - 所有关键接口都做了幂等性设计
2. **分布式锁** - 提现操作使用分布式锁防止并发
3. **事务管理** - 关键操作使用事务保证数据一致性
4. **缓存策略** - 合理使用Redis缓存提升性能
5. **降级处理** - Feign调用失败时降级处理

## 作者

Live Room Team - Finance Service
