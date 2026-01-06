package common.annotation;

import java.lang.annotation.*;

/**
 * 参数验证注解
 * 
 * 用于标记需要验证参数的方法
 * ValidateParamAspect 会自动拦截并验证：
 * - 参数是否为空
 * - 参数是否符合指定条件
 * 
 * 使用示例：
 * 
 * @ValidateParam
 *                public void deleteUser(@NotEmpty(message = "ID不能为空") Long
 *                userId) {
 *                ...
 *                }
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ValidateParam {

    /**
     * 是否验证参数（默认 true）
     * 
     * 可以设置为 false 来禁用某个方法的参数验证
     */
    boolean value() default true;

    /**
     * 是否检查所有参数非空
     * 
     * 默认值：false
     */
    boolean notNull() default false;

    /**
     * 自定义验证错误消息
     */
    String message() default "";
}
