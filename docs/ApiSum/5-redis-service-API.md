# Redis 服务（Redis Service）API 接口文档

## 服务基本信息
- **服务名称**: redis-service
- **服务端口**: 8085
- **基础路径**: /redis/api/v1
- **版本**: 1.0.0
- **功能描述**: 提供缓存管理、分布式锁、幂等性检查等Redis功能接口

---

## 1. 缓存管理（Cache Management）

### 1.1 设置缓存
- **端点**: `POST /api/v1/cache/set`
- **功能**: 在Redis中设置键值对
- **查询参数**:
  - `key`: String (必填，缓存键)
  - `value`: String (必填，缓存值)
  - `ttl`: Long (默认0，过期时间（秒），0表示永不过期)
- **响应**: BaseResponse<Boolean>
- **成功**: true 表示设置成功
- **用途**: 存储频繁访问的数据

### 1.2 获取缓存
- **端点**: `GET /api/v1/cache/get`
- **功能**: 从Redis中获取缓存值
- **查询参数**:
  - `key`: String (必填，缓存键)
- **响应**: BaseResponse<Object>
  - 消息: "Cache hit" (命中) / "Cache miss" (未命中)
  - 数据: 缓存值或null
- **用途**: 读取缓存数据

### 1.3 检查键是否存在
- **端点**: `GET /api/v1/cache/exists`
- **功能**: 检查指定键是否存在于Redis
- **查询参数**:
  - `key`: String (必填，缓存键)
- **响应**: BaseResponse<Boolean>

### 1.4 删除缓存
- **端点**: `DELETE /api/v1/cache/delete`
- **功能**: 删除Redis中的缓存项
- **查询参数**:
  - `key`: String (必填，缓存键)
- **响应**: BaseResponse<Boolean>

### 1.5 设置缓存过期时间
- **端点**: `POST /api/v1/cache/expire`
- **功能**: 为已存在的键设置过期时间
- **查询参数**:
  - `key`: String (必填，缓存键)
  - `ttl`: Long (必填，过期时间（秒）)
- **响应**: BaseResponse<Boolean>
- **用途**: 更新已有缓存的生命周期

### 1.6 获取缓存的剩余过期时间
- **端点**: `GET /api/v1/cache/ttl`
- **功能**: 获取键的剩余生存时间
- **查询参数**:
  - `key`: String (必填，缓存键)
- **响应**: BaseResponse<Long>
  - 返回值: 剩余秒数，-1表示键不存在或永不过期

### 1.7 自增操作
- **端点**: `POST /api/v1/cache/increment`
- **功能**: 将键的值增加指定的数量（原子操作）
- **查询参数**:
  - `key`: String (必填，缓存键)
  - `delta`: Long (默认1，增加量)
- **响应**: BaseResponse<Long>
  - 返回: 增加后的值
- **用途**: 计数器、浏览量统计等

### 1.8 自减操作
- **端点**: `POST /api/v1/cache/decrement`
- **功能**: 将键的值减少指定的数量（原子操作）
- **查询参数**:
  - `key`: String (必填，缓存键)
  - `delta`: Long (默认1，减少量)
- **响应**: BaseResponse<Long>
  - 返回: 减少后的值

### 1.9 Redis健康检查
- **端点**: `GET /api/v1/cache/health`
- **功能**: 检查Redis连接是否正常
- **响应**: BaseResponse<Boolean>
  - 消息: "Redis is healthy" / "Redis is unavailable"
  - 数据: true (正常) / false (异常)
- **用途**: 监控Redis服务状态

---

## 2. 分布式锁（Distributed Lock）

### 2.1 尝试获取分布式锁
- **端点**: `POST /api/v1/lock/try-lock`
- **功能**: 尝试获取一个分布式锁（非阻塞）
- **查询参数**:
  - `lockKey`: String (必填，锁的唯一标识)
  - `lockValue`: String (必填，锁的值，用于标识持有者，如UUID)
  - `lockTimeout`: Long (默认30000，锁的超时时间（毫秒）)
- **响应**: BaseResponse<Boolean>
  - true: 成功获取锁
  - false: 锁已被其他请求持有
- **原理**: 使用SET NX EX命令实现

### 2.2 释放分布式锁
- **端点**: `POST /api/v1/lock/release-lock`
- **功能**: 释放分布式锁（安全释放，验证lockValue）
- **查询参数**:
  - `lockKey`: String (必填，锁的唯一标识)
  - `lockValue`: String (必填，锁的值，必须与获取时相同)
