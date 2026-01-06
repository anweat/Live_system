-- ============================================================
-- Mock Service 数据库表结构
-- 用于支持大规模测试场景
-- ============================================================

USE live_audience_db;

-- ============================================================
-- mock_user - Mock用户表（Bot存储）
-- ============================================================
CREATE TABLE IF NOT EXISTS mock_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    nickname VARCHAR(128) NOT NULL COMMENT '昵称',
    gender INT NOT NULL COMMENT '性别：0-女、1-男',
    age INT COMMENT '年龄',
    avatar_url VARCHAR(200) COMMENT '头像URL',
    is_bot TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否为Bot：0-否、1-是',
    consumption_level INT NOT NULL DEFAULT 0 COMMENT '消费等级：0-低、1-中、2-高',
    tags VARCHAR(500) COMMENT '标签JSON数组',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用、1-启用',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_is_bot (is_bot),
    INDEX idx_consumption_level (consumption_level),
    INDEX idx_gender (gender),
    INDEX idx_status (status)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Mock用户表（Bot存储）';

-- ============================================================
-- mock_live_room - Mock直播间表
-- ============================================================
CREATE TABLE IF NOT EXISTS mock_live_room (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '直播间ID',
    anchor_id BIGINT COMMENT '主播ID',
    title VARCHAR(255) NOT NULL COMMENT '直播间标题',
    description VARCHAR(1000) COMMENT '描述',
    category VARCHAR(50) COMMENT '分类',
    tags VARCHAR(500) COMMENT '标签JSON数组',
    cover_url VARCHAR(200) COMMENT '封面URL',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-未开播、1-直播中、2-已结束',
    current_audience_count INT NOT NULL DEFAULT 0 COMMENT '当前观众数',
    total_audience_count INT NOT NULL DEFAULT 0 COMMENT '累计观众数',
    start_time DATETIME COMMENT '开播时间',
    end_time DATETIME COMMENT '结束时间',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_anchor_id (anchor_id),
    INDEX idx_status (status),
    INDEX idx_current_audience_count (current_audience_count)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'Mock直播间表';

-- ============================================================
-- 创建索引优化查询性能
-- ============================================================

-- 为标签查询创建全文索引（MySQL 5.7+）
-- ALTER TABLE mock_user ADD FULLTEXT INDEX ft_tags(tags);
-- ALTER TABLE mock_live_room ADD FULLTEXT INDEX ft_tags(tags);

COMMIT;
