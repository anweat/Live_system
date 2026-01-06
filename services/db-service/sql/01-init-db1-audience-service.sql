-- ============================================================
-- DB1: 观众服务数据库初始化脚本
-- 包含用户、主播、观众、直播间、打赏记录、标签等表
-- ============================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS live_audience_db CHARACTER
SET
    utf8mb4 COLLATE utf8mb4_unicode_ci;

USE live_audience_db;

-- ============================================================
-- 1. user - 用户基础表
-- ============================================================
CREATE TABLE IF NOT EXISTS user (
    user_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    user_type INT NOT NULL DEFAULT 0 COMMENT '用户类型：0-游客、1-注册用户',
    gender INT NOT NULL DEFAULT 0 COMMENT '性别：0-未知、1-男、2-女',
    age INT COMMENT '年龄',
    username VARCHAR(64) UNIQUE COMMENT '用户名(注册用户)',
    nickname VARCHAR(128) NOT NULL COMMENT '昵称/姓名',
    password_hash VARCHAR(255) COMMENT '密码哈希值(注册用户)',
    email VARCHAR(128) UNIQUE COMMENT '邮箱',
    phone_number VARCHAR(20) UNIQUE COMMENT '电话号码',
    avatar_url VARCHAR(500) COMMENT '头像URL',
    bio VARCHAR(500) COMMENT '个人简介',
    ip_location VARCHAR(255) COMMENT 'IP地理位置',
    account_status INT NOT NULL DEFAULT 0 COMMENT '账户状态：0-正常、1-禁用、2-禁言、3-封禁',
    is_deleted INT NOT NULL DEFAULT 0 COMMENT '是否删除：0-未删除、1-已删除',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_type (user_type),
    INDEX idx_username (username),
    INDEX idx_account_status (account_status),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户基础表';

-- ============================================================
-- 2. anchor - 主播信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS anchor (
    user_id BIGINT PRIMARY KEY COMMENT '主播ID(外键引用user)',
    live_room_id BIGINT UNIQUE COMMENT '直播间ID(一一对应)',
    anchor_level INT NOT NULL DEFAULT 0 COMMENT '主播等级(影响分成)',
    like_count BIGINT NOT NULL DEFAULT 0 COMMENT '点赞数',
    fan_count BIGINT NOT NULL DEFAULT 0 COMMENT '粉丝数',
    available_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '可提取余额',
    total_earnings DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '累计收益',
    current_commission_rate DECIMAL(5, 2) NOT NULL DEFAULT 50 COMMENT '当前分成比例(%)',
    banned_until DATETIME COMMENT '封禁截止时间(null表示未被封禁)',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_fan_count (fan_count),
    INDEX idx_total_earnings (total_earnings)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '主播信息表';

-- ============================================================
-- 3. audience - 观众信息表
-- ============================================================
CREATE TABLE IF NOT EXISTS audience (
    user_id BIGINT PRIMARY KEY COMMENT '观众ID(外键引用user)',
    consumption_level INT NOT NULL DEFAULT 1 COMMENT '消费等级：0-低(后20%)、1-中(20%-80%)、2-高(前20%)',
    total_recharge_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '累计打赏金额',
    total_recharge_count BIGINT NOT NULL DEFAULT 0 COMMENT '累计打赏次数',
    last_recharge_time DATETIME COMMENT '最后打赏时间',
    vip_level INT NOT NULL DEFAULT 0 COMMENT '粉丝等级：0-普通、1-铁粉、2-银粉、3-金粉、4-超级粉丝',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user (user_id) ON DELETE CASCADE,
    INDEX idx_consumption_level (consumption_level),
    INDEX idx_total_recharge_amount (total_recharge_amount),
    INDEX idx_last_recharge_time (last_recharge_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '观众信息表';

-- ============================================================
-- 4. live_room - 直播间基础表
-- ============================================================
CREATE TABLE IF NOT EXISTS live_room (
    live_room_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '直播间ID',
    anchor_id BIGINT NOT NULL UNIQUE COMMENT '主播ID(唯一对应)',
    anchor_name VARCHAR(128) NOT NULL COMMENT '主播名称',
    room_name VARCHAR(255) NOT NULL COMMENT '直播间名称',
    description VARCHAR(1000) COMMENT '直播间描述',
    room_status INT NOT NULL DEFAULT 0 COMMENT '状态：0-未开播、1-直播中、2-直播结束、3-被封禁',
    category VARCHAR(50) COMMENT '直播间分类：游戏、娱乐、户外等',
    cover_url VARCHAR(500) COMMENT '封面图URL',
    stream_url VARCHAR(500) COMMENT '直播流URL',
    max_viewers INT NOT NULL DEFAULT 10000 COMMENT '最大容纳观众数',
    start_time DATETIME COMMENT '上次开播时间',
    end_time DATETIME COMMENT '上次关播时间',
    total_earnings DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '累计收益',
    total_viewers BIGINT NOT NULL DEFAULT 0 COMMENT '累计观看人次',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (anchor_id) REFERENCES anchor (user_id) ON DELETE CASCADE,
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_room_status (room_status),
    INDEX idx_category (category),
    INDEX idx_start_time (start_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '直播间基础表';

-- ============================================================
-- 5. live_room_realtime - 直播间实时数据表
-- ============================================================
CREATE TABLE IF NOT EXISTS live_room_realtime (
    realtime_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '实时数据ID',
    live_room_id BIGINT NOT NULL UNIQUE COMMENT '直播间ID(一一对应)',
    current_viewer_count INT NOT NULL DEFAULT 0 COMMENT '当前观众数',
    current_revenue_amount DECIMAL(15, 2) NOT NULL DEFAULT 0 COMMENT '当前场次收益',
    message_count BIGINT NOT NULL DEFAULT 0 COMMENT '弹幕数',
    recharge_count BIGINT NOT NULL DEFAULT 0 COMMENT '打赏数',
    online_duration BIGINT NOT NULL DEFAULT 0 COMMENT '在线时长(秒)',
    avg_latency INT NOT NULL DEFAULT 0 COMMENT '平均延迟(ms)',
    max_latency INT NOT NULL DEFAULT 0 COMMENT '最大延迟(ms)',
    version BIGINT NOT NULL DEFAULT 0 COMMENT '版本号(乐观锁)',
    last_update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    FOREIGN KEY (live_room_id) REFERENCES live_room (live_room_id) ON DELETE CASCADE,
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_current_viewer_count (current_viewer_count)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '直播间实时数据表，频繁更新';

-- ============================================================
-- 6. live_session_audience - 直播会话观众表
-- ============================================================
CREATE TABLE IF NOT EXISTS live_session_audience (
    session_audience_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '会话观众ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    audience_id BIGINT NOT NULL COMMENT '观众ID',
    join_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '进入时间',
    leave_time DATETIME COMMENT '离开时间',
    watch_duration INT DEFAULT 0 COMMENT '观看时长(秒)',
    FOREIGN KEY (live_room_id) REFERENCES live_room (live_room_id) ON DELETE CASCADE,
    FOREIGN KEY (audience_id) REFERENCES audience (user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_session (
        live_room_id,
        audience_id,
        join_time
    ),
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_audience_id (audience_id),
    INDEX idx_join_time (join_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '直播会话观众表，用于管理在线观众列表';

-- ============================================================
-- 7. message - 弹幕消息表
-- ============================================================
CREATE TABLE IF NOT EXISTS message (
    message_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '消息ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    sender_id BIGINT NOT NULL COMMENT '发送者ID',
    content VARCHAR(500) NOT NULL COMMENT '消息内容',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (live_room_id) REFERENCES live_room (live_room_id) ON DELETE CASCADE,
    FOREIGN KEY (sender_id) REFERENCES audience (user_id) ON DELETE CASCADE,
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_sender_id (sender_id),
    INDEX idx_create_time (create_time)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '弹幕消息表';

-- ============================================================
-- 8. recharge - 打赏明细记录表（核心业务表）
-- ============================================================
CREATE TABLE IF NOT EXISTS recharge (
    recharge_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '打赏ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID',
    audience_id BIGINT NOT NULL COMMENT '观众ID',
    recharge_amount DECIMAL(15, 2) NOT NULL COMMENT '打赏金额',
    recharge_type INT NOT NULL DEFAULT 0 COMMENT '打赏类型：0-普通、1-礼物、2-特殊',
    message VARCHAR(500) COMMENT '打赏消息',
    trace_id VARCHAR(64) NOT NULL UNIQUE COMMENT 'traceId(链路追踪&幂等控制)',
    status INT NOT NULL DEFAULT 0 COMMENT '状态：0-已入账、1-待结算、2-已结算、3-已退款',
    settlement_id BIGINT COMMENT '对应的结算ID',
    recharge_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '打赏时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (live_room_id) REFERENCES live_room (live_room_id) ON DELETE CASCADE,
    FOREIGN KEY (anchor_id) REFERENCES anchor (user_id) ON DELETE CASCADE,
    FOREIGN KEY (audience_id) REFERENCES audience (user_id) ON DELETE CASCADE,
    UNIQUE KEY uk_trace_id (trace_id),
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_audience_id (audience_id),
    INDEX idx_recharge_time (recharge_time),
    INDEX idx_status (status),
    INDEX idx_settlement_id (settlement_id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '打赏明细记录表，核心业务表';

-- ============================================================
-- 9. tag - 标签表
-- ============================================================
CREATE TABLE IF NOT EXISTS tag (
    tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '标签ID',
    tag_name VARCHAR(100) NOT NULL UNIQUE COMMENT '标签名称',
    description VARCHAR(500) COMMENT '标签描述',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_tag_name (tag_name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签表，支持预设和自定义标签';

-- ============================================================
-- 10. tag_relation - 标签关联度表
-- ============================================================
CREATE TABLE IF NOT EXISTS tag_relation (
    relation_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    tag_id1 BIGINT NOT NULL COMMENT '标签1 ID',
    tag_id2 BIGINT NOT NULL COMMENT '标签2 ID',
    relation_type INT NOT NULL DEFAULT 0 COMMENT '关联类型：0-推荐关联、1-互斥关联、2-强相关',
    relation_score DECIMAL(5, 2) NOT NULL COMMENT '关联度分数(0-100)',
    cooccurrence_count BIGINT NOT NULL DEFAULT 0 COMMENT '共同出现次数',
    strength_level INT NOT NULL DEFAULT 1 COMMENT '关联强度等级：1-弱、2-中、3-强',
    last_calc_time DATETIME COMMENT '最后计算时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (tag_id1) REFERENCES tag (tag_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id2) REFERENCES tag (tag_id) ON DELETE CASCADE,
    UNIQUE KEY uk_tag_relation (tag_id1, tag_id2),
    INDEX idx_relation_score (relation_score),
    INDEX idx_relation_type (relation_type),
    INDEX idx_strength_level (strength_level)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '标签关联度表，用于推荐和分析';

-- ============================================================
-- 11. anchor_tag - 主播标签关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS anchor_tag (
    anchor_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    anchor_id BIGINT NOT NULL COMMENT '主播ID(外键)',
    tag_id BIGINT NOT NULL COMMENT '标签ID(外键)',
    tag_weight DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '标签权重(0-100)，用于显示主播倾向',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (anchor_id) REFERENCES anchor (user_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag (tag_id) ON DELETE CASCADE,
    UNIQUE KEY uk_anchor_tag (anchor_id, tag_id),
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_tag_weight (tag_weight)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '主播标签关联表，存储主播的标签和权重';

-- ============================================================
-- 12. audience_tag - 观众标签关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS audience_tag (
    audience_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    audience_id BIGINT NOT NULL COMMENT '观众ID(外键)',
    tag_id BIGINT NOT NULL COMMENT '标签ID(外键)',
    tag_weight DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '标签权重(0-100)，用于显示观众倾向',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (audience_id) REFERENCES audience (user_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag (tag_id) ON DELETE CASCADE,
    UNIQUE KEY uk_audience_tag (audience_id, tag_id),
    INDEX idx_audience_id (audience_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_tag_weight (tag_weight)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '观众标签关联表，存储观众的标签和权重';

-- ============================================================
-- 13. live_room_tag - 直播间标签关联表
-- ============================================================
CREATE TABLE IF NOT EXISTS live_room_tag (
    live_room_tag_id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '关联ID',
    live_room_id BIGINT NOT NULL COMMENT '直播间ID(外键)',
    tag_id BIGINT NOT NULL COMMENT '标签ID(外键)',
    tag_weight DECIMAL(5, 2) NOT NULL DEFAULT 0 COMMENT '标签权重(0-100)，用于显示直播间倾向',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (live_room_id) REFERENCES live_room (live_room_id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tag (tag_id) ON DELETE CASCADE,
    UNIQUE KEY uk_live_room_tag (live_room_id, tag_id),
    INDEX idx_live_room_id (live_room_id),
    INDEX idx_tag_id (tag_id),
    INDEX idx_tag_weight (tag_weight)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '直播间标签关联表，存储直播间的标签和权重';

-- ============================================================
-- 14. sync_progress - 数据同步进度表
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
    INDEX idx_source_service (source_service),
    INDEX idx_last_sync_time (last_sync_time),
    INDEX idx_status (sync_status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据同步进度表，记录观众服务和财务分析服务的同步状态';

-- ============================================================
-- 创建索引完成
-- ============================================================
COMMIT;