# Nginx反向代理接口汇总

## 更新日期：2026-01-02

---

## 一、服务架构概览

本系统采用Nginx作为统一的API网关，将客户端请求分发到不同的后端微服务。

### 架构图：
```
客户端
    ↓
Nginx (Port: 80)
    ├─ /anchor/        → anchor-service:8081      (主播服务) - 37个接口
    ├─ /audience/      → audience-service:8082    (观众服务) - 21个接口
    ├─ /finance/       → finance-service:8083     (财务服务) - 18个接口
    ├─ /data-analysis/ → data-analysis-service:8084 (数据分析服务) - 38个接口
    └─ /redis/         → redis-service:8085       (Redis缓存服务) - 15个接口
```

**总计：129个接口**

---

## 二、各服务接口详情

### 1. 主播服务 (anchor-service) - 共37个接口

**基础路径**: `/anchor/api/v1/`  
**服务地址**: `anchor-service:8081`

#### 1.1 主播管理接口（12个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/anchors` | 创建主播 |
| GET | `/anchors/{id}` | 查询主播信息 |
| PUT | `/anchors/{id}` | 修改主播信息 |
| GET | `/anchors` | 查询主播列表（分页） |
| GET | `/anchors/verification-status/{status}` | 按认证状态查询 |
| GET | `/anchors/search` | 搜索主播 |
| PATCH | `/anchors/{id}/stats` | 更新统计数据 |
| PATCH | `/anchors/{id}/earnings` | 更新累计收益 |
| GET | `/anchors/{id}/available-amount` | 查询可提取余额 |
| PATCH | `/anchors/{id}/available-amount` | 更新可提取余额 |
| GET | `/anchors/count` | 查询主播总数 |
| GET | `/anchors/count/verification-status/{status}` | 按状态统计 |

#### 1.2 直播间管理接口（10个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/live-rooms/{id}` | 查询直播间信息 |
| GET | `/live-rooms/anchor/{anchorId}` | 按主播查询直播间 |
| POST | `/live-rooms/{id}/start` | 开启直播 |
| POST | `/live-rooms/{id}/end` | 结束直播 |
| PATCH | `/live-rooms/{id}/realtime` | 更新实时数据 |
| GET | `/live-rooms/live` | 查询正在直播 |
| GET | `/live-rooms/category/{category}` | 按分类查询 |
| PUT | `/live-rooms/{id}` | 更新直播间信息 |
| GET | `/live-rooms/count/live` | 统计直播中数量 |
| GET | `/live-rooms/count/category/{category}` | 按分类统计 |

#### 1.3 直播间实时数据接口（5个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/live-rooms/realtime/viewer-enter` | 观众进入直播间 |
| POST | `/live-rooms/realtime/viewer-leave` | 观众离开直播间 |
| POST | `/live-rooms/realtime/danmaku` | 发送弹幕 |
| POST | `/live-rooms/realtime/reward` | 观众打赏（由观众服务调用） |
| GET | `/live-rooms/realtime/{id}` | 查询实时数据 |

#### 1.4 分成比例管理接口（2个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/commission-rate/{id}/current` | 查询当前比例 |
| DELETE | `/commission-rate/{id}/cache` | 清除缓存 |

#### 1.5 提现管理接口（3个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/withdrawal/apply` | 申请提现 |
| GET | `/withdrawal/list/{anchorId}` | 查询提现记录 |
| GET | `/withdrawal/trace/{traceId}` | 按traceId查询 |

#### 1.6 打赏记录查询接口（5个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/recharge/anchor/{id}` | 查询主播打赏记录 |
| GET | `/recharge/live-room/{id}` | 查询直播间打赏 |
| GET | `/recharge/trace/{traceId}` | 按traceId查询 |
| GET | `/recharge/anchor/{id}/total` | 统计打赏总额 |
| GET | `/recharge/anchor/{id}/top10` | 查询TOP10观众 |

---

### 2. 观众服务 (audience-service) - 共21个接口

**基础路径**: `/audience/api/v1/`  
**服务地址**: `audience-service:8082`

#### 2.1 观众管理接口（10个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/audiences` | 创建观众账号 |
| POST | `/audiences/guest` | 创建游客观众 |
| GET | `/audiences/{id}` | 根据ID查询观众信息 |
| PUT | `/audiences/{id}` | 修改观众信息（昵称/头像等） |
| GET | `/audiences` | 查询观众列表（分页） |
| GET | `/audiences/search` | 搜索观众 |
| GET | `/audiences/{id}/consumption-stats` | 查询观众消费统计 |
| PUT | `/audiences/{id}/disable` | 禁用观众账户 |
| PUT | `/audiences/{id}/enable` | 启用观众账户 |

#### 2.2 打赏接口（9个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/recharge` | **观众打赏（实时通知直播间）** |
| GET | `/recharge/{id}` | 根据ID查询单条打赏记录 |
| GET | `/recharge/by-trace-id/{traceId}` | 根据流水号查询打赏 |
| GET | `/recharge/anchor/{id}` | 查询主播收到的打赏记录 |
| GET | `/recharge/audience/{id}` | 查询观众的所有打赏记录 |
| GET | `/recharge/live-room/{id}` | 查询直播间的打赏记录 |
| GET | `/recharge/top10` | 查询TOP10观众榜单 |
| GET | `/recharge/unsync` | 查询待同步打赏记录 |
| PATCH | `/recharge/{id}/sync` | 标记打赏记录已同步 |

**重要说明**：  
打赏接口 `POST /audience/api/v1/recharge` 在保存记录后会实时调用主播服务的 `POST /anchor/api/v1/live-rooms/realtime/reward` 接口，更新直播间实时数据。采用降级策略，主播服务不可用时不影响打赏记录保存。

#### 2.3 测试/同步接口（2个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/test/sync/queue/status` | 查询同步队列状态 |
| POST | `/test/sync/trigger` | 触发同步任务 |

---

### 3. 财务服务 (finance-service) - 共18个接口

**基础路径**: `/finance/api/v1/`  
**服务地址**: `finance-service:8083`

#### 3.1 分成比例管理接口（3个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/commission` | 创建/更新主播分成比例配置 |
| GET | `/commission/{anchorId}/current` | 查询当前生效的分成比例 |
| GET | `/commission/{anchorId}/history` | 查询历史分成配置 |

#### 3.2 结算管理接口（3个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/settlement/{anchorId}/balance` | 查询主播余额 |
| GET | `/settlement/{anchorId}/details` | 查询结算明细 |
| POST | `/settlement/trigger` | 手动触发结算 |

#### 3.3 提现管理接口（5个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/withdrawal` | 创建提现申请 |
| GET | `/withdrawal/{anchorId}` | 查询主播的提现列表 |
| GET | `/withdrawal/by-trace-id/{traceId}` | 根据流水号查询提现 |
| PUT | `/withdrawal/{id}/approve` | 审核通过提现申请 |
| PUT | `/withdrawal/{id}/reject` | 审核拒绝提现申请 |

#### 3.4 内部同步接口（2个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/internal/sync/recharges` | 接收批量打赏数据（内部） |
| GET | `/internal/sync/progress` | 查询同步进度（内部） |

#### 3.5 统计接口（5个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/api/finance/statistics/anchor/revenue/{id}` | 主播收入统计 |
| GET | `/api/finance/statistics/anchor/hourly/{id}` | 主播小时统计 |
| GET | `/api/finance/statistics/anchor/top-audiences/{id}` | 主播TOP观众 |
| POST | `/api/finance/statistics/anchor/batch-revenue` | 批量主播收入 |
| GET | `/api/finance/statistics/top-anchors` | TOP主播排行 |
| GET | `/transaction/trace/{traceId}` | 根据流水号查询交易 |

---

### 4. 数据分析服务 (data-analysis-service) - 共38个接口

**基础路径**: `/data-analysis/api/v1/analysis/`  
**服务地址**: `data-analysis-service:8084`

#### 4.1 统计分析接口（4个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/statistics/gmv-trend` | GMV趋势分析 |
| GET | `/statistics/key-metrics` | 关键指标统计 |
| GET | `/statistics/time-heatmap` | 时间热力图 |
| GET | `/statistics/category-performance` | 分类表现分析 |

#### 4.2 排行榜接口（4个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/ranking/anchor/income` | 主播收入排行 |
| GET | `/ranking/audience/consumption` | 观众消费排行 |
| GET | `/ranking/anchor/fans` | 主播粉丝排行 |
| GET | `/ranking/live-room/popularity` | 直播间热度排行 |

#### 4.3 标签分析接口（3个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/tag/heatmap` | 标签热力图 |
| GET | `/tag/related` | 关联标签查询 |
| GET | `/tag/hot` | 热门标签 |

#### 4.4 观众分析接口（4个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/audience/{id}/portrait` | 观众用户画像 |
| GET | `/audience/consumption-distribution` | 消费分布分析 |
| GET | `/audience/retention` | 用户留存分析 |
| GET | `/audience/churn-warning` | 流失预警分析 |

#### 4.5 主播分析接口（3个）

| 方法 | 路径 | 描述 |
|------|------|------|
| GET | `/anchor/{id}/income` | 主播收入分析 |
| GET | `/anchor/{id}/radar` | 主播雷达图分析 |
| GET | `/anchor/compare` | 主播对比分析 |

