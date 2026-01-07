# Mock Service 设计文档 V2.0

## 📋 文档概述

**版本**: 2.0  
**更新日期**: 2026-01-XX  
**服务端口**: 8090  
**访问路径**: http://localhost:8090/mock

本文档描述重构后的 Mock Service 架构设计，采用服务接口调用方式替代直接数据库操作，并实现模拟数据ID的独立管理。

---

## 🎯 设计目标

### 核心目标
1. **服务化调用** - 所有数据操作通过调用各微服务的 REST API 完成，不直接操作数据库
2. **ID 独立管理** - 模拟数据的 ID 单独存储，便于批量清理和追踪
3. **标准化架构** - 完全遵循 Common 模块的架构规范（异常、日志、响应）
4. **可追踪性** - 所有操作使用 TraceId 进行链路追踪
5. **幂等性保证** - 关键操作（打赏、提现等）保证幂等性

---

## 🏗️ 架构设计

### 分层架构

```
┌─────────────────────────────────────────┐
│         REST API Controller             │
│  MockAnchorController                   │
│  MockAudienceController                 │
│  MockLiveRoomController                 │
│  MockSimulationController               │
│  MockDataManagementController           │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│         Service Layer                   │
│  MockAnchorService                      │
│  MockAudienceService                    │
│  MockLiveRoomService                    │
│  MockSimulationService                  │
│  MockDataTrackingService (新增)         │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│       External Service Client           │
│  AnchorServiceClient (Feign)            │
│  AudienceServiceClient (Feign)          │
│  FinanceServiceClient (Feign)           │
│  RedisServiceClient (Feign)             │
└─────────────────┬───────────────────────┘
                  │
┌─────────────────▼───────────────────────┐
│     Local Data Tracking (本地存储)       │
│  MockDataTracking (模拟数据ID追踪表)     │
│  MockDataRepository                     │
└─────────────────────────────────────────┘
```

---

## 📊 数据库设计

### 核心表设计

#### 1. mock_data_tracking - 模拟数据追踪表

用于记录所有通过 Mock Service 创建的模拟数据的 ID，便于后续批量清理。

```sql
CREATE TABLE mock_data_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: ANCHOR/AUDIENCE/LIVE_ROOM/RECHARGE',
    entity_id BIGINT NOT NULL COMMENT '实体ID（来自各服务的响应）',
    trace_id VARCHAR(100) COMMENT '创建时的traceId',
    batch_id VARCHAR(100) COMMENT '批量创建的批次ID',
    data_snapshot JSON COMMENT '创建时的数据快照（可选）',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT DEFAULT 0 COMMENT '是否已删除: 0-未删除, 1-已删除',
    deleted_time DATETIME COMMENT '删除时间',
    
    INDEX idx_entity_type_id (entity_type, entity_id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_created_time (created_time),
    INDEX idx_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟数据追踪表';
```

#### 2. mock_batch_info - 批次信息表

记录批量创建任务的元信息。

```sql
CREATE TABLE mock_batch_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    batch_id VARCHAR(100) UNIQUE NOT NULL COMMENT '批次ID',
    batch_type VARCHAR(50) NOT NULL COMMENT '批次类型: ANCHOR/AUDIENCE/SIMULATION',
    total_count INT DEFAULT 0 COMMENT '总数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    status VARCHAR(20) DEFAULT 'RUNNING' COMMENT '状态: RUNNING/SUCCESS/PARTIAL/FAILED',
    start_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    created_by VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '创建者',
    
    INDEX idx_batch_type (batch_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='批次信息表';
```

#### 3. mock_simulation_task - 模拟任务表

记录行为模拟任务的执行情况。

```sql
CREATE TABLE mock_simulation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    task_id VARCHAR(100) UNIQUE NOT NULL COMMENT '任务ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    audience_count INT DEFAULT 0 COMMENT '参与观众数',
    duration_seconds INT DEFAULT 0 COMMENT '持续时间（秒）',
    simulation_config JSON COMMENT '模拟配置（进入/离开/弹幕/打赏等）',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/RUNNING/COMPLETED/FAILED/CANCELLED',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    error_message TEXT COMMENT '错误信息',
    
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='模拟任务表';
```

---

## 🔌 外部服务接口集成

### 1. 主播服务 (Anchor Service)

#### 接口清单

