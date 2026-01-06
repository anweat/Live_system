package common.aop;

import common.annotation.Idempotent;
import common.exception.BusinessException;
import common.logger.AppLoggerExt;
import common.logger.TraceLogger;
import common.config.RedisProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 幂等性切面
 * 
 * 功能：
 * - 拦截带 @Idempotent 注解的方法
 * - 防止同一请求短时间内重复调用
 * - 使用 key 生成唯一标识
 * - 支持两种模式：
 *   1. Redis 模式（生产环境）: 通过 redis-service 调用
 *   2. 本地模式（开发环境）: 使用内存 ConcurrentHashMap
 * 
 * 使用示例：
 * 
 * @Idempotent(key = "#userId", timeout = 5, unit = TimeUnit.SECONDS)
 *                 public void submitOrder(Long userId, Order order) {
 *                 ...
 *                 }
 */
@Aspect
@Component
public class IdempotentAspect {

    // SpEL 表达式解析器
    private static final SpelExpressionParser EXPRESSION_PARSER = new SpelExpressionParser();

    // 本地缓存（当 Redis 禁用时使用）
    private static final Map<String, Long> LOCAL_IDEMPOTENT_CACHE = new ConcurrentHashMap<>();

    @Autowired(required = false)
    private RestTemplate restTemplate;

    @Autowired(required = false)
    private RedisProperties redisProperties;

    /**
     * 幂等性检查切面（环绕通知）
     * 
     * @param joinPoint            连接点
     * @param idempotentAnnotation 幂等性注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(idempotentAnnotation)")
    public Object idempotentAround(ProceedingJoinPoint joinPoint, Idempotent idempotentAnnotation)
            throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // 生成幂等性 key
        String idempotentKey = generateIdempotentKey(
                idempotentAnnotation.key(),
                method,
                args,
                className,
                methodName);

        // 检查是否启用了 Redis
        boolean redisEnabled = redisProperties != null && redisProperties.isEnabled();
        
        if (redisEnabled) {
            // 使用 redis-service 检查幂等性
            long ttl = idempotentAnnotation.unit().toSeconds(idempotentAnnotation.timeout());
            boolean isFirstRequest = checkIdempotencyWithRedis(idempotentKey, ttl);

            if (!isFirstRequest) {
                String message = idempotentAnnotation.message();
                TraceLogger.warn("检测到重复请求（Redis 模式）：{} | message: {}", idempotentKey, message);
                throw new BusinessException(message);
            }
        } else {
            // 降级到本地内存缓存
            long ttl = idempotentAnnotation.unit().toSeconds(idempotentAnnotation.timeout());
            boolean isFirstRequest = checkIdempotencyLocal(idempotentKey, ttl);

            if (!isFirstRequest) {
                String message = idempotentAnnotation.message();
                TraceLogger.warn("检测到重复请求（本地内存模式）：{} | message: {}", idempotentKey, message);
                throw new BusinessException(message);
            }
        }

        try {
            // 执行目标方法
            return joinPoint.proceed();
        } catch (Exception e) {
            // 执行失败，删除幂等性记录（允许重试）
            if (redisEnabled) {
                deleteIdempotencyKeyFromRedis(idempotentKey);
            } else {
                deleteIdempotencyKeyLocal(idempotentKey);
            }
            throw e;
        }
    }

    /**
     * 通过 redis-service 检查幂等性
     */
    private boolean checkIdempotencyWithRedis(String idempotentKey, long ttl) {
        try {
            if (restTemplate == null) {
                // 降级处理：如果没有 RestTemplate，返回 true 允许执行
                TraceLogger.warn("RestTemplate not available, fallback to allow execution: {}", "");
                return true;
            }

            String redisServiceUrl = "http://localhost:8085/redis/api/v1/lock/check-idempotency" +
                    "?idempotentKey=" + idempotentKey + "&ttl=" + ttl;

            try {
                @SuppressWarnings("unchecked")
                java.util.Map<String, Object> response = restTemplate.getForObject(redisServiceUrl, java.util.Map.class);
                if (response != null) {
                    Boolean success = (Boolean) response.get("success");
                    return success != null && success;
                }
                return true; // 失败时允许执行，避免雪崩
            } catch (Exception e) {
                TraceLogger.error("Failed to check idempotency with redis-service: {}", e.getMessage(), e);
                return true; // 发生异常时允许执行，避免雪崩
            }
        } catch (Exception e) {
            TraceLogger.error("Error in checkIdempotencyWithRedis: {}", e.getMessage(), e);
            return true;
        }
    }

