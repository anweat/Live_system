package common.annotation;

import java.lang.annotation.*;

/**
 * 日志记录注解
 * 
 * 用于标记需要记录日志的方法
 * LogAspect 会自动拦截并记录：
 * - 方法名、参数、返回值
 * - 执行时间
 * - 异常信息
 * 
 * 使用示例：
 * 
 * @Log
 *      public void updateUser(User user) {
 *      ...
 *      }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作描述（用于日志显示）
     * 
     * 默认值：方法名
     */
    String value() default "";

    /**
     * 是否记录参数值
     * 
     * 默认值：true
     */
    boolean logArgs() default true;

    /**
     * 是否记录返回值
     * 
     * 默认值：true
     */
    boolean logResult() default true;

    /**
     * 是否记录执行时间
     * 
     * 默认值：true
     */
    boolean logTime() default true;
}
