package common.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 幂等性注解
 * 
 * 用于标记需要防止重复提交的方法
 * IdempotentAspect 会自动拦截并防止：
 * - 同一请求短时间内重复调用
 * - 根据 key 生成唯一标识
 * 
 * 使用示例：
 * 
 * @Idempotent(key = "userId", timeout = 5, unit = TimeUnit.SECONDS)
 *                 public void submitOrder(Long userId, Order order) {
 *                 ...
 *                 }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Idempotent {

    /**
     * 生成幂等性 key 的参数名
     * 
     * 支持 SpEL 表达式，例如：
     * - "userId" - 方法参数名
     * - "#userId" - SpEL 格式参数名
     * - "userId+'-'+orderId" - 拼接多个参数
     */
    String key() default "";

    /**
     * 超时时间（在这个时间内不允许重复调用）
     * 
     * 默认值：5 秒
     */
    long timeout() default 5;

    /**
     * 时间单位
     * 
     * 默认值：秒
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * 重复调用时的错误消息
     */
    String message() default "请求过于频繁，请稍后再试";
}
