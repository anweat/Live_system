package common.logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import lombok.extern.slf4j.Slf4j;
import common.util.TraceIdGenerator;

/**
 * 链路追踪日志工具类
 * 支持在分布式系统中使用traceId追踪跨服务的请求
 * 
 * 使用示例：
 * TraceLogger.info("user_service", "用户登录", userId);
 * TraceLogger.warn("payment_service", "支付异常", paymentId);
 * TraceLogger.error("settlement_service", "结算失败", settlementId);
 */
@Slf4j
public class TraceLogger {

    private static final String TRACE_ID_KEY = "traceId";
    private static final String SERVICE_NAME_KEY = "serviceName";
    private static final String MODULE_KEY = "module";
    private static final String OPERATION_KEY = "operation";

    /** 日志上下文本地存储 */
    private static final ThreadLocal<Map<String, String>> logContext = ThreadLocal.withInitial(ConcurrentHashMap::new);

    /**
     * 初始化日志上下文
     * 
     * @param traceId     链路追踪ID，用于关联同一请求在各服务中的日志
     * @param serviceName 服务名称，如：user-service, payment-service
     */
    public static void initContext(String traceId, String serviceName) {
        Map<String, String> context = logContext.get();
        context.put(TRACE_ID_KEY, traceId);
        context.put(SERVICE_NAME_KEY, serviceName);

        // 设置MDC用于SLF4J自动注入
        MDC.put(TRACE_ID_KEY, traceId);
        MDC.put(SERVICE_NAME_KEY, serviceName);
    }

    /**
     * 设置日志模块
     * 用于标识当前执行的模块
     * 
     * @param module 模块名，如：audience, anchor, settlement
     */
    public static void setModule(String module) {
        Map<String, String> context = logContext.get();
        context.put(MODULE_KEY, module);
        MDC.put(MODULE_KEY, module);
    }

    /**
     * 设置操作类型
     * 用于标识当前执行的操作
     * 
     * @param operation 操作类型，如：CREATE, UPDATE, DELETE, QUERY
     */
    public static void setOperation(String operation) {
        Map<String, String> context = logContext.get();
        context.put(OPERATION_KEY, operation);
        MDC.put(OPERATION_KEY, operation);
    }

    /**
     * 获取当前traceId
     * 
     * @return traceId
     */
    public static String getTraceId() {
        String traceId = MDC.get(TRACE_ID_KEY);
        if (traceId == null) {
            traceId = logContext.get().get(TRACE_ID_KEY);
        }
        return traceId;
    }

    /**
     * 获取当前serviceName
     * 
     * @return serviceName
     */
    public static String getServiceName() {
        String serviceName = MDC.get(SERVICE_NAME_KEY);
        if (serviceName == null) {
            serviceName = logContext.get().get(SERVICE_NAME_KEY);
        }
        return serviceName;
    }

    /**
     * 清除日志上下文
     * 应在请求结束时调用以避免内存泄漏
     */
    public static void clearContext() {
        logContext.remove();
        MDC.clear();
    }

    /**
     * 记录HTTP请求信息
     * 
     * @param method  HTTP方法 GET/POST/PUT/DELETE
     * @param url     请求URL
     * @param headers 请求头
     */
    public static void logHttpRequest(String method, String url, Map<String, String> headers) {
        StringBuilder sb = new StringBuilder();
        sb.append("HTTP Request - ").append(method).append(" ").append(url);

        if (headers != null && !headers.isEmpty()) {
            sb.append(" [Headers: ");
            headers.forEach((k, v) -> {
                // 隐藏敏感信息
                if ("Authorization".equalsIgnoreCase(k) || "Cookie".equalsIgnoreCase(k)) {
                    sb.append(k).append("=***,");
                } else {
                    sb.append(k).append("=").append(v).append(",");
                }
            });
            sb.setLength(sb.length() - 1);
            sb.append("]");
        }

        log.info(sb.toString());
    }