| 功能 | 方法 | 接口路径 | 说明 |
|------|------|----------|------|
| 创建主播 | POST | `/anchor/api/v1/anchors` | 创建主播账号 |
| 查询主播 | GET | `/anchor/api/v1/anchors/{id}` | 查询主播信息 |
| 更新主播 | PUT | `/anchor/api/v1/anchors/{id}` | 更新主播信息 |
| 查询直播间 | GET | `/anchor/api/v1/live-rooms/anchor/{anchorId}` | 查询主播的直播间 |
| 开启直播 | POST | `/anchor/api/v1/live-rooms/{id}/start` | 开启直播 |
| 结束直播 | POST | `/anchor/api/v1/live-rooms/{id}/end` | 结束直播 |
| 观众进入 | POST | `/anchor/api/v1/live-rooms/realtime/viewer-enter` | 观众进入直播间 |
| 观众离开 | POST | `/anchor/api/v1/live-rooms/realtime/viewer-leave` | 观众离开直播间 |
| 发送弹幕 | POST | `/anchor/api/v1/live-rooms/realtime/danmaku` | 发送弹幕 |

#### Feign Client 定义

```java
@FeignClient(
    name = "anchor-service",
    url = "${mock.service.anchor.url:http://localhost:8081}",
    fallbackFactory = AnchorServiceFallbackFactory.class
)
public interface AnchorServiceClient {
    
    @PostMapping("/anchor/api/v1/anchors")
    BaseResponse<AnchorVO> createAnchor(@RequestBody CreateAnchorRequest request);
    
    @GetMapping("/anchor/api/v1/anchors/{id}")
    BaseResponse<AnchorVO> getAnchor(@PathVariable("id") Long anchorId);
    
    @PostMapping("/anchor/api/v1/live-rooms/{id}/start")
    BaseResponse<LiveRoomVO> startLiveRoom(@PathVariable("id") Long liveRoomId);
    
    @PostMapping("/anchor/api/v1/live-rooms/realtime/viewer-enter")
    BaseResponse<Void> viewerEnter(@RequestBody ViewerEnterRequest request);
    
    @PostMapping("/anchor/api/v1/live-rooms/realtime/danmaku")
    BaseResponse<Void> sendDanmaku(@RequestBody DanmakuRequest request);
    
    // ... 其他接口
}
```

### 2. 观众服务 (Audience Service)

#### 接口清单

| 功能 | 方法 | 接口路径 | 说明 |
|------|------|----------|------|
| 创建观众 | POST | `/audience/api/v1/audiences` | 创建观众账号 |
| 创建游客 | POST | `/audience/api/v1/audiences/guest` | 创建游客账号 |
| 查询观众 | GET | `/audience/api/v1/audiences/{id}` | 查询观众信息 |
| 观众打赏 | POST | `/audience/api/v1/recharge` | 观众打赏主播 |
| 查询打赏 | GET | `/audience/api/v1/recharge/by-trace-id/{traceId}` | 根据traceId查询打赏 |

#### Feign Client 定义

```java
@FeignClient(
    name = "audience-service",
    url = "${mock.service.audience.url:http://localhost:8082}",
    fallbackFactory = AudienceServiceFallbackFactory.class
)
public interface AudienceServiceClient {
    
    @PostMapping("/audience/api/v1/audiences")
    BaseResponse<AudienceVO> createAudience(@RequestBody CreateAudienceRequest request);
    
    @PostMapping("/audience/api/v1/audiences/guest")
    BaseResponse<AudienceVO> createGuestAudience(@RequestBody CreateGuestRequest request);
    
    @GetMapping("/audience/api/v1/audiences/{id}")
    BaseResponse<AudienceVO> getAudience(@PathVariable("id") Long audienceId);
    
    @PostMapping("/audience/api/v1/recharge")
    BaseResponse<RechargeVO> recharge(@RequestBody RechargeRequest request);
    
    // ... 其他接口
}
```

### 3. 财务服务 (Finance Service)

#### 接口清单

| 功能 | 方法 | 接口路径 | 说明 |
|------|------|----------|------|
| 查询余额 | GET | `/finance/api/v1/settlement/{anchorId}/balance` | 查询主播余额 |
| 创建分成比例 | POST | `/finance/api/v1/commission` | 创建分成比例配置 |

### 4. Redis 服务 (Redis Service)

