package common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ID 生成工具类
 * 
 * 功能：
 * - 生成有序的分布式 ID
 * - 生成订单号
 * - 生成交易号
 * - 线程安全
 */
public class IdGeneratorUtil {

    // 雪花算法常量
    private static final long DATACENTER_BITS = 5L;
    private static final long MACHINE_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;

    private static final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_BITS);
    private static final long MAX_MACHINE_ID = -1L ^ (-1L << MACHINE_BITS);
    private static final long MAX_SEQUENCE = -1L ^ (-1L << SEQUENCE_BITS);

    private static final long MACHINE_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_SHIFT = SEQUENCE_BITS + MACHINE_BITS;
    private static final long TIMESTAMP_SHIFT = SEQUENCE_BITS + MACHINE_BITS + DATACENTER_BITS;

    private static final long EPOCH = 1609459200000L; // 2021-01-01 00:00:00 UTC

    private final long datacenterId;
    private final long machineId;
    private AtomicLong sequence = new AtomicLong(0);
    private AtomicLong lastTimestamp = new AtomicLong(-1);

    private static final IdGeneratorUtil INSTANCE = new IdGeneratorUtil(0, 0);

    /**
     * 构造方法
     * 
     * @param datacenterId 数据中心 ID（0-31）
     * @param machineId    机器 ID（0-31）
     */
    public IdGeneratorUtil(long datacenterId, long machineId) {
        if (datacenterId < 0 || datacenterId > MAX_DATACENTER_ID) {
            throw new IllegalArgumentException("datacenterId 必须在 0-31 之间");
        }
        if (machineId < 0 || machineId > MAX_MACHINE_ID) {
            throw new IllegalArgumentException("machineId 必须在 0-31 之间");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    /**
     * 生成下一个 ID（使用默认实例）
     * 
     * @return ID
     */
    public static long nextId() {
        return INSTANCE.nextId0();
    }

    /**
     * 生成下一个 ID
     * 
     * @return ID
     */
    private synchronized long nextId0() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨处理
        if (timestamp < lastTimestamp.get()) {
            // 等待时钟前进
            timestamp = lastTimestamp.get();
        }

        if (timestamp == lastTimestamp.get()) {
            // 同一毫秒内，序列号递增
            sequence.set((sequence.get() + 1) & MAX_SEQUENCE);
            if (sequence.get() == 0) {
                // 序列号溢出，等待下一毫秒
                timestamp = tilNextMillis(lastTimestamp.get());
            }
        } else {
            // 新的毫秒，重置序列号
            sequence.set(0);
        }

        lastTimestamp.set(timestamp);

        return ((timestamp - EPOCH) << TIMESTAMP_SHIFT)
                | (datacenterId << DATACENTER_SHIFT)
                | (machineId << MACHINE_SHIFT)
                | sequence.get();
    }

    /**
     * 等待到下一毫秒
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 生成订单号（格式：YYYYMMDDHHMMSS + 8位随机数）
     * 
     * @return 订单号
     */
    public static String generateOrderNo() {
        String dateTime = DateTimeUtil.format(DateTimeUtil.now(), "yyyyMMddHHmmss");
        String random = RandomUtil.generateRandomNumbers(8);
        return dateTime + random;
    }

    /**
     * 生成交易号（格式：TXN + 时间戳 + 随机数）
     * 
     * @return 交易号
     */
    public static String generateTransactionNo() {
        long timestamp = System.currentTimeMillis();
        String random = RandomUtil.generateRandomNumbers(6);
        return "TXN" + timestamp + random;
    }

    /**
     * 生成用户编号（格式：USR + 6位随机数）
     * 
     * @return 用户编号
     */
    public static String generateUserNo() {
        return "USR" + RandomUtil.generateRandomNumbers(6);
    }

    /**
     * 生成房间编号（格式：ROOM + 时间戳后6位 + 3位随机数）
     * 
     * @return 房间编号
     */
    public static String generateRoomNo() {
        String timestamp = String.valueOf(System.currentTimeMillis()).substring(7);
        String random = RandomUtil.generateRandomNumbers(3);
        return "ROOM" + timestamp + random;
    }

    /**
     * 生成结算单号（格式：SETTLE + 时间戳 + 4位随机数）
     * 
     * @return 结算单号
     */
    public static String generateSettlementNo() {
        long timestamp = System.currentTimeMillis();
        String random = RandomUtil.generateRandomNumbers(4);
        return "SETTLE" + timestamp + random;
    }

    /**
     * 生成充值单号（格式：RECHARGE + 时间戳 + 4位随机数）
     * 
     * @return 充值单号
     */
    public static String generateRechargeNo() {
        long timestamp = System.currentTimeMillis();
        String random = RandomUtil.generateRandomNumbers(4);
        return "RECHARGE" + timestamp + random;
    }

    /**
     * 生成提现单号（格式：WITHDRAW + 时间戳 + 4位随机数）
     * 
     * @return 提现单号
     */
    public static String generateWithdrawNo() {
        long timestamp = System.currentTimeMillis();
        String random = RandomUtil.generateRandomNumbers(4);
        return "WITHDRAW" + timestamp + random;
    }

    /**
     * 生成邀请码（格式：6位大小写字母和数字组合）
     * 
     * @return 邀请码
     */
    public static String generateInviteCode() {
        return RandomUtil.generateRandomAlphanumeric(6).toUpperCase();
    }

    /**
     * 生成验证码（格式：6位数字）
     * 
     * @return 验证码
     */
    public static String generateVerificationCode() {
        return RandomUtil.generateVerificationCode(6);
    }

    /**
     * 生成批次号（格式：BATCH + 时间戳 + 4位随机数）
     * 
     * @return 批次号
     */
    public static String generateBatchNo() {
        long timestamp = System.currentTimeMillis();
        String random = RandomUtil.generateRandomNumbers(4);
        return "BATCH" + timestamp + random;
    }
}
