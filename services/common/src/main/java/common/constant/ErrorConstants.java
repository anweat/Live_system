package common.constant;

/**
 * 错误码常量定义
 * 遵循统一的错误码规范: [系统码][模块码][业务码]
 * 
 * 系统码（1位）:
 * 1 = 通用错误
 * 2 = 验证错误
 * 3 = 业务错误
 * 4 = 系统错误
 * 
 * 模块码（2位）:
 * 01 = 用户模块
 * 02 = 直播间模块
 * 03 = 支付模块
 * 04 = 结算模块
 * 05 = 提现模块
 * 06 = 数据库模块
 * 
 * 业务码（2位）:
 * 01-99 = 具体错误码
 */
public class ErrorConstants {

    // ==================== 通用错误 (1xxxx) ====================

    public static final int SUCCESS = 0;

    /** 请求参数格式错误 */
    public static final int INVALID_REQUEST = 100001;

    /** 请求参数缺失 */
    public static final int MISSING_PARAMETER = 100002;

    /** 操作不被允许 */
    public static final int OPERATION_NOT_ALLOWED = 100003;

    /** 资源不存在 */
    public static final int RESOURCE_NOT_FOUND = 100004;

    /** 参数验证失败 */
    public static final int VALIDATION_FAILED = 100005;

    /** 系统错误 */
    public static final int SYSTEM_ERROR = 100006;

    /** 访问被拒绝 */
    public static final int SERVICE_ERROR = 100007;

    /** 业务错误 */
    public static final int BUSINESS_ERROR = 100008;


    // ==================== 参数验证错误 (2xxxx) ====================

    /** 用户ID格式错误 */
    public static final int INVALID_USER_ID = 210101;

    /** 邮箱格式错误 */
    public static final int INVALID_EMAIL = 210102;

    /** 手机号格式错误 */
    public static final int INVALID_PHONE = 210103;

    /** 金额格式错误或为负数 */
    public static final int INVALID_AMOUNT = 210301;

    /** 金额超过限额 */
    public static final int AMOUNT_EXCEEDS_LIMIT = 210302;

    /** 金额不足 */
    public static final int INSUFFICIENT_AMOUNT = 210303;

    // ==================== 用户相关错误 (3x01xx) ====================

    /** 用户不存在 */
    public static final int USER_NOT_FOUND = 300101;

    /** 用户已存在 */
    public static final int USER_ALREADY_EXISTS = 300102;

    /** 用户被禁用 */
    public static final int USER_DISABLED = 300103;

    /** 用户被禁言 */
    public static final int USER_MUTED = 300104;

    /** 用户被封禁 */
    public static final int USER_BANNED = 300105;

    /** 密码错误 */
    public static final int INVALID_PASSWORD = 300106;

    /** 主播不存在 */
    public static final int ANCHOR_NOT_FOUND = 300107;

    /** 观众不存在 */
    public static final int AUDIENCE_NOT_FOUND = 300108;

    // ==================== 直播间相关错误 (3x02xx) ====================

    /** 直播间不存在 */
    public static final int ROOM_NOT_FOUND = 300201;

    /** 直播间已存在 */
    public static final int ROOM_ALREADY_EXISTS = 300202;

    /** 直播间未开播 */
    public static final int ROOM_NOT_LIVE = 300203;

    /** 直播间已关闭 */
    public static final int ROOM_CLOSED = 300204;

    /** 直播间被封禁 */
    public static final int ROOM_BANNED = 300205;


    // ==================== 支付相关错误 (3x03xx) ====================

    /** 打赏记录不存在 */
    public static final int RECHARGE_NOT_FOUND = 300301;

    /** 重复的打赏请求 (traceId重复) */
    public static final int DUPLICATE_RECHARGE = 300302;

    /** 打赏金额无效 */
    public static final int INVALID_RECHARGE_AMOUNT = 300303;

    /** 打赏处理失败 */
    public static final int RECHARGE_PROCESSING_FAILED = 300304;

    /** 不支持的打赏类型 */
    public static final int UNSUPPORTED_RECHARGE_TYPE = 300305;

    // ==================== 结算相关错误 (3x04xx) ====================