#### 接口清单

| 功能 | 方法 | 接口路径 | 说明 |
|------|------|----------|------|
| 幂等性检查 | POST | `/redis/api/v1/lock/check-idempotency` | 防重复提交 |
| 获取分布式锁 | POST | `/redis/api/v1/lock/try-lock` | 获取锁 |
| 释放分布式锁 | POST | `/redis/api/v1/lock/release-lock` | 释放锁 |

---

## 💼 核心业务流程

### 1. 创建单个主播流程

```
1. 生成 traceId
2. 调用 AnchorService 创建主播
3. 解析响应获取 anchorId
4. 保存到 mock_data_tracking (entity_type='ANCHOR', entity_id=anchorId)
5. 调用 AnchorService 查询主播的直播间信息
6. 保存直播间ID到 mock_data_tracking (entity_type='LIVE_ROOM')
7. 记录 TraceLogger 日志
8. 返回结果
```

### 2. 批量创建观众流程

```
1. 生成 batchId
2. 创建 mock_batch_info 记录
3. 循环创建观众：
   3.1 生成随机观众数据
   3.2 生成 traceId
   3.3 调用 AudienceService 创建观众
   3.4 保存到 mock_data_tracking (entity_type='AUDIENCE', batch_id=batchId)
   3.5 更新批次进度
4. 更新 mock_batch_info 状态
5. 记录 AppLogger 日志
6. 返回批次结果
```

### 3. 行为模拟流程

```
1. 生成 taskId
2. 创建 mock_simulation_task 记录
3. 查询可用的Bot观众列表（从 mock_data_tracking 获取）
4. 异步执行模拟任务：
   4.1 模拟观众进入直播间 (调用 AnchorService)
   4.2 定时模拟发送弹幕 (调用 AnchorService)
   4.3 定时模拟打赏 (调用 AudienceService，带 traceId)
   4.4 保存打赏记录ID到 mock_data_tracking
   4.5 模拟观众离开直播间
5. 更新任务状态
6. 记录完整的 TraceLogger 日志
```

### 4. 批量清理模拟数据流程

```
1. 从 mock_data_tracking 查询待删除的数据ID
2. 按类型分组：
   - RECHARGE: 调用 Finance Service 标记为测试数据（不实际删除）
   - ANCHOR: 调用 Anchor Service 删除或禁用
   - AUDIENCE: 调用 Audience Service 删除或禁用
   - LIVE_ROOM: 自动级联处理
3. 更新 mock_data_tracking 的 is_deleted 标记
4. 记录删除日志
5. 返回清理结果统计
```

---

## 🔧 核心服务实现

### 1. MockDataTrackingService

**职责**: 管理模拟数据的ID追踪

```java
@Service
@Slf4j
public class MockDataTrackingService {
    
    private final MockDataTrackingRepository repository;
    
    /**
     * 追踪单个模拟数据
     */
    public void trackEntity(String entityType, Long entityId, String traceId, String batchId) {
        try {
            MockDataTracking tracking = new MockDataTracking();
            tracking.setEntityType(entityType);
            tracking.setEntityId(entityId);
            tracking.setTraceId(traceId);
            tracking.setBatchId(batchId);
            
            repository.save(tracking);
            
            TraceLogger.info("mock_data", "track_entity", traceId, 
                Map.of("entityType", entityType, "entityId", entityId));
                
        } catch (Exception e) {
            AppLogger.error("追踪模拟数据失败", e);
            throw new BusinessException(ErrorConstants.SYSTEM_ERROR, "追踪模拟数据失败", e);
        }
    }
    
    /**
     * 查询批次的所有数据ID
     */
    public List<MockDataTracking> findByBatchId(String batchId) {
        return repository.findByBatchIdAndIsDeleted(batchId, false);
    }
    
    /**
     * 按类型查询模拟数据
     */
    public List<MockDataTracking> findByEntityType(String entityType) {
        return repository.findByEntityTypeAndIsDeleted(entityType, false);
    }
    
    /**
     * 标记为已删除
     */
    @Transactional
    public void markAsDeleted(List<Long> trackingIds) {
        repository.markAsDeleted(trackingIds, LocalDateTime.now());
        AppLogger.info("标记模拟数据为已删除，数量: {}", trackingIds.size());
    }
}
```

### 2. MockAnchorService