    /**
     * 使用本地内存检查幂等性（Redis 禁用时）
     */
    private boolean checkIdempotencyLocal(String idempotentKey, long ttl) {
        try {
            Long expiryTime = LOCAL_IDEMPOTENT_CACHE.get(idempotentKey);
            
            if (expiryTime == null) {
                // 首次请求，记录到本地缓存
                long expiry = System.currentTimeMillis() + (ttl * 1000);
                LOCAL_IDEMPOTENT_CACHE.put(idempotentKey, expiry);
                return true;
            }
            
            // 检查是否已过期
            if (System.currentTimeMillis() > expiryTime) {
                LOCAL_IDEMPOTENT_CACHE.remove(idempotentKey);
                // 过期了，重新记录
                long expiry = System.currentTimeMillis() + (ttl * 1000);
                LOCAL_IDEMPOTENT_CACHE.put(idempotentKey, expiry);
                return true;
            }
            
            // 重复请求
            return false;
        } catch (Exception e) {
            TraceLogger.error("Error in checkIdempotencyLocal: {}", e.getMessage(), e);
            return true; // 异常时允许执行
        }
    }

    /**
     * 从 Redis 删除幂等性 key
     */
    private void deleteIdempotencyKeyFromRedis(String idempotentKey) {
        try {
            if (restTemplate == null) {
                return;
            }

            String redisServiceUrl = "http://localhost:8085/redis/api/v1/cache/delete" +
                    "?key=" + idempotentKey;

            try {
                restTemplate.delete(redisServiceUrl);
            } catch (Exception e) {
                TraceLogger.warn("Failed to delete idempotency key from redis-service: {}", e.getMessage());
            }
        } catch (Exception e) {
            TraceLogger.warn("Error in deleteIdempotencyKeyFromRedis: {}", e.getMessage());
        }
    }

    /**
     * 从本地删除幂等性 key
     */
    private void deleteIdempotencyKeyLocal(String idempotentKey) {
        try {
            LOCAL_IDEMPOTENT_CACHE.remove(idempotentKey);
        } catch (Exception e) {
            TraceLogger.warn("Error in deleteIdempotencyKeyLocal: {}", e.getMessage());
        }
    }

    /**
     * 生成幂等性 key
     */
    private String generateIdempotentKey(String keyExpression, Method method, Object[] args,
            String className, String methodName) {
        String key;

        if (keyExpression == null || keyExpression.isEmpty()) {
            // 如果没有指定 key，使用方法名 + 所有参数的哈希值
            StringBuilder sb = new StringBuilder();
            sb.append(className).append(".").append(methodName);
            for (Object arg : args) {
                if (arg != null) {
                    sb.append("_").append(arg.hashCode());
                }
            }
            key = sb.toString();
        } else if (keyExpression.startsWith("#")) {
            // 支持 SpEL 表达式（如 #userId）
            key = parseSpelExpression(keyExpression, method, args);
        } else {
            // 简单的参数名匹配
            key = parseSimpleParameter(keyExpression, method, args);
        }

        return key;
    }

    /**
     * 解析 SpEL 表达式
     */
    private String parseSpelExpression(String expression, Method method, Object[] args) {
        try {
            EvaluationContext context = new StandardEvaluationContext();
            String[] paramNames = getParameterNames(method);

            // 将参数添加到 SpEL 上下文
            for (int i = 0; i < paramNames.length && i < args.length; i++) {
                context.setVariable(paramNames[i], args[i]);
            }

            Object result = EXPRESSION_PARSER.parseExpression(expression).getValue(context);
            return result != null ? result.toString() : "unknown";
        } catch (Exception e) {
            return "error_" + System.nanoTime();
        }
    }

    /**
     * 解析简单参数名
     */
    private String parseSimpleParameter(String paramName, Method method, Object[] args) {
        try {
            String[] paramNames = getParameterNames(method);
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName) && i < args.length) {
                    return args[i] != null ? args[i].toString() : "null";
                }
            }
        } catch (Exception e) {
            // 忽略异常
        }
        return paramName;
    }

    /**
     * 获取方法参数名
     */
    private String[] getParameterNames(Method method) {
        // 这里简化处理，实际项目中需要使用 Spring 的 ParameterNameDiscoverer
        Class<?>[] paramTypes = method.getParameterTypes();
        String[] names = new String[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            names[i] = "arg" + i;
        }
        return names;
    }
}