#### 4.6 手动分析接口（15个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/manual/query` | 自定义查询 |
| POST | `/manual/sync/full` | 全量数据同步 |
| POST | `/manual/sync/incremental` | 增量数据同步 |
| GET | `/manual/custom-range` | 自定义时间范围分析 |
| GET | `/manual/compare-periods` | 时段对比分析 |
| POST | `/manual/cohort-analysis` | 队列分析 |
| GET | `/manual/compare-anchor-groups` | 主播分组对比 |
| POST | `/manual/funnel-analysis` | 漏斗分析 |
| GET | `/manual/anomaly-detection` | 异常检测 |
| GET | `/manual/correlation-analysis` | 相关性分析 |
| POST | `/manual/import-csv` | 导入CSV数据 |
| POST | `/manual/generate-report` | 生成报表 |
| GET | `/manual/prediction` | 预测分析 |
| DELETE | `/manual/cache` | 清除缓存 |
| GET | `/manual/data-quality-check` | 数据质量检查 |

#### 4.7 分析任务接口（5个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/task/trigger/hourly-statistics` | 触发小时统计任务 |
| POST | `/task/trigger/audience-portrait` | 触发用户画像分析 |
| POST | `/task/trigger/tag-relation` | 触发标签关联分析 |
| POST | `/task/trigger/retention-analysis` | 触发留存分析 |
| GET | `/task/status` | 查询任务状态 |

---

### 5. Redis缓存服务 (redis-service) - 共15个接口

**基础路径**: `/redis/api/v1/`  
**服务地址**: `redis-service:8085`

#### 5.1 缓存操作接口（9个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/cache/set` | 设置缓存 |
| GET | `/cache/get` | 获取缓存 |
| GET | `/cache/exists` | 检查键存在性 |
| DELETE | `/cache/delete` | 删除缓存 |
| POST | `/cache/expire` | 设置过期时间 |
| GET | `/cache/ttl` | 获取剩余过期时间 |
| POST | `/cache/increment` | 递增操作 |
| POST | `/cache/decrement` | 递减操作 |
| GET | `/cache/health` | 健康检查 |

#### 5.2 分布式锁接口（6个）

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/lock/try-lock` | 尝试获取锁 |
| POST | `/lock/release-lock` | 释放锁 |
| POST | `/lock/force-release-lock` | 强制释放锁 |
| GET | `/lock/is-locked` | 检查是否被锁定 |
| GET | `/lock/get-lock-value` | 获取锁持有者 |
| POST | `/lock/check-idempotency` | 幂等性检查（防重复提交） |

---

## 三、Nginx配置特性

### 3.1 负载均衡配置
```nginx
upstream service_name {
    server service-name:port max_fails=2 fail_timeout=30s;
    keepalive 32;
}
```

**参数说明**：
- `max_fails=2`: 失败2次后标记为不可用
- `fail_timeout=30s`: 30秒后重新尝试
- `keepalive 32`: 保持32个长连接

### 3.2 代理头配置
每个location都配置了以下HTTP头：
```nginx
proxy_http_version 1.1;
proxy_set_header Connection "";
proxy_set_header Host $host;
proxy_set_header X-Real-IP $remote_addr;
proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
proxy_set_header X-Forwarded-Proto $scheme;
proxy_set_header X-Trace-Id $http_x_trace_id;  # 链路追踪ID
```

### 3.3 超时配置
```nginx
proxy_connect_timeout 10s;   # 连接超时
proxy_send_timeout 30s;      # 发送超时
proxy_read_timeout 30s;      # 读取超时
```

### 3.4 Gzip压缩
```nginx
gzip on;
gzip_vary on;
gzip_proxied any;
gzip_comp_level 6;
gzip_types text/plain text/css text/xml text/javascript 
           application/json application/javascript;
```

### 3.5 请求体大小限制
```nginx
client_max_body_size 20M;
```

---

## 四、服务间调用关系

### 4.1 打赏流程（跨服务调用）

```
前端/客户端
    ↓ POST /audience/api/v1/recharge
观众服务 (audience-service:8082)
    ├─ 保存打赏记录到DB
    ├─ Feign调用 ↓
    │  POST /anchor/api/v1/live-rooms/realtime/reward
    └─ 主播服务 (anchor-service:8081)
        ├─ 更新直播间实时数据（Redis + MySQL）
        └─ 返回成功
    ↓
加入同步队列（待同步到财务服务）
    ↓ 定时任务（每5分钟）
    ↓ POST /finance/api/v1/recharge/batch-sync
