-- ============================================================
-- Mock Service 数据库表结构
-- 用于支持大规模测试场景
-- ============================================================

-- 切换到观众数据库（用于存储 Mock 服务数据）
USE live_audience_db;

-- ============================================================
-- mock_data_tracking - 模拟数据追踪表
-- 用于记录所有通过 Mock Service 创建的模拟数据的 ID，便于后续批量清理
-- ============================================================
CREATE TABLE IF NOT EXISTS mock_data_tracking (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '追踪记录ID',
    entity_type VARCHAR(50) NOT NULL COMMENT '实体类型: ANCHOR/AUDIENCE/LIVE_ROOM/RECHARGE',
    entity_id BIGINT NOT NULL COMMENT '实体ID（来自各服务的响应）',
    trace_id VARCHAR(100) COMMENT '创建时的traceId',
    batch_id VARCHAR(100) COMMENT '批量创建的批次ID',
    data_snapshot TEXT COMMENT '创建时的数据快照（JSON格式，可选）',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    is_deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否已删除: 0-未删除, 1-已删除',
    deleted_time DATETIME COMMENT '删除时间',
    INDEX idx_entity_type_id (entity_type, entity_id),
    INDEX idx_batch_id (batch_id),
    INDEX idx_trace_id (trace_id),
    INDEX idx_created_time (created_time),
    INDEX idx_is_deleted (is_deleted)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '模拟数据追踪表';

-- ============================================================
-- mock_batch_info - 批次信息表
-- 记录批量创建任务的元信息
-- ============================================================
CREATE TABLE IF NOT EXISTS mock_batch_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '批次记录ID',
    batch_id VARCHAR(100) NOT NULL UNIQUE COMMENT '批次ID（唯一）',
    batch_type VARCHAR(50) NOT NULL COMMENT '批次类型: ANCHOR/AUDIENCE/SIMULATION',
    total_count INT DEFAULT 0 COMMENT '总数量',
    success_count INT DEFAULT 0 COMMENT '成功数量',
    fail_count INT DEFAULT 0 COMMENT '失败数量',
    status VARCHAR(20) COMMENT '状态: RUNNING/SUCCESS/PARTIAL/FAILED',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    error_message TEXT COMMENT '错误信息',
    created_by VARCHAR(50) DEFAULT 'SYSTEM' COMMENT '创建者',
    INDEX idx_batch_type (batch_type),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '批次信息表';

-- ============================================================
-- mock_simulation_task - 模拟任务表
-- 记录行为模拟任务的执行情况
-- ============================================================
CREATE TABLE IF NOT EXISTS mock_simulation_task (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务记录ID',
    task_id VARCHAR(100) NOT NULL UNIQUE COMMENT '任务ID（唯一）',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    audience_count INT DEFAULT 0 COMMENT '参与观众数',
    duration_seconds INT DEFAULT 0 COMMENT '持续时间（秒）',
    simulation_config TEXT COMMENT '模拟配置（JSON格式：进入/离开/弹幕/打赏等）',
    status VARCHAR(20) COMMENT '状态: PENDING/RUNNING/COMPLETED/FAILED/CANCELLED',
    progress INT DEFAULT 0 COMMENT '进度百分比',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    created_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    error_message TEXT COMMENT '错误信息',
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '模拟任务表';

-- ============================================================
-- 创建索引优化查询性能
-- ============================================================

-- 为数据追踪表添加复合索引
ALTER TABLE mock_data_tracking ADD INDEX idx_entity_batch_trace (entity_type, batch_id, trace_id);

-- 为模拟任务表添加复合索引
ALTER TABLE mock_simulation_task ADD INDEX idx_room_status_time (live_room_id, status, created_time);
