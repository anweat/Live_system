package common.logger;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 应用日志工具类
 * 用于记录应用级别的日志（启动、关闭、健康检查等）
 * 不需要traceId，主要用于诊断和监控应用本身的状态
 * 
 * 与TraceLogger区别：
 * - TraceLogger：用于请求链路追踪，需要traceId
 * - AppLogger：用于应用级日志，不需要traceId
 */
@Slf4j
public class AppLogger {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    private static final String SEPARATOR = " | ";

    /**
     * 记录信息级别日志
     */
    public static void info(String message, Object... args) {
        String formattedMsg = formatMessage(message, args);
        log.info(formattedMsg);
    }

    /**
     * 记录警告级别日志
     */
    public static void warn(String message, Object... args) {
        String formattedMsg = formatMessage(message, args);
        log.warn(formattedMsg);
    }

    /**
     * 记录错误级别日志
     */
    public static void error(String message, Object... args) {
        String formattedMsg = formatMessage(message, args);
        log.error(formattedMsg);
    }

    /**
     * 记录错误级别日志（带异常）
     */
    public static void error(String message, Throwable ex, Object... args) {
        String formattedMsg = formatMessage(message, args);
        log.error(formattedMsg, ex);
    }

    /**
     * 记录调试级别日志
     */
    public static void debug(String message, Object... args) {
        String formattedMsg = formatMessage(message, args);
        log.debug(formattedMsg);
    }

    /**
     * 记录应用启动日志
     */
    public static void logStartup(String applicationName, String version, int port) {
        info("================================================");
        info("应用启动成功");
        info("应用名称: {}", applicationName);
        info("应用版本: {}", version);
        info("启动端口: {}", port);
        info("启动时间: {}", LocalDateTime.now().format(DATE_FORMATTER));
        info("JVM版本: {}", System.getProperty("java.version"));
        info("操作系统: {} {}", System.getProperty("os.name"), System.getProperty("os.version"));
        info("================================================");
    }

    /**
     * 记录应用关闭日志
     */
    public static void logShutdown(String applicationName) {
        info("================================================");
        info("应用关闭开始");
        info("应用名称: {}", applicationName);
        info("关闭时间: {}", LocalDateTime.now().format(DATE_FORMATTER));
        info("================================================");
    }

    /**
     * 记录应用就绪日志
     */
    public static void logReady(String applicationName) {
        info("应用已就绪，可以接收请求 - {}", applicationName);
    }

    /**
     * 记录服务初始化
     */
    public static void logServiceInitialize(String serviceName) {
        info("初始化服务: {}", serviceName);
    }

    /**
     * 记录服务初始化完成
     */
    public static void logServiceInitializeComplete(String serviceName) {
        info("服务初始化完成: {}", serviceName);
    }

    /**
     * 记录健康检查
     */
    public static void logHealthCheck(String checkName, boolean healthy, String... details) {
        if (healthy) {
            info("健康检查通过 - {}: {}", checkName, String.join(", ", details));
        } else {
            warn("健康检查失败 - {}: {}", checkName, String.join(", ", details));
        }
    }

    /**
     * 记录数据库连接状态
     */
    public static void logDatabaseConnection(String dataSourceName, boolean connected, String details) {
        if (connected) {
            info("数据库连接成功 - 数据源: {} | {}", dataSourceName, details);
        } else {
            error("数据库连接失败 - 数据源: {} | {}", dataSourceName, details);
        }
    }

    /**
     * 记录表检查结果
     */
    public static void logTableCheck(String tableName, boolean exists, String details) {
        if (exists) {
            info("数据库表存在 - 表名: {} | {}", tableName, details);
        } else {
            warn("数据库表不存在 - 表名: {} | {}", tableName, details);
        }
    }

    /**
     * 记录数据库初始化
     */
    public static void logDatabaseInitialize(String dataSourceName) {
        info("开始初始化数据库: {}", dataSourceName);
    }

    /**
     * 记录数据库初始化完成
     */
    public static void logDatabaseInitializeComplete(String dataSourceName, int tableCount) {
        info("数据库初始化完成 - 数据源: {} | 表数: {}", dataSourceName, tableCount);
    }

    /**
     * 记录配置信息
     */
    public static void logConfiguration(String key, String value) {
        debug("配置: {} = {}", key, hideSensitiveValue(key, value));
    }

    /**
     * 记录缓存操作
     */
    public static void logCacheOperation(String operation, String cacheName, String details) {
        debug("缓存操作 - 操作: {} | 缓存: {} | {}", operation, cacheName, details);
    }

    /**
     * 记录定时任务执行
     */
    public static void logScheduledTask(String taskName, boolean success, long durationMs) {
        if (success) {
            info("定时任务执行成功 - 任务: {} | 耗时: {}ms", taskName, durationMs);
        } else {
            warn("定时任务执行失败 - 任务: {} | 耗时: {}ms", taskName, durationMs);
        }
    }

    /**
     * 记录资源加载
     */
    public static void logResourceLoaded(String resourceName, String location) {
        info("资源加载 - 名称: {} | 位置: {}", resourceName, location);
    }

    /**
     * 记录插件加载
     */
    public static void logPluginLoaded(String pluginName, String version) {
        info("插件加载成功 - 名称: {} | 版本: {}", pluginName, version);
    }

    /**
     * 记录依赖加载
     */
    public static void logDependencyLoaded(String dependencyName, String version) {
        debug("依赖加载 - 名称: {} | 版本: {}", dependencyName, version);
    }

    /**
     * 记录环境信息
     */
    public static void logEnvironmentInfo(String envName, String value) {
        info("环境信息 - {}: {}", envName, value);
    }

    /**
     * 记录性能指标
     */
    public static void logMetrics(String metricsName, Map<String, Object> values) {
        StringBuilder sb = new StringBuilder();
        values.forEach((k, v) -> sb.append(k).append("=").append(v).append(", "));
        info("性能指标 - {}: {}", metricsName, sb.toString());
    }

    /**
     * 记录关键事件
     */
    public static void logEvent(String eventType, String eventName, Map<String, String> properties) {
        StringBuilder sb = new StringBuilder();
        properties.forEach((k, v) -> sb.append(k).append("=").append(hideSensitiveValue(k, v)).append(", "));
        info("事件 - 类型: {} | 名称: {} | 属性: {}", eventType, eventName, sb.toString());
    }

    /**
     * 记录简单事件
     */
    public static void logEvent(String eventType, String eventName) {
        info("事件 - 类型: {} | 名称: {}", eventType, eventName);
    }

    /**
     * 格式化消息 - 支持占位符 {}
     */
    private static String formatMessage(String message, Object[] args) {
        if (args == null || args.length == 0) {
            return message;
        }

        String result = message;
        for (Object arg : args) {
            result = result.replaceFirst("\\{\\}", String.valueOf(arg));
        }
        return result;
    }

    /**
     * 隐藏敏感字段的值
     */
    private static String hideSensitiveValue(String key, String value) {
        if (value == null) {
            return null;
        }

        String lowerKey = key.toLowerCase();

        // 敏感关键词列表
        if (lowerKey.contains("password") ||
                lowerKey.contains("token") ||
                lowerKey.contains("secret") ||
                lowerKey.contains("auth") ||
                lowerKey.contains("credential") ||
                lowerKey.contains("key")) {

            // 只显示前2个字符
            if (value.length() > 2) {
                return value.substring(0, 2) + "***";
            } else {
                return "***";
            }
        }

        return value;
    }
}