财务服务 (finance-service:8083)
    ├─ 计算分成比例
    ├─ 更新主播余额
    └─ 创建结算记录
```

### 4.2 提现流程

```
前端/客户端
    ↓ POST /anchor/api/v1/withdrawal/apply
主播服务 (anchor-service:8081)
    ├─ 验证可提现余额
    ├─ 创建提现申请
    └─ Feign调用 ↓
       POST /finance/api/v1/withdrawal
财务服务 (finance-service:8083)
    ├─ 创建提现记录
    ├─ 扣减主播余额
    └─ 等待审核
```

### 4.3 分成比例查询流程（带缓存）

```
前端/客户端
    ↓ GET /anchor/api/v1/commission-rate/{id}/current
主播服务 (anchor-service:8081)
    ├─ 检查Redis缓存
    ├─ 缓存未命中 ↓
    │  Feign调用财务服务
    │  GET /finance/api/v1/commission-rate/{id}/current
    └─ 财务服务 (finance-service:8083)
        ├─ 查询数据库
        ├─ 返回分成比例
        └─ 主播服务将结果缓存到Redis（TTL: 1小时）
```

---

## 五、健康检查与监控

### 5.1 Nginx健康检查
```bash
# 检查Nginx状态
curl http://localhost/health
# 响应: OK
```

### 5.2 各服务健康检查
```bash
# 主播服务
curl http://localhost/anchor/actuator/health

# 观众服务
curl http://localhost/audience/actuator/health

# 财务服务
curl http://localhost/finance/actuator/health

# 数据分析服务
curl http://localhost/data-analysis/actuator/health
```

### 5.3 Nginx日志
```bash
# 访问日志
tail -f /var/log/nginx/access.log

# 错误日志
tail -f /var/log/nginx/error.log
```

---

## 六、部署与运维

### 6.1 配置文件位置
```
services/nginx/docker/nginx.conf
```

### 6.2 测试配置
```bash
nginx -t
```

### 6.3 重新加载配置（无需停机）
```bash
nginx -s reload
```

### 6.4 重启Nginx
```bash
# Docker环境
docker-compose restart nginx

# 或
docker restart nginx-container
```

### 6.5 服务启动顺序
```
1. Redis服务 (redis-service)
2. 数据库服务 (MySQL)
3. 财务服务 (finance-service:8083)
4. 主播服务 (anchor-service:8081)
5. 观众服务 (audience-service:8082)
6. 数据分析服务 (data-analysis-service:8084)
7. Nginx (端口80)
```

---

## 七、API调用示例

### 7.1 观众打赏
```bash
curl -X POST http://localhost/audience/api/v1/recharge \
  -H "Content-Type: application/json" \
  -H "X-Trace-Id: RC-20260102-103045-001" \
  -d '{
    "audienceId": 123,
    "anchorId": 456,
    "liveRoomId": 789,
    "amount": 100.00,
    "rechargeType": "COIN",
    "traceId": "RC-20260102-103045-001"
  }'
```

### 7.2 查询主播信息
```bash
curl http://localhost/anchor/api/v1/anchors/456
```

### 7.3 查询主播收益
```bash
curl http://localhost/finance/api/v1/balance/456
```

### 7.4 查询TOP10观众
```bash
curl http://localhost/anchor/api/v1/recharge/anchor/456/top10?period=week
```

---

## 八、错误处理

### 8.1 服务不可用
当后端服务不可用时，Nginx会返回：
- **502 Bad Gateway**: 服务宕机或网络问题
- **504 Gateway Timeout**: 服务响应超时

### 8.2 负载均衡策略
- 失败2次后，将服务标记为不可用
- 30秒后重新尝试连接
- 支持多实例负载均衡（需配置多个server）

---

## 九、接口汇总统计

| 服务 | 接口数量 | 服务地址 |
|------|---------|---------|
| 主播服务 | 37个 | anchor-service:8081 |
| 观众服务 | 21个 | audience-service:8082 |
| 财务服务 | 18个 | finance-service:8083 |
| 数据分析服务 | 38个 | data-analysis-service:8084 |
| Redis缓存服务 | 15个 | redis-service:8085 |
| **总计** | **129个** | - |

---

## 十、版本历史

| 版本 | 日期 | 修改内容 | 修改人 |
|-----|------|---------|-------|
| v1.0 | 2026-01-02 | 初始版本 | Team |
| v1.1 | 2026-01-02 | 完善观众服务和财务服务接口注释 | Team |
| v1.2 | 2026-01-02 | 添加数据分析服务接口详情 | Team |
| v2.0 | 2026-01-02 | 全面整合各模块接口，更新至129个接口 | Team |

---

**文档维护者**: Team  
**最后更新**: 2026-01-02
