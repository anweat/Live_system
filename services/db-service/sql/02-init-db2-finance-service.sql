-- ============================================================
-- DB2: 财务&分析服务数据库初始化脚本
-- 包含分成结算、提现、统计分析表
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS live_finance_db CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE live_finance_db;

-- ============================================================
-- 1. commission_rate - 分成比例表
-- ============================================================
CREATE TABLE IF NOT EXISTS commission_rate (
    commission_rate_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分成比例ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    anchor_name VARCHAR(128) NOT NULL COMMENT '主播名称(冗余，方便查询)',
    commission_rate DECIMAL(5, 2) NOT NULL COMMENT '分成比例(%)',
    effective_time DATETIME NOT NULL COMMENT '生效时间',
    expire_time DATETIME COMMENT '过期时间(null表示未过期)',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-未启用、1-启用、2-已过期',
    remark VARCHAR(500) COMMENT '操作备注(为什么调整)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_effective_time (effective_time),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '分成比例表，支持历史版本追踪';

-- ============================================================
-- 2. settlement - 结算表
-- ============================================================
CREATE TABLE IF NOT EXISTS settlement (
    settlement_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结算ID',
    anchor_id BIGINT NOT NULL UNIQUE COMMENT '主播ID(一一对应)',
    anchor_name VARCHAR(128) NOT NULL COMMENT '主播名称',
    settlement_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '结算金额(应付)',
    withdrawn_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '已提取金额',
    available_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '可提取金额(结算-已提取)',
    settlement_cycle INT NOT NULL DEFAULT 1 COMMENT '结算周期(天数)',
    last_settlement_time DATETIME COMMENT '上次结算时间',
    next_settlement_time DATETIME COMMENT '下次结算时间',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-正常、1-冻结、2-禁提',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_status (status),
    INDEX idx_next_settlement_time (next_settlement_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '结算表，记录主播的可领取余额';

-- ============================================================
-- 3. settlement_detail - 结算明细表
-- ============================================================
CREATE TABLE IF NOT EXISTS settlement_detail (
    detail_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    settlement_id BIGINT NOT NULL COMMENT '结算ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    total_recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '本期打赏总额',
    commission_rate DECIMAL(5, 2) NOT NULL COMMENT '本期分成比例',
    settlement_amount DECIMAL(15, 2) NOT NULL COMMENT '本期应付金额(总额 * 比例)',
    settlement_start_time DATETIME NOT NULL COMMENT '结算开始时间',
    settlement_end_time DATETIME NOT NULL COMMENT '结算结束时间',
    recharge_count INT NOT NULL DEFAULT 0 COMMENT '打赏笔数',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-已结算、1-已对账、2-已审核',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (settlement_id) REFERENCES settlement (settlement_id) ON DELETE CASCADE,
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_settlement_start_time (settlement_start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '结算明细表，支持按期查询结算情况';

-- ============================================================
-- 4. withdrawal - 提现记录表
-- ============================================================
CREATE TABLE IF NOT EXISTS withdrawal (
    withdrawal_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提现ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    anchor_name VARCHAR(128) NOT NULL COMMENT '主播名称',
    withdrawal_amount DECIMAL(15, 2) NOT NULL COMMENT '提现金额',
    withdrawal_type INT NOT NULL DEFAULT 0 COMMENT '提现方式：0-银行卡、1-支付宝、2-微信',
    bank_name VARCHAR(100) COMMENT '开户行',
    bank_card_encrypted VARCHAR(255) COMMENT '银行卡号(加密存储)',
    account_holder VARCHAR(128) COMMENT '账户持有人名称',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-申请中、1-处理中、2-已打款、3-失败、4-已拒绝',
    trace_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'traceId(幂等性控制)',
    reject_reason VARCHAR(500) COMMENT '拒绝原因',
    transfer_serial_number VARCHAR(100) COMMENT '转账流水号',
    applied_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    processed_time DATETIME COMMENT '处理时间',
    version INT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_trace_id (trace_id),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_status (status),
    INDEX idx_applied_time (applied_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提现记录表';

-- ============================================================
-- 5. hourly_statistics - 小时统计表
-- ============================================================
CREATE TABLE IF NOT EXISTS hourly_statistics (
    stat_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '统计ID',
    statistic_hour DATETIME NOT NULL COMMENT '统计小时(精确到小时)',
    anchor_id BIGINT COMMENT '主播ID',
    anchor_name VARCHAR(128) COMMENT '主播名称',
    anchor_gender INT COMMENT '主播性别',
    audience_gender INT COMMENT '打赏观众性别',
    total_recharge_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '打赏总金额',
    recharge_count INT NOT NULL DEFAULT 0 COMMENT '打赏笔数',
    unique_payer_count INT NOT NULL DEFAULT 0 COMMENT '付款人数',
    max_single_amount DECIMAL(15, 2) COMMENT '单笔最大金额',
    avg_single_amount DECIMAL(15, 2) COMMENT '单笔平均金额',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_statistic_hour (statistic_hour),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_anchor_gender (anchor_gender),
    INDEX idx_audience_gender (audience_gender),
    INDEX idx_total_amount (total_recharge_amount),
    UNIQUE KEY uk_stat_dimension (
        statistic_hour,
        anchor_id,
        anchor_gender,
        audience_gender
    )
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '小时统计表，支持多维度查询';

-- ============================================================
-- 6. audience_portrait - 观众画像表
-- ============================================================
CREATE TABLE IF NOT EXISTS audience_portrait (
    portrait_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '画像ID',
    audience_id BIGINT NOT NULL UNIQUE COMMENT '观众ID(一一对应)',
    audience_name VARCHAR(128) NOT NULL COMMENT '观众名称',
    total_recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '累计打赏金额',
    recharge_count INT NOT NULL DEFAULT 0 COMMENT '打赏笔数',
    consumption_percentile INT NOT NULL COMMENT '消费分位数(0-100)',
    consumption_level INT NOT NULL DEFAULT 1 COMMENT '消费等级：0-低(后20%)、1-中(20%-80%)、2-高(前20%)',
    portrait_description VARCHAR(500) COMMENT '画像描述(文字说明)',
    favorite_anchor_ids VARCHAR(1000) COMMENT '最喜欢的主播IDs(JSON)',
    favorite_categories VARCHAR(500) COMMENT '偏好的直播类别(JSON)',
    last_recharge_time DATETIME COMMENT '最后打赏时间',
    first_recharge_time DATETIME COMMENT '首次打赏时间',
    activity_level INT NOT NULL DEFAULT 0 COMMENT '活跃度等级：0-低、1-中、2-高',
    retention_days INT NOT NULL DEFAULT 0 COMMENT '留存天数',
    prediction_ltv DECIMAL(15, 2) COMMENT '预测生命周期价值(LTV)',
    calc_time DATETIME NOT NULL COMMENT '计算时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_audience_id (audience_id),
    INDEX idx_consumption_level (consumption_level),
    INDEX idx_consumption_percentile (consumption_percentile),
    INDEX idx_calc_time (calc_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '观众画像表，支持消费人群分层和标签化';

-- ============================================================
-- 7. recharge_record - 打赏记录表（财务服务持久化）
-- ============================================================
CREATE TABLE IF NOT EXISTS recharge_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    original_recharge_id BIGINT NOT NULL COMMENT '原始打赏记录ID(来自DB1)',
    trace_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'traceId(全局唯一，幂等性控制)',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    anchor_name VARCHAR(128) NOT NULL COMMENT '主播名称',
    audience_id BIGINT NOT NULL COMMENT '观众ID',
    audience_name VARCHAR(128) NOT NULL COMMENT '观众名称',
    recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '打赏金额',
    recharge_time DATETIME NOT NULL COMMENT '打赏时间',
    recharge_type INT DEFAULT 0 COMMENT '打赏类型：0-普通打赏、1-礼物打赏',
    live_room_id BIGINT COMMENT '直播间ID',
    sync_batch_id VARCHAR(64) NOT NULL COMMENT '同步批次ID',
    settlement_status INT NOT NULL DEFAULT 0 COMMENT '结算状态：0-待结算、1-已结算、2-已提现',
    applied_commission_rate DECIMAL(5, 2) COMMENT '应用的分成比例(%)',
    settlement_amount DECIMAL(15, 2) COMMENT '结算金额(打赏金额 * 分成比例)',
    settlement_time DATETIME COMMENT '结算时间',
    source_service VARCHAR(50) NOT NULL COMMENT '数据源服务',
    received_time DATETIME NOT NULL COMMENT '接收时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_trace_id (trace_id),
    INDEX idx_original_recharge_id (original_recharge_id),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_audience_id (audience_id),
    INDEX idx_recharge_time (recharge_time),
    INDEX idx_sync_batch_id (sync_batch_id),
    INDEX idx_settlement_status (settlement_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '打赏记录表（财务服务持久化），用于结算计算和统计分析';

-- ============================================================
-- 8. sync_progress - 数据同步进度表
-- ============================================================
CREATE TABLE IF NOT EXISTS sync_progress (
    progress_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '进度ID',
    sync_type INT NOT NULL COMMENT '同步类型：0-打赏数据同步、1-用户数据同步',
    source_service VARCHAR(50) NOT NULL COMMENT '数据源服务',
    target_service VARCHAR(50) NOT NULL COMMENT '目标服务',
    last_sync_recharge_id BIGINT NOT NULL DEFAULT 0 COMMENT '最后同步的打赏记录ID',
    total_synced_count BIGINT NOT NULL DEFAULT 0 COMMENT '累计同步笔数',
    total_synced_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '累计同步金额',
    last_sync_time DATETIME COMMENT '最后同步时间',
    next_sync_time DATETIME COMMENT '下次同步时间',
    sync_status INT NOT NULL DEFAULT 0 COMMENT '状态：0-待同步、1-同步中、2-已同步、3-失败',
    error_message VARCHAR(500) COMMENT '错误信息',
    sync_interval_seconds INT NOT NULL DEFAULT 300 COMMENT '同步间隔(秒)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_sync_type (sync_type),
    INDEX idx_last_sync_time (last_sync_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据同步进度表，记录观众服务和财务分析服务的同步状态';

-- ============================================================
-- 创建索引完成
-- ============================================================
COMMIT;