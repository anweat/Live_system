package common.aop;

import common.annotation.ValidateParam;
import common.exception.ValidationException;
import common.logger.AppLoggerExt;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Set;

/**
 * 参数验证切面
 * 
 * 功能：
 * - 拦截带 @ValidateParam 注解的方法
 * - 自动验证方法参数
 * - 支持 @NotNull、@NotEmpty 等标准验证注解
 * - 支持自定义验证逻辑
 * 
 * 使用示例：
 * 
 * @ValidateParam(notNull = true)
 *                        public void deleteUser(@NotNull(message = "用户ID不能为空")
 *                        Long userId) {
 *                        ...
 *                        }
 */
@Aspect
@Component
public class ValidateParamAspect {

    private final Validator validator;

    public ValidateParamAspect(Validator validator) {
        this.validator = validator;
    }

    /**
     * 参数验证切面（环绕通知）
     * 
     * @param joinPoint               连接点
     * @param validateParamAnnotation 参数验证注解
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("@annotation(validateParamAnnotation)")
    public Object validateAround(ProceedingJoinPoint joinPoint, ValidateParam validateParamAnnotation)
            throws Throwable {

        // 如果验证被禁用，直接执行
        if (!validateParamAnnotation.value()) {
            return joinPoint.proceed();
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // 验证参数
        if (validateParamAnnotation.notNull()) {
            // 验证所有参数非空
            validateAllParametersNotNull(args, signature, className, methodName);
        }

        // 使用 Validator 进行标准验证（@NotNull、@NotEmpty 等）
        validateWithAnnotations(args, method, className, methodName);

        // 执行目标方法
        return joinPoint.proceed();
    }

    /**
     * 验证所有参数非空
     */
    private void validateAllParametersNotNull(Object[] args, MethodSignature signature,
            String className, String methodName) {
        if (args == null || args.length == 0) {
            return;
        }

        String[] paramNames = signature.getParameterNames();
        for (int i = 0; i < args.length; i++) {
            if (args[i] == null) {
                String message = "参数 " + paramNames[i] + " 不能为空";
                AppLoggerExt.warn(className, methodName, "参数验证失败：{}", message);
                throw new ValidationException(message);
            }
        }
    }

    /**
     * 使用标准验证注解进行验证
     */
    private void validateWithAnnotations(Object[] args, Method method,
            String className, String methodName) {
        if (args == null || args.length == 0 || validator == null) {
            return;
        }

        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < args.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }

            // 检查参数是否有 @Valid 注解
            if (hasValidAnnotation(parameters[i])) {
                // 执行 Bean Validation
                Set<ConstraintViolation<Object>> violations = validator.validate(arg);
                if (!violations.isEmpty()) {
                    StringBuilder message = new StringBuilder("参数验证失败：");
                    for (ConstraintViolation<?> violation : violations) {
                        message.append(violation.getPropertyPath())
                                .append(" ")
                                .append(violation.getMessage())
                                .append(";");
                    }
                    AppLoggerExt.warn(className, methodName, "参数验证失败：{}", message.toString());
                    throw new ValidationException(message.toString());
                }
            }
        }
    }

    /**
     * 检查参数是否有 @Valid 注解
     */
    private boolean hasValidAnnotation(Parameter parameter) {
        return parameter.getAnnotation(Valid.class) != null;
    }
}