    /**
     * 记录HTTP请求体和参数
     * 
     * @param params 请求参数
     */
    public static void logRequestParams(Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            return;
        }

        StringBuilder sb = new StringBuilder("Request Parameters: ");
        params.forEach((k, v) -> {
            // 隐藏敏感信息
            if ("password".equalsIgnoreCase(k) || "token".equalsIgnoreCase(k) ||
                    "secret".equalsIgnoreCase(k)) {
                sb.append(k).append("=***,");
            } else {
                sb.append(k).append("=").append(v).append(",");
            }
        });
        sb.setLength(sb.length() - 1);

        log.info(sb.toString());
    }

    /**
     * 记录HTTP响应信息
     * 
     * @param statusCode   HTTP状态码
     * @param responseTime 响应时间(毫秒)
     */
    public static void logHttpResponse(int statusCode, long responseTime) {
        String status = statusCode >= 200 && statusCode < 300 ? "SUCCESS"
                : statusCode >= 400 && statusCode < 500 ? "CLIENT_ERROR"
                        : statusCode >= 500 ? "SERVER_ERROR" : "UNKNOWN";
        log.info("HTTP Response - Status: {} ({}), Time: {}ms", statusCode, status, responseTime);
    }

    /**
     * 记录业务操作 - 信息级别
     * 
     * @param module    模块名
     * @param operation 操作描述
     * @param bizId     业务ID (可选)
     * @param extInfo   扩展信息 key-value对 (可选)
     */
    public static void info(String module, String operation, Object bizId, Object... extInfo) {
        setModule(module);
        setOperation(operation);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(module).append("] ").append(operation);

        if (bizId != null) {
            sb.append(" - BizId: ").append(bizId);
        }

        if (extInfo != null && extInfo.length > 0) {
            sb.append(" [");
            for (int i = 0; i < extInfo.length; i += 2) {
                if (i + 1 < extInfo.length) {
                    sb.append(extInfo[i]).append("=").append(extInfo[i + 1]);
                    if (i + 2 < extInfo.length) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        }

        log.info(sb.toString());
    }

    /**
     * 记录业务操作 - 信息级别 (无业务ID)
     * 
     * @param module    模块名
     * @param operation 操作描述
     */
    public static void info(String module, String operation) {
        info(module, operation, null);
    }

    /**
     * 记录业务操作 - 警告级别
     * 
     * @param module    模块名
     * @param operation 操作描述
     * @param bizId     业务ID (可选)
     * @param extInfo   扩展信息 (可选)
     */
    public static void warn(String module, String operation, Object bizId, Object... extInfo) {
        setModule(module);
        setOperation(operation);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(module).append("] ").append(operation);

        if (bizId != null) {
            sb.append(" - BizId: ").append(bizId);
        }

        if (extInfo != null && extInfo.length > 0) {
            sb.append(" [");
            for (int i = 0; i < extInfo.length; i += 2) {
                if (i + 1 < extInfo.length) {
                    sb.append(extInfo[i]).append("=").append(extInfo[i + 1]);
                    if (i + 2 < extInfo.length) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        }

        log.warn(sb.toString());
    }

    /**
     * 记录业务操作 - 警告级别 (无业务ID)
     * 
     * @param module    模块名
     * @param operation 操作描述
     */
    public static void warn(String module, String operation) {
        warn(module, operation, null);
    }

    /**
     * 记录业务操作 - 错误级别
     * 
     * @param module    模块名
     * @param operation 操作描述
     * @param bizId     业务ID (可选)
     * @param throwable 异常对象
     * @param extInfo   扩展信息 (可选)
     */
    public static void error(String module, String operation, Object bizId, Throwable throwable, Object... extInfo) {
        setModule(module);
        setOperation(operation);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(module).append("] ").append(operation);

        if (bizId != null) {
            sb.append(" - BizId: ").append(bizId);
        }

        if (extInfo != null && extInfo.length > 0) {
            sb.append(" [");
            for (int i = 0; i < extInfo.length; i += 2) {
                if (i + 1 < extInfo.length) {
                    sb.append(extInfo[i]).append("=").append(extInfo[i + 1]);
                    if (i + 2 < extInfo.length) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        }

        if (throwable != null) {
            log.error(sb.toString(), throwable);
        } else {
            log.error(sb.toString());
        }
    }

    /**
     * 记录业务操作 - 错误级别 (无业务ID)
     * 
     * @param module    模块名
     * @param operation 操作描述
     * @param throwable 异常对象
     */
    public static void error(String module, String operation, Throwable throwable) {
        error(module, operation, null, throwable);
    }

    /**
     * 记录业务操作 - 调试级别
     * 
     * @param module    模块名
     * @param operation 操作描述
     * @param bizId     业务ID (可选)
     * @param extInfo   扩展信息 (可选)
     */
    public static void debug(String module, String operation, Object bizId, Object... extInfo) {
        if (!log.isDebugEnabled()) {
            return;
        }

        setModule(module);
        setOperation(operation);
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(module).append("] ").append(operation);

        if (bizId != null) {
            sb.append(" - BizId: ").append(bizId);
        }

        if (extInfo != null && extInfo.length > 0) {
            sb.append(" [");
            for (int i = 0; i < extInfo.length; i += 2) {
                if (i + 1 < extInfo.length) {
                    sb.append(extInfo[i]).append("=").append(extInfo[i + 1]);
                    if (i + 2 < extInfo.length) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        }

        log.debug(sb.toString());
    }

    /**
     * 记录服务启动事件
     * 
     * @param serviceName 服务名
     * @param version     版本号
     * @param port        服务端口
     */
    public static void logServiceStartup(String serviceName, String version, int port) {
        log.info("========================================");
        log.info("Service Startup: {} v{}", serviceName, version);
        log.info("Port: {}", port);
        log.info("Timestamp: {}", new Date());
        log.info("========================================");
    }

    /**
     * 记录服务关闭事件
     * 
     * @param serviceName 服务名
     */
    public static void logServiceShutdown(String serviceName) {
        log.info("========================================");
        log.info("Service Shutdown: {}", serviceName);
        log.info("Timestamp: {}", new Date());
        log.info("========================================");
    }

    /**
     * 记录性能信息
     * 
     * @param module     模块名
     * @param operation  操作描述
     * @param durationMs 耗时(毫秒)
     * @param extInfo    扩展信息 (可选)
     */
    public static void logPerformance(String module, String operation, long durationMs, Object... extInfo) {
        setModule(module);
        setOperation(operation);

        String level = "INFO";
        if (durationMs > 5000) {
            level = "WARN";
        } else if (durationMs > 10000) {
            level = "ERROR";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[").append(module).append("] ").append(operation)
                .append(" - Duration: ").append(durationMs).append("ms [").append(level).append("]");

        if (extInfo != null && extInfo.length > 0) {
            sb.append(" [");
            for (int i = 0; i < extInfo.length; i += 2) {
                if (i + 1 < extInfo.length) {
                    sb.append(extInfo[i]).append("=").append(extInfo[i + 1]);
                    if (i + 2 < extInfo.length) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");
        }

        if ("ERROR".equals(level)) {
            log.error(sb.toString());
        } else if ("WARN".equals(level)) {
            log.warn(sb.toString());
        } else {
            log.info(sb.toString());
        }
    }

    /**
     * 生成新的traceId
     * 调用 TraceIdGenerator 生成标准格式的 traceId
     * 格式: serviceName-timestamp-sequence
     * 
     * @param serviceName 服务名
     * @return 生成的traceId
     */
    public static String generateTraceId(String serviceName) {
        return TraceIdGenerator.generate(serviceName);
    }

    /**
     * 获取日志上下文信息
     * 用于调试和监控
     * 
     * @return 当前的日志上下文Map
     */
    public static Map<String, String> getContextInfo() {
        return new HashMap<>(logContext.get());
    }
}
