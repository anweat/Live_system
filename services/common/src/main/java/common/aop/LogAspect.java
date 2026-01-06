package common.aop;

import common.annotation.Log;
import common.logger.AppLoggerExt;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;


/**
 * 日志记录切面
 * 
 * 功能：
 * - 拦截带 @Log 注解的方法
 * - 自动记录方法名、参数、返回值
 * - 记录执行时间和异常信息
 * 
 * 使用示例：
 * 
 * @Log(value = "更新用户信息")
 *            public void updateUser(User user) {
 *            ...
 *            }
 */
@Aspect
@Component
public class LogAspect {

    /**
     * 日志记录切面（环绕通知）
     * 
     * @param joinPoint     连接点
     * @param logAnnotation 日志注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(logAnnotation)")
    public Object logAround(ProceedingJoinPoint joinPoint, Log logAnnotation) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String methodName = method.getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();

        // 获取操作描述
        String description = logAnnotation.value();
        if (description == null || description.isEmpty()) {
            description = methodName;
        }

        // 获取参数
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        // 记录开始日志
        if (logAnnotation.logArgs()) {
            StringBuilder argsStr = new StringBuilder();
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i++) {
                    if (i > 0) {
                        argsStr.append(", ");
                    }
                    argsStr.append(paramNames[i]).append("=").append(formatArg(args[i]));
                }
            }
            AppLoggerExt.info(className, description, "开始执行，参数：{}", argsStr.toString());
        } else {
            AppLoggerExt.info(className, description, "开始执行");
        }

        long startTime = System.currentTimeMillis();
        Object result = null;
        boolean success = true;

        try {
            // 执行目标方法
            result = joinPoint.proceed();
            return result;
        } catch (Throwable e) {
            success = false;
            AppLoggerExt.error(className, description, "执行异常：{}", e.getMessage(), e);
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long costTime = endTime - startTime;

            if (success) {
                if (logAnnotation.logResult()) {
                    String resultStr = formatArg(result);
                    if (logAnnotation.logTime()) {
                        AppLoggerExt.info(className, description, "执行完成，耗时：{}ms，返回值：{}",
                                costTime, resultStr);
                    } else {
                        AppLoggerExt.info(className, description, "执行完成，返回值：{}", resultStr);
                    }
                } else {
                    if (logAnnotation.logTime()) {
                        AppLoggerExt.info(className, description, "执行完成，耗时：{}ms", costTime);
                    } else {
                        AppLoggerExt.info(className, description, "执行完成");
                    }
                }
            }
        }
    }

    /**
     * 格式化参数值（避免输出过长）
     * 
     * @param arg 参数值
     * @return 格式化后的字符串
     */
    private String formatArg(Object arg) {
        if (arg == null) {
            return "null";
        }

        String str = arg.toString();

        // 如果是数组，显示简化版本
        if (arg.getClass().isArray()) {
            Object[] array = (Object[]) arg;
            return "Array[" + array.length + "]";
        }

        // 如果字符串过长，截断
        if (str.length() > 200) {
            return str.substring(0, 200) + "...";
        }

        return str;
    }
}
