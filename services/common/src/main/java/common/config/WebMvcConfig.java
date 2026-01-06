package common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import common.logger.interceptor.TraceIdInterceptor;

/**
 * Web MVC配置类
 * 注册请求拦截器、格式化器、转换器等
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 注册HTTP请求拦截器
     * TraceIdInterceptor用于处理traceId的提取/生成和日志上下文初始化
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new TraceIdInterceptor())
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/actuator/**", // 排除健康检查端点
                        "/swagger-ui/**", // 排除Swagger文档
                        "/v2/api-docs", // 排除API文档
                        "/webjars/**" // 排除web资源
                );
    }
}
