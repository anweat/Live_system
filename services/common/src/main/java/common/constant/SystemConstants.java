package common.constant;

/**
 * 系统常量定义
 */
public class SystemConstants {

    // ==================== 系统信息 ====================

    /** 系统名称 */
    public static final String SYSTEM_NAME = "Live Tipping System";

    /** 系统版本 */
    public static final String SYSTEM_VERSION = "1.0.0";

    // ==================== 分页常量 ====================

    /** 默认页码 */
    public static final int DEFAULT_PAGE_NO = 1;

    /** 默认页大小 */
    public static final int DEFAULT_PAGE_SIZE = 20;

    /** 最大页大小 */
    public static final int MAX_PAGE_SIZE = 1000;

    /** 最小页大小 */
    public static final int MIN_PAGE_SIZE = 1;

    // ==================== 批处理常量 ====================

    /** 批处理默认大小 */
    public static final int BATCH_SIZE = 500;

    /** 批处理最大大小 */
    public static final int MAX_BATCH_SIZE = 5000;

    // ==================== 数据库常量 ====================

    /** 数据库主键ID最小值 */
    public static final long MIN_ID = 1L;

    /** 数据库主键ID最大值 */
    public static final long MAX_ID = Long.MAX_VALUE;

    // ==================== 金额相关常量 ====================

    /** 最小打赏金额 (0.01元) */
    public static final java.math.BigDecimal MIN_RECHARGE_AMOUNT = new java.math.BigDecimal("0.01");

    /** 最大打赏金额 (99999.99元) */
    public static final java.math.BigDecimal MAX_RECHARGE_AMOUNT = new java.math.BigDecimal("99999.99");

    /** 最小提现金额 (1元) */
    public static final java.math.BigDecimal MIN_WITHDRAWAL_AMOUNT = new java.math.BigDecimal("1.00");

    /** 最大提现金额 (100000元) */
    public static final java.math.BigDecimal MAX_WITHDRAWAL_AMOUNT = new java.math.BigDecimal("100000.00");

    /** 金额精度 (小数点后2位) */
    public static final int AMOUNT_SCALE = 2;

    // ==================== 超时相关常量 ====================

    /** 默认超时时间 (秒) */
    public static final long DEFAULT_TIMEOUT = 30;

    /** 数据库操作超时 (秒) */
    public static final long DB_TIMEOUT = 30;

    /** HTTP 请求超时 (秒) */
    public static final long HTTP_TIMEOUT = 30;

    /** 缓存过期时间 (小时) */
    public static final long CACHE_EXPIRE_HOURS = 24;

    // ==================== 长度限制 ====================

    /** 用户名最大长度 */
    public static final int USERNAME_MAX_LENGTH = 64;

    /** 昵称最大长度 */
    public static final int NICKNAME_MAX_LENGTH = 128;

    /** 邮箱最大长度 */
    public static final int EMAIL_MAX_LENGTH = 128;

    /** 电话号码最大长度 */
    public static final int PHONE_MAX_LENGTH = 20;

    /** 直播间名称最大长度 */
    public static final int ROOM_NAME_MAX_LENGTH = 256;

    /** 直播间描述最大长度 */
    public static final int ROOM_DESCRIPTION_MAX_LENGTH = 1000;

    /** 消息最大长度 */
    public static final int MESSAGE_MAX_LENGTH = 500;

    /** 备注最大长度 */
    public static final int REMARK_MAX_LENGTH = 500;

    // ==================== traceId 相关 ====================

    /** traceId 前缀分隔符 */
    public static final String TRACE_ID_SEPARATOR = "-";

    /** HTTP traceId 请求头名称 */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** HTTP 服务名称请求头 */
    public static final String SERVICE_NAME_HEADER = "X-Service-Name";

    // ==================== 缓存键前缀 ====================

    /** 用户信息缓存前缀 */
    public static final String CACHE_USER_PREFIX = "user:";

    /** 主播信息缓存前缀 */
    public static final String CACHE_ANCHOR_PREFIX = "anchor:";

    /** 直播间信息缓存前缀 */
    public static final String CACHE_ROOM_PREFIX = "room:";

    /** 分成比例缓存前缀 */
    public static final String CACHE_COMMISSION_PREFIX = "commission:";

    /** 实时数据缓存前缀 */
    public static final String CACHE_REALTIME_PREFIX = "realtime:";

    // ==================== 锁键前缀 ====================

    /** 提现锁前缀 */
    public static final String LOCK_WITHDRAWAL_PREFIX = "lock:withdrawal:";

    /** 结算锁前缀 */
    public static final String LOCK_SETTLEMENT_PREFIX = "lock:settlement:";

    /** 用户信息锁前缀 */
    public static final String LOCK_USER_PREFIX = "lock:user:";

    // ==================== 性能相关常量 ====================

    /** 性能警告阈值 (毫秒) */
    public static final long PERFORMANCE_WARNING_THRESHOLD = 1000;

    /** 性能严重警告阈值 (毫秒) */
    public static final long PERFORMANCE_ERROR_THRESHOLD = 5000;

    // ==================== 业务常量 ====================

    /** 最大并发直播间数 */
    public static final int MAX_CONCURRENT_ROOMS = 100;

    /** 最大在线观众数 */
    public static final int MAX_ONLINE_AUDIENCE = 300000;

    /** 每秒最大请求数 */
    public static final int MAX_RPS = 1000;

    // ==================== 日期时间常量 ====================

    /** 默认日期时间格式 */
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** 日期格式 */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";

    /** 时间格式 */
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    // ==================== 编码相关 ====================

    /** 默认字符集 */
    public static final String DEFAULT_CHARSET = "UTF-8";

    /** JSON 内容类型 */
    public static final String CONTENT_TYPE_JSON = "application/json";

    // ==================== 工具方法 ====================

    /**
     * 验证金额
     */
    public static boolean validateAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(MIN_RECHARGE_AMOUNT) >= 0 &&
                amount.compareTo(MAX_RECHARGE_AMOUNT) <= 0;
    }

    /**
     * 验证提现金额
     */
    public static boolean validateWithdrawalAmount(java.math.BigDecimal amount) {
        if (amount == null) {
            return false;
        }
        return amount.compareTo(MIN_WITHDRAWAL_AMOUNT) >= 0 &&
                amount.compareTo(MAX_WITHDRAWAL_AMOUNT) <= 0;
    }
}