**职责**: 调用主播服务创建模拟主播

```java
@Service
@Slf4j
public class MockAnchorService {
    
    private final AnchorServiceClient anchorClient;
    private final MockDataTrackingService trackingService;
    private final RandomDataGenerator randomGenerator;
    
    /**
     * 创建单个模拟主播
     */
    public MockAnchorResult createMockAnchor(CreateMockAnchorRequest request) {
        String traceId = TraceIdGenerator.generate("MOCK_ANCHOR");
        
        try {
            TraceLogger.start("mock_anchor", "create", traceId);
            
            // 1. 准备主播数据
            CreateAnchorRequest anchorRequest = buildAnchorRequest(request);
            
            // 2. 调用主播服务
            BaseResponse<AnchorVO> response = anchorClient.createAnchor(anchorRequest);
            
            if (!response.isSuccess()) {
                throw new BusinessException(ErrorConstants.SERVICE_CALL_ERROR, 
                    "创建主播失败: " + response.getMessage());
            }
            
            AnchorVO anchor = response.getData();
            Long anchorId = anchor.getId();
            
            // 3. 追踪主播ID
            trackingService.trackEntity("ANCHOR", anchorId, traceId, null);
            
            TraceLogger.success("mock_anchor", "create", traceId, 
                Map.of("anchorId", anchorId, "anchorName", anchor.getName()));
            
            // 4. 获取直播间信息
            BaseResponse<LiveRoomVO> liveRoomResponse = anchorClient.getLiveRoomByAnchorId(anchorId);
            if (liveRoomResponse.isSuccess() && liveRoomResponse.getData() != null) {
                Long liveRoomId = liveRoomResponse.getData().getId();
                trackingService.trackEntity("LIVE_ROOM", liveRoomId, traceId, null);
            }
            
            return MockAnchorResult.builder()
                .anchorId(anchorId)
                .anchorName(anchor.getName())
                .liveRoomId(liveRoomResponse.getData().getId())
                .traceId(traceId)
                .build();
                
        } catch (BusinessException e) {
            TraceLogger.error("mock_anchor", "create", traceId, e);
            throw e;
        } catch (Exception e) {
            TraceLogger.error("mock_anchor", "create", traceId, e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "创建模拟主播异常", e);
        }
    }
    
    /**
     * 批量创建模拟主播
     */
    public MockBatchResult batchCreateMockAnchors(int count) {
        String batchId = "BATCH_ANCHOR_" + System.currentTimeMillis();
        
        AppLogger.info("开始批量创建模拟主播，数量: {}, batchId: {}", count, batchId);
        
        // 创建批次记录
        MockBatchInfo batchInfo = createBatchInfo(batchId, "ANCHOR", count);
        
        List<MockAnchorResult> results = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        
        for (int i = 0; i < count; i++) {
            try {
                CreateMockAnchorRequest request = randomGenerator.generateRandomAnchor();
                MockAnchorResult result = createMockAnchor(request);
                results.add(result);
                successCount++;
                
            } catch (Exception e) {
                failCount++;
                AppLogger.error("批量创建主播失败，索引: {}", e, i);
            }
        }
        
        // 更新批次信息
        updateBatchInfo(batchInfo.getId(), successCount, failCount);
        
        AppLogger.info("批量创建主播完成，成功: {}, 失败: {}", successCount, failCount);
        
        return MockBatchResult.builder()
            .batchId(batchId)
            .totalCount(count)
            .successCount(successCount)
            .failCount(failCount)
            .results(results)
            .build();
    }
}
```

### 3. MockSimulationService

**职责**: 模拟观众行为