- **响应**: BaseResponse<Boolean>
  - true: 成功释放锁
  - false: 释放失败（锁值不匹配或锁不存在）
- **安全性**: 只有持有锁的请求才能成功释放

### 2.3 强制释放锁
- **端点**: `POST /api/v1/lock/force-release-lock`
- **功能**: 强制释放分布式锁（不验证lockValue）
- **查询参数**:
  - `lockKey`: String (必填，锁的唯一标识)
- **响应**: BaseResponse<Boolean>
- **警告**: 慎用！可能导致数据不一致
- **使用场景**: 紧急情况下解除死锁

### 2.4 检查锁是否被持有
- **端点**: `GET /api/v1/lock/is-locked`
- **功能**: 检查指定的锁是否被持有
- **查询参数**:
  - `lockKey`: String (必填，锁的唯一标识)
- **响应**: BaseResponse<Boolean>
- **用途**: 检查资源是否被其他请求锁定

### 2.5 获取锁值
- **端点**: `GET /api/v1/lock/get-lock-value`
- **功能**: 获取指定锁当前持有的值
- **查询参数**:
  - `lockKey`: String (必填，锁的唯一标识)
- **响应**: BaseResponse<String>
  - 返回: 锁值或null（如果锁不存在）
- **用途**: 诊断调试、检查锁的持有者

---

## 3. 幂等性检查（Idempotency）

### 3.1 检查幂等性（防重复提交）
- **端点**: `POST /api/v1/lock/check-idempotency`
- **功能**: 检查请求是否重复（基于幂等性键）
- **查询参数**:
  - `idempotentKey`: String (必填，幂等性键，通常是traceId)
  - `ttl`: Long (默认3600，过期时间（秒）)
- **响应**: BaseResponse<Boolean>
  - true: 首次请求，应该继续处理
  - false: 重复请求，应该拒绝并返回之前的结果
- **机制**: 
  1. 首次请求：在Redis中设置key=idempotentKey，value=uuid
  2. 重复请求：检查key是否存在
- **用途**: 防止重复提交导致的重复计费、重复打赏等问题

---

## 缓存键命名规范

建议使用以下格式：
```
{service}:{resource}:{id}  // 如：anchor:profile:123
{service}:{resource}:{id}:{timestamp}  // 如：recharge:transaction:456:1704067200
{service}:lock:{business}  // 如：finance:lock:withdrawal
{service}:temp:{traceId}  // 如：audience:temp:trace_abc123
```

---

## 分布式锁使用示例

```
1. 获取锁: POST /api/v1/lock/try-lock?lockKey=finance:lock:settlement&lockValue={uuid}&lockTimeout=30000
2. 如果获取成功，执行业务逻辑
3. 完成后释放锁: POST /api/v1/lock/release-lock?lockKey=finance:lock:settlement&lockValue={uuid}
4. 如果异常中断，由超时时间自动释放锁
```

---

## 缓存策略建议

### 热数据缓存
- 用户信息：TTL 1小时
- 主播信息：TTL 30分钟
- 直播间信息：TTL 5分钟
- 排行榜数据：TTL 5分钟

### 临时数据缓存
- 临时令牌：TTL 15分钟
- 验证码：TTL 5分钟
- 会话信息：TTL 30分钟

### 计数器缓存
- 在线人数：实时更新，无TTL
- 打赏计数：实时更新，无TTL
- 访问次数：每小时重置

---

## 错误处理

所有接口都返回统一的响应格式：

```json
{
  "code": 200,
  "message": "Cache set success",
  "data": true
}
```

**常见错误码**:
- 200: 成功
- 400: 请求参数错误
- 500: Redis连接错误或操作失败
- 503: Redis服务不可用

---

## 注意事项

1. Redis的set/get操作都是原子的，但复杂业务逻辑需要使用分布式锁
2. 使用分布式锁时，务必在finally块中释放锁
3. 建议设置合理的锁超时时间，防止异常情况下的死锁
4. 分布式锁不是可重入的，同一线程不能多次获取同一个锁
5. 缓存数据可能过期或被清理，业务逻辑需要处理缓存未命中的情况
6. 大量的自增/自减操作可能影响Redis性能，需要监控

---

## 性能优化

1. 使用Redis Cluster提高并发能力
2. 合理设置TTL，避免内存溢出
3. 定期清理过期数据
4. 使用Pipeline提高批量操作效率
5. 监控Redis的内存使用情况