    /** 结算记录不存在 */
    public static final int SETTLEMENT_NOT_FOUND = 300401;

    /** 结算金额无效 */
    public static final int INVALID_SETTLEMENT_AMOUNT = 300402;

    /** 结算失败 */
    public static final int SETTLEMENT_FAILED = 300403;

    /** 无效的结算时期 */
    public static final int INVALID_SETTLEMENT_PERIOD = 300404;

    /** 分成比例不存在 */
    public static final int COMMISSION_RATE_NOT_FOUND = 300405;

    // ==================== 提现相关错误 (3x05xx) ====================

    /** 提现申请不存在 */
    public static final int WITHDRAWAL_NOT_FOUND = 300501;

    /** 提现申请已存在 (状态不合法) */
    public static final int WITHDRAWAL_ALREADY_EXISTS = 300502;

    /** 可提取余额不足 */
    public static final int INSUFFICIENT_WITHDRAWAL_BALANCE = 300503;

    /** 提现金额超过限额 */
    public static final int WITHDRAWAL_AMOUNT_EXCEEDS_LIMIT = 300504;

    /** 不支持的提现方式 */
    public static final int UNSUPPORTED_WITHDRAWAL_TYPE = 300505;

    /** 提现处理失败 */
    public static final int WITHDRAWAL_PROCESSING_FAILED = 300506;

    // ==================== 数据库相关错误 (4x06xx) ====================

    /** 数据库连接失败 */
    public static final int DATABASE_CONNECTION_FAILED = 400601;

    /** 数据库操作失败 */
    public static final int DATABASE_OPERATION_FAILED = 400602;

    /** 表不存在 */
    public static final int TABLE_NOT_EXISTS = 400603;

    /** 数据重复 */
    public static final int DATA_DUPLICATE = 400604;

    /** 数据一致性错误 */
    public static final int DATA_CONSISTENCY_ERROR = 400605;

    // ==================== 系统错误 (4xxxx) ====================

    /** 系统内部错误 */
    public static final int INTERNAL_ERROR = 400001;

    /** 服务不可用 */
    public static final int SERVICE_UNAVAILABLE = 400002;

    /** 请求超时 */
    public static final int REQUEST_TIMEOUT = 400003;

    /** 远程服务调用失败 */
    public static final int REMOTE_SERVICE_CALL_FAILED = 400004;

    /** 配置错误 */
    public static final int CONFIGURATION_ERROR = 400005;

    // ==================== 权限相关错误 (3xxxx) ====================

    /** 无权访问 */
    public static final int ACCESS_DENIED = 300001;

    /** 权限不足 */
    public static final int INSUFFICIENT_PERMISSION = 300002;

    /** Token 无效 */
    public static final int INVALID_TOKEN = 300003;

    /** Token 已过期 */
    public static final int TOKEN_EXPIRED = 300004;

    /**
     * 根据错误码获取错误描述
     */
    public static String getErrorMessage(int errorCode) {
        switch (errorCode) {
            case SUCCESS:
                return "操作成功";
            case INVALID_REQUEST:
                return "请求参数格式错误";
            case MISSING_PARAMETER:
                return "请求参数缺失";
            case RESOURCE_NOT_FOUND:
                return "资源不存在";
            case USER_NOT_FOUND:
                return "用户不存在";
            case ROOM_NOT_FOUND:
                return "直播间不存在";
            case RECHARGE_NOT_FOUND:
                return "打赏记录不存在";
            case DUPLICATE_RECHARGE:
                return "重复的打赏请求";
            case INVALID_AMOUNT:
                return "金额格式错误或为负数";
            case INSUFFICIENT_AMOUNT:
                return "金额不足";
            case INSUFFICIENT_WITHDRAWAL_BALANCE:
                return "可提取余额不足";
            case DATABASE_CONNECTION_FAILED:
                return "数据库连接失败";
            case DATABASE_OPERATION_FAILED:
                return "数据库操作失败";
            case INTERNAL_ERROR:
                return "系统内部错误";
            case SERVICE_UNAVAILABLE:
                return "服务不可用";
            default:
                return "未知错误";
        }
    }
}