```java
@Service
@Slf4j
public class MockSimulationService {
    
    private final AudienceServiceClient audienceClient;
    private final AnchorServiceClient anchorClient;
    private final RedisServiceClient redisClient;
    private final MockDataTrackingService trackingService;
    private final ExecutorService executorService;
    
    /**
     * 启动行为模拟
     */
    public MockSimulationResult startSimulation(MockSimulationRequest request) {
        String taskId = "TASK_" + System.currentTimeMillis();
        String traceId = TraceIdGenerator.generate("SIMULATION");
        
        TraceLogger.start("simulation", "start", traceId);
        
        try {
            // 1. 创建模拟任务记录
            MockSimulationTask task = createSimulationTask(taskId, request);
            
            // 2. 查询可用的Bot观众
            List<MockDataTracking> audiences = trackingService.findByEntityType("AUDIENCE");
            
            if (audiences.size() < request.getAudienceCount()) {
                throw new BusinessException(ErrorConstants.BUSINESS_ERROR, 
                    "可用Bot观众不足，需要: " + request.getAudienceCount() + ", 实际: " + audiences.size());
            }
            
            // 3. 选择参与模拟的观众
            List<Long> selectedAudienceIds = audiences.stream()
                .limit(request.getAudienceCount())
                .map(MockDataTracking::getEntityId)
                .collect(Collectors.toList());
            
            // 4. 异步执行模拟
            executorService.submit(() -> executeSimulation(task, selectedAudienceIds, request));
            
            TraceLogger.success("simulation", "start", traceId, 
                Map.of("taskId", taskId, "audienceCount", selectedAudienceIds.size()));
            
            return MockSimulationResult.builder()
                .taskId(taskId)
                .status("RUNNING")
                .audienceCount(selectedAudienceIds.size())
                .traceId(traceId)
                .build();
                
        } catch (BusinessException e) {
            TraceLogger.error("simulation", "start", traceId, e);
            throw e;
        } catch (Exception e) {
            TraceLogger.error("simulation", "start", traceId, e);
            throw new SystemException(ErrorConstants.SYSTEM_ERROR, "启动模拟任务失败", e);
        }
    }
    
    /**
     * 执行模拟任务
     */
    private void executeSimulation(MockSimulationTask task, List<Long> audienceIds, 
                                   MockSimulationRequest config) {
        String taskId = task.getTaskId();
        
        try {
            AppLogger.info("开始执行模拟任务: {}", taskId);
            
            // 1. 观众进入直播间
            if (config.isSimulateEnter()) {
                simulateViewerEnter(config.getLiveRoomId(), audienceIds);
            }
            
            // 2. 启动弹幕模拟线程
            if (config.isSimulateMessage()) {
                simulateDanmaku(config.getLiveRoomId(), audienceIds, config.getDurationSeconds());
            }
            
            // 3. 启动打赏模拟线程
            if (config.isSimulateRecharge()) {
                simulateRecharge(config.getLiveRoomId(), audienceIds, 
                    config.getDurationSeconds(), config.getRechargeProbability());
            }
            
            // 4. 等待模拟时长
            Thread.sleep(config.getDurationSeconds() * 1000L);
            
            // 5. 观众离开直播间
            if (config.isSimulateLeave()) {
                simulateViewerLeave(config.getLiveRoomId(), audienceIds);
            }
            
            // 6. 更新任务状态
            updateTaskStatus(task.getId(), "COMPLETED", 100);
            
            AppLogger.info("模拟任务执行完成: {}", taskId);
            
        } catch (Exception e) {
            AppLogger.error("模拟任务执行失败: {}", e, taskId);
            updateTaskStatus(task.getId(), "FAILED", 0);
        }
    }
    
    /**
     * 模拟观众打赏
     */
    private void simulateRecharge(Long liveRoomId, List<Long> audienceIds, 
                                  int durationSeconds, int probability) {
        for (Long audienceId : audienceIds) {
            // 按概率决定是否打赏
            if (RandomUtils.nextInt(0, 100) < probability) {
                String traceId = TraceIdGenerator.generate("RECHARGE");
                
                // 幂等性检查
                BaseResponse<Boolean> idempotencyCheck = redisClient.checkIdempotency(traceId, 3600L);
                
                if (Boolean.TRUE.equals(idempotencyCheck.getData())) {
                    try {
                        RechargeRequest request = RechargeRequest.builder()
                            .audienceId(audienceId)
                            .liveRoomId(liveRoomId)
                            .amount(RandomUtils.nextDouble(1.0, 100.0))
                            .rechargeType("COIN")
                            .traceId(traceId)
                            .build();
                        
                        BaseResponse<RechargeVO> response = audienceClient.recharge(request);
                        
                        if (response.isSuccess()) {
                            // 追踪打赏记录
                            trackingService.trackEntity("RECHARGE", 
                                response.getData().getId(), traceId, null);
                        }
                        
                    } catch (Exception e) {
                        AppLogger.error("模拟打赏失败", e);
                    }
                }
            }
            
            // 随机间隔
            ThreadUtils.sleep(RandomUtils.nextLong(5000, 15000));
        }
    }
}
```

