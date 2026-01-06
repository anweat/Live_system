package common.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * TraceId 生成器
 * 
 * 功能：
 * - 生成链路追踪 ID
 * - 支持多种格式
 * - 性能优化（原子递增）
 * - 线程安全
 * 
 * 使用示例：
 * String traceId = TraceIdGenerator.generate("user-service");
 * String traceId = TraceIdGenerator.generateWithHostname();
 * String traceId = TraceIdGenerator.generateShort();
 */
public class TraceIdGenerator {

    // 序列号生成器（原子递增，性能更高）
    private static final AtomicLong SEQUENCE = new AtomicLong(0);

    // 获取本机主机名
    private static final String HOSTNAME = getHostname();

    /**
     * 生成标准格式 TraceId
     * 格式：serviceName-timestamp-sequence
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generate(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        return String.format("%s-%d-%d",
                serviceName,
                System.currentTimeMillis(),
                getNextSequence());
    }

    /**
     * 生成扩展格式 TraceId（包含主机名）
     * 格式：serviceName-hostname-timestamp-sequence
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generateWithHostname(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        String host = HOSTNAME != null ? HOSTNAME : "local";
        return String.format("%s-%s-%d-%d",
                serviceName,
                host,
                System.currentTimeMillis(),
                getNextSequence());
    }

    /**
     * 生成超短格式 TraceId
     * 格式：timestamp-sequence (用于日志输出)
     * 
     * @return TraceId
     */
    public static String generateShort() {
        return String.format("%d-%d",
                System.currentTimeMillis(),
                getNextSequence());
    }

    /**
     * 生成完整格式 TraceId（包含所有信息）
     * 格式：serviceName-hostname-timestamp-sequence-randomId
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generateFull(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        String host = HOSTNAME != null ? HOSTNAME : "local";
        String randomId = generateRandomId();
        return String.format("%s-%s-%d-%d-%s",
                serviceName,
                host,
                System.currentTimeMillis(),
                getNextSequence(),
                randomId);
    }

    /**
     * 生成基于 UUID 的 TraceId
     * 格式：serviceName-uuid (更安全，但更长)
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generateWithUUID(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        String uuid = RandomUtil.generateUUID();
        return String.format("%s-%s",
                serviceName,
                uuid);
    }

    /**
     * 生成基于雪花算法的 TraceId
     * 格式：serviceName-snowflakeId (最优性能)
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generateWithSnowflake(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        long snowflakeId = IdGeneratorUtil.nextId();
        return String.format("%s-%d",
                serviceName,
                snowflakeId);
    }

    /**
     * 生成自定义格式的 TraceId
     * 
     * @param serviceName 服务名
     * @param customPart  自定义部分（如用户 ID、订单 ID 等）
     * @return TraceId
     */
    public static String generateCustom(String serviceName, String customPart) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        if (customPart == null || customPart.isEmpty()) {
            return generate(serviceName);
        }
        return String.format("%s-%s-%d-%d",
                serviceName,
                customPart,
                System.currentTimeMillis(),
                getNextSequence());
    }

    /**
     * 从现有 TraceId 中提取服务名
     * 
     * @param traceId TraceId 字符串
     * @return 服务名
     */
    public static String extractServiceName(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return "unknown";
        }
        String[] parts = traceId.split("-");
        if (parts.length > 0) {
            return parts[0];
        }
        return "unknown";
    }

    /**
     * 从现有 TraceId 中提取时间戳
     * 
     * @param traceId TraceId 字符串
     * @return 时间戳（毫秒），如果解析失败返回 0
     */
    public static long extractTimestamp(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return 0;
        }
        String[] parts = traceId.split("-");
        // 标准格式：serviceName-timestamp-sequence
        // 完整格式：serviceName-hostname-timestamp-sequence-randomId
        try {
            if (parts.length >= 3) {
                // 尝试解析第二个部分（可能是 hostname）
                long timestamp = Long.parseLong(parts[1]);
                if (timestamp > 1000000000000L) { // 有效的时间戳范围
                    return timestamp;
                }
                // 否则尝试第三个部分
                if (parts.length >= 4) {
                    return Long.parseLong(parts[2]);
                }
            }
        } catch (NumberFormatException e) {
            // 忽略异常，返回 0
        }
        return 0;
    }

    /**
     * 判断 TraceId 是否有效
     * 
     * @param traceId TraceId 字符串
     * @return 是否有效
     */
    public static boolean isValid(String traceId) {
        if (traceId == null || traceId.isEmpty()) {
            return false;
        }
        // 基本格式检查：至少要有 serviceName 和 timestamp
        String[] parts = traceId.split("-");
        return parts.length >= 2;
    }

    /**
     * 获取下一个序列号（原子操作，线程安全）
     */
    private static long getNextSequence() {
        long seq = SEQUENCE.incrementAndGet();
        // 每 1000000 重置一次，避免数字过大
        if (seq > 1000000) {
            SEQUENCE.set(0);
        }
        return seq;
    }

    /**
     * 生成随机 ID（6位十六进制）
     */
    private static String generateRandomId() {
        int randomInt = RandomUtil.nextInt(0xFFFFFF); // 6位十六进制
        return String.format("%06x", randomInt);
    }

    /**
     * 获取本机主机名
     */
    private static String getHostname() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (java.net.UnknownHostException e) {
            return null;
        }
    }

    /**
     * 生成可读的 TraceId（包含时间戳信息）
     * 
     * @param serviceName 服务名
     * @return TraceId
     */
    public static String generateReadable(String serviceName) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        String datetime = DateTimeUtil.format(DateTimeUtil.now(), "yyyyMMddHHmmss");
        return String.format("%s-%s-%d",
                serviceName,
                datetime,
                getNextSequence());
    }

    /**
     * 批量生成 TraceId（用于测试或批量操作）
     * 
     * @param serviceName 服务名
     * @param count       数量
     * @return TraceId 列表
     */
    public static java.util.List<String> generateBatch(String serviceName, int count) {
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "unknown";
        }
        java.util.List<String> traceIds = new java.util.ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            traceIds.add(generate(serviceName));
        }
        return traceIds;
    }
}
