package common.constant;

/**
 * 状态常量类扩展 - 财务服务相关状态
 */
public class FinanceStatusConstants {

    /**
     * 提现状态
     */
    public static class WithdrawalStatus {
        /** 申请中 */
        public static final int APPLYING = 0;
        /** 处理中 */
        public static final int PROCESSING = 1;
        /** 已打款 */
        public static final int COMPLETED = 2;
        /** 失败 */
        public static final int FAILED = 3;
        /** 已拒绝 */
        public static final int REJECTED = 4;
    }

    /**
     * 结算状态
     */
    public static class SettlementStatus {
        /** 正常 */
        public static final int NORMAL = 0;
        /** 冻结 */
        public static final int FROZEN = 1;
        /** 禁提 */
        public static final int FORBIDDEN = 2;
        
        /** 已结算 */
        public static final int SETTLED = 0;
        /** 已对账 */
        public static final int CHECKED = 1;
        /** 已审核 */
        public static final int AUDITED = 2;
    }

    /**
     * 同步状态
     */
    public static class SyncStatus {
        /** 待同步 */
        public static final int PENDING = 0;
        /** 同步中 */
        public static final int SYNCING = 1;
        /** 已同步 */
        public static final int SYNCED = 2;
        /** 失败 */
        public static final int FAILED = 3;
    }
}