---

## 🎨 API 接口设计

### 1. 主播管理接口

#### POST /mock/api/v1/anchor/create - 创建单个模拟主播

**请求体**:
```json
{
  "anchorName": "测试主播01",
  "gender": 0,
  "bio": "这是一个测试主播",
  "tags": ["唱歌", "跳舞"]
}
```

**响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "anchorId": 123456,
    "anchorName": "测试主播01",
    "liveRoomId": 789012,
    "traceId": "MOCK_ANCHOR-20260102-103045-001"
  },
  "timestamp": 1704175845000
}
```

#### POST /mock/api/v1/anchor/batch-create - 批量创建主播

**请求参数**: `count` (int) - 创建数量

**响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "batchId": "BATCH_ANCHOR_1704175845000",
    "totalCount": 10,
    "successCount": 10,
    "failCount": 0,
    "results": [...]
  },
  "timestamp": 1704175845000
}
```

### 2. 观众管理接口

#### POST /mock/api/v1/audience/batch-create-bots - 批量创建Bot观众

**请求体**:
```json
{
  "count": 50,
  "assignRandomTags": true,
  "assignConsumptionLevel": true,
  "malePercentage": 55,
  "minAge": 18,
  "maxAge": 40
}
```

### 3. 行为模拟接口

#### POST /mock/api/v1/simulation/start - 启动行为模拟

**请求体**:
```json
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

#### GET /mock/api/v1/simulation/task/{taskId} - 查询模拟任务状态

**响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "taskId": "TASK_1704175845000",
    "status": "RUNNING",
    "progress": 65,
    "startTime": "2026-01-02 10:30:45",
    "estimatedEndTime": "2026-01-02 10:35:45"
  }
}
```

### 4. 数据管理接口

#### GET /mock/api/v1/data/statistics - 查询模拟数据统计

**响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "totalAnchors": 100,
    "totalAudiences": 5000,
    "totalLiveRooms": 100,
    "totalRecharges": 15000,
    "totalBatches": 10
  }
}
```

#### POST /mock/api/v1/data/cleanup - 清理模拟数据

**请求体**:
```json
{
  "entityTypes": ["ANCHOR", "AUDIENCE", "RECHARGE"],
  "batchIds": ["BATCH_ANCHOR_1704175845000"],
  "beforeDate": "2026-01-01 00:00:00"
}
```

**响应**:
```json
{
  "code": 0,
  "message": "操作成功",
  "data": {
    "deletedAnchors": 10,
    "deletedAudiences": 50,
    "deletedRecharges": 200,
    "totalDeleted": 260
  }
}
```

#### DELETE /mock/api/v1/data/batch/{batchId} - 删除批次数据

---

## 🔐 异常处理

### 异常层次结构

```
BaseException (Common模块)
├── BusinessException (业务异常)
│   ├── 数据不存在
│   ├── 参数验证失败
│   └── 业务规则违反
├── SystemException (系统异常)
│   ├── 服务调用失败
│   ├── 网络超时
│   └── 数据库错误
└── ValidationException (参数校验异常)
```

### 异常处理示例

```java
try {
    BaseResponse<AnchorVO> response = anchorClient.createAnchor(request);
    
    if (!response.isSuccess()) {
        throw new BusinessException(ErrorConstants.SERVICE_CALL_ERROR, 
            "创建主播失败: " + response.getMessage());
    }
    
} catch (FeignException e) {
    TraceLogger.error("mock_anchor", "create", traceId, e);
    throw new SystemException(ErrorConstants.SERVICE_UNAVAILABLE, 
        "主播服务不可用", e);
        
} catch (BusinessException e) {
    throw e;
    
} catch (Exception e) {
    TraceLogger.error("mock_anchor", "create", traceId, e);
    throw new SystemException(ErrorConstants.SYSTEM_ERROR, 
        "创建模拟主播异常", e);
}
```

---

## 📋 日志规范

### 1. AppLogger 使用场景

- 服务启动/关闭
- 批量任务开始/结束
- 定时任务执行
- 资源加载
- 系统配置

```java
AppLogger.logStartup("mock-service", "2.0", 8090);
AppLogger.info("开始批量创建模拟主播，数量: {}, batchId: {}", count, batchId);
AppLogger.logScheduledTask("cleanup-task", true, 1500);
```

### 2. TraceLogger 使用场景

- API 请求处理
- 外部服务调用
- 关键业务操作
- 错误追踪

```java
TraceLogger.start("mock_anchor", "create", traceId);
TraceLogger.info("mock_anchor", "create", traceId, 
    Map.of("anchorId", anchorId, "anchorName", anchorName));
