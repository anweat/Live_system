package common.logger;

import lombok.extern.slf4j.Slf4j;

/**
 * AppLogger 扩展方法
 * 添加业务相关的日志方法
 * 与AppLogger搭配使用
 */
@Slf4j
public class AppLoggerExt {

    /**
     * 记录信息日志（通用方法）
     */
    public static void info(String className, String methodOrDesc, String message, Object... args) {
        String logMsg = "[" + className + "] " + methodOrDesc + " - " + message;
        if (args != null && args.length > 0) {
            log.info(logMsg, args);
        } else {
            log.info(logMsg);
        }
    }

    /**
     * 记录警告日志（通用方法）
     */
    public static void warn(String className, String methodOrDesc, String message, Object... args) {
        String logMsg = "[" + className + "] " + methodOrDesc + " - " + message;
        if (args != null && args.length > 0) {
            log.warn(logMsg, args);
        } else {
            log.warn(logMsg);
        }
    }

    /**
     * 记录错误日志（通用方法）
     */
    public static void error(String className, String methodOrDesc, String message, Object... args) {
        String logMsg = "[" + className + "] " + methodOrDesc + " - " + message;
        if (args != null && args.length > 0) {
            log.error(logMsg, args);
        } else {
            log.error(logMsg);
        }
    }

    /**
     * 记录错误日志（带异常）
     */
    public static void error(String className, String methodOrDesc, String message, Object arg1, Throwable cause) {
        String logMsg = "[" + className + "] " + methodOrDesc + " - " + message;
        log.error(logMsg, arg1, cause);
    }

    /**
     * 记录验证错误
     */
    public static void logValidationError(String message) {
        log.warn("VALIDATION_ERROR | message: {}", message);
    }

    /**
     * 记录系统错误
     */
    public static void logSystemError(int errorCode, String message, Throwable cause) {
        if (cause != null) {
            log.error("SYSTEM_ERROR | code: {} | message: {}", errorCode, message, cause);
        } else {
            log.error("SYSTEM_ERROR | code: {} | message: {}", errorCode, message);
        }
    }

    /**
     * 记录API调用
     */
    public static void logApiCall(String method, String path, long durationMs) {
        log.info("API_CALL | method: {} | path: {} | duration: {}ms", method, path, durationMs);
    }

    /**
     * 记录数据库操作
     */
    public static void logDbOperation(String operation, String table, long durationMs) {
        log.info("DB_OPERATION | operation: {} | table: {} | duration: {}ms", operation, table, durationMs);
    }

    /**
     * 记录缓存操作
     */
    public static void logCacheOperation(String operation, String key, long durationMs) {
        log.info("CACHE_OPERATION | operation: {} | key: {} | duration: {}ms", operation, key, durationMs);
    }

    /**
     * 记录业务事件
     */
    public static void logBusinessEvent(String event, String details) {
        log.info("BUSINESS_EVENT | event: {} | details: {}", event, details);
    }

    /**
     * 记录性能警告
     */
    public static void logPerformanceWarning(String operation, long durationMs, long thresholdMs) {
        log.warn("PERFORMANCE_WARNING | operation: {} | duration: {}ms | threshold: {}ms",
                operation, durationMs, thresholdMs);
    }

    /**
     * 记录业务错误
     */
    public static void logBusinessError(int errorCode, String message) {
        log.error("BUSINESS_ERROR | errorCode: {} | message: {}", errorCode, message);
    }
}
