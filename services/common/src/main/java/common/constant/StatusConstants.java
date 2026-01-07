package common.constant;

/**
 * 状态常量定义
 * 包含系统中各种状态的定义
 */
public class StatusConstants {

    // ==================== 用户相关状态 ====================

    /**
     * 用户类型
     */
    public static class UserType {
        /** 游客 */
        public static final int GUEST = 0;
        /** 注册用户 */
        public static final int REGISTERED = 1;
    }

    /**
     * 用户性别
     */
    public static class Gender {
        /** 未知 */
        public static final int UNKNOWN = 0;
        /** 男 */
        public static final int MALE = 1;
        /** 女 */
        public static final int FEMALE = 2;
    }

    /**
     * 账户状态
     */
    public static class AccountStatus {
        /** 正常 */
        public static final int NORMAL = 0;
        /** 禁用 */
        public static final int DISABLED = 1;
        /** 禁言 */
        public static final int MUTED = 2;
        /** 封禁 */
        public static final int BANNED = 3;
    }

    // ==================== 直播间相关状态 ====================

    /**
     * 直播间状态
     */
    public static class RoomStatus {
        /** 未开播 */
        public static final int OFFLINE = 0;
        /** 直播中 */
        public static final int LIVE = 1;
        /** 直播结束 */
        public static final int FINISHED = 2;
        /** 被封禁 */
        public static final int BANNED = 3;
    }

    /**
     * 消息类型
     */
    public static class MessageType {
        /** 普通消息 */
        public static final int NORMAL = 0;
        /** 系统消息 */
        public static final int SYSTEM = 1;
        /** 礼物消息 */
        public static final int GIFT = 2;
        /** 公告 */
        public static final int ANNOUNCEMENT = 3;
    }

    // ==================== 打赏相关状态 ====================

    /**
     * 打赏类型
     */
    public static class RechargeType {
        /** 普通打赏 */
        public static final int NORMAL = 0;
        /** 礼物 */
        public static final int GIFT = 1;
        /** 跳过广告 */
        public static final int SKIP_AD = 2;
    }

    /**
     * 打赏状态
     */
    public static class RechargeStatus {
        /** 已入账 */
        public static final int CREDITED = 0;
        /** 待结算 */
        public static final int PENDING = 1;
        /** 已结算 */
        public static final int SETTLED = 2;
        /** 已退款 */
        public static final int REFUNDED = 3;
    }

    // ==================== 提现相关状态 ====================

    /**
     * 提现状态
     */
    public static class WithdrawalStatus {
        /** 申请中 */
        public static final int APPLYING = 0;
        /** 处理中 */
        public static final Integer PROCESSING = 1;
        /** 转账中 */
        public static final int TRANSFERRING = 2;
        /** 已完成 */
        public static final int COMPLETED = 3;
        /** 已拒绝 */
        public static final int REJECTED = 4;


    }

    /**
     * 提现方式
     */
    public static class WithdrawalType {
        /** 银行卡 */
        public static final int BANK_CARD = 0;
        /** 支付宝 */
        public static final int ALIPAY = 1;
        /** 微信 */
        public static final int WECHAT = 2;
    }

    // ==================== 结算相关状态 ====================

    /**
     * 结算状态
     */
    public static class SettlementStatus {
        /** 待结算 */
        public static final int PENDING = 0;
        /** 结算中 */
        public static final int PROCESSING = 1;
        /** 已结算 */
        public static final int COMPLETED = 2;
        /** 结算失败 */
        public static final int FAILED = 3;
    }

    /**
     * 结算方式
     */
    public static class SettlementMethod {
        /** 每小时结算 */
        public static final int HOURLY = 0;
        /** 每天结算 */
        public static final int DAILY = 1;
        /** 每周结算 */
        public static final int WEEKLY = 2;
        /** 每月结算 */
        public static final int MONTHLY = 3;
    }

    // ==================== 消费等级 ====================

    /**
     * 消费等级
     */
    public static class ConsumptionLevel {
        /** 低消费 */
        public static final int LOW = 0;
        /** 中消费 */
        public static final int MEDIUM = 1;
        /** 高消费 */
        public static final int HIGH = 2;
    }

    /**
     * VIP等级
     */
    public static class VipLevel {
        /** 普通用户 */
        public static final int NORMAL = 0;
        /** 铁粉 */
        public static final int IRON_FAN = 1;
        /** 银粉 */
        public static final int SILVER_FAN = 2;
        /** 金粉 */
        public static final int GOLD_FAN = 3;
        /** 超级粉丝 */
        public static final int SUPER_FAN = 4;
    }

    // ==================== 主播认证相关 ====================

    /**
     * 认证状态
     */
    public static class VerificationStatus {
        /** 未认证 */
        public static final int UNVERIFIED = 0;
        /** 已认证 */
        public static final int VERIFIED = 1;
        /** 认证中 */
        public static final int VERIFYING = 2;
    }

    /**
     * 主播等级
     */
    public static class AnchorLevel {
        /** 初级主播 */
        public static final int JUNIOR = 0;
        /** 中级主播 */
        public static final int INTERMEDIATE = 1;
        /** 高级主播 */
        public static final int SENIOR = 2;
        /** VIP主播 */
        public static final int VIP = 3;
    }

    // ==================== 通用标志 ====================

    /**
     * 删除标志
     */
    public static class IsDeleted {
        /** 未删除 */
        public static final int NO = 0;
        /** 已删除 */
        public static final int YES = 1;
    }

    /**
     * 启用标志
     */
    public static class IsEnabled {
        /** 禁用 */
        public static final int DISABLED = 0;
        /** 启用 */
        public static final int ENABLED = 1;
    }
}