TraceLogger.success("mock_anchor", "create", traceId, resultMap);
TraceLogger.error("mock_anchor", "create", traceId, exception);
```

---

## ⚙️ 配置文件

### application.yml

```yaml
server:
  port: 8090
  servlet:
    context-path: /mock

spring:
  application:
    name: mock-service
  
  datasource:
    url: jdbc:mysql://localhost:3306/live_system?useUnicode=true&characterEncoding=UTF-8
    username: root
    password: ${DB_PASSWORD:root}
  
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false

# Mock Service 配置
mock:
  # 外部服务地址
  service:
    anchor:
      url: http://localhost:8081
    audience:
      url: http://localhost:8082
    finance:
      url: http://localhost:8083
    redis:
      url: http://localhost:8085
  
  # Bot 配置
  bot:
    name-prefix: "Bot_"
    default-batch-size: 50
    max-batch-size: 500
  
  # 行为模拟配置
  simulation:
    enabled: true
    thread-pool-size: 10
    enter-interval-min: 1000
    enter-interval-max: 5000
    message-interval-min: 3000
    message-interval-max: 10000
    recharge-interval-min: 10000
    recharge-interval-max: 30000
  
  # 随机数据配置
  random:
    gender-male-rate: 55
    consumption-low-rate: 60
    consumption-medium-rate: 30
    consumption-high-rate: 10
    recharge-min: 1.0
    recharge-max: 1000.0
    tag-count-min: 1
    tag-count-max: 5

# Feign 配置
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
  httpclient:
    enabled: true
```

---

## 🚀 部署说明

### 1. 依赖服务

Mock Service 启动前需要确保以下服务可用：
- Anchor Service (8081)
- Audience Service (8082)
- Finance Service (8083)
- Redis Service (8085)
- MySQL 数据库

### 2. 启动步骤

```bash
# 1. 初始化数据库表
# SQL 脚本已自动执行（JPA auto-update）

# 2. 启动服务
cd services/mock-service
mvn spring-boot:run

# 3. 验证服务
curl http://localhost:8090/mock/api/v1/data/statistics
```

---

## ✅ 优势与特点

### 相比 V1.0 的改进

1. **服务化架构** - 不直接操作数据库，完全通过服务接口调用
2. **数据可追踪** - 所有模拟数据ID独立存储，便于管理和清理
3. **标准化规范** - 完全遵循 Common 模块的异常、日志、响应规范
4. **幂等性保证** - 关键操作（打赏）使用 traceId 保证幂等性
5. **批次管理** - 批量操作可追踪，支持批次级别的数据清理
6. **链路追踪** - 所有操作都有完整的 TraceLogger 日志
7. **服务降级** - 外部服务调用失败时有降级处理

### 技术亮点

- ✅ Feign Client 服务调用
- ✅ 分布式锁防并发
- ✅ Redis 幂等性检查
- ✅ 异步任务执行
- ✅ 完整的异常体系
- ✅ 标准化日志输出
- ✅ 数据追踪与清理

---

## 📝 注意事项

1. **性能考虑** - 批量创建时建议分批执行，单批不超过 500 个
2. **服务依赖** - 外部服务不可用时会抛出异常，注意服务启动顺序
3. **数据清理** - 模拟数据会产生大量记录，建议定期清理
4. **幂等性** - 打赏等关键操作使用 traceId，重复调用不会产生多条记录
5. **线程池** - 模拟任务使用线程池，注意控制并发数量
6. **TraceId 规范** - 所有 traceId 必须唯一，建议使用时间戳+随机数

---

## 📚 相关文档

- [Common 模块功能文档](../common/docs/功能文档.md)
- [主播服务 API 文档](../anchor-service/README.md)
- [观众服务 API 文档](../audience-service/README.md)
- [Nginx 接口汇总](../nginx/NGINX接口汇总.md)

---

**文档版本**: 2.0  
**最后更新**: 2026-01-XX  
**维护者**: 开发团队

