package common.logger.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;

/**
 * HTTP请求拦截器
 * 用于自动提取/生成traceId并初始化日志上下文
 * 在所有HTTP请求处理前后进行拦截
 * 
 * 注册方式在WebMvcConfig中:
 * registry.addInterceptor(new TraceIdInterceptor())
 * .addPathPatterns("/**");
 */
@Slf4j
@Component
public class TraceIdInterceptor implements HandlerInterceptor {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String TRACE_ID_ATTR = "traceId";
    private static final String START_TIME_ATTR = "startTime";

    /**
     * 在请求处理之前进行拦截
     * 1. 从请求头提取traceId或生成新的
     * 2. 初始化日志上下文
     * 3. 记录请求信息
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        // 提取或生成traceId
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isEmpty()) {
            traceId = TraceLogger.generateTraceId("api-gateway");
        }

        // 存储到request属性中供后续使用
        request.setAttribute(TRACE_ID_ATTR, traceId);
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        // 初始化日志上下文
        String serviceName = request.getHeader("X-Service-Name");
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "api-gateway";
        }
        TraceLogger.initContext(traceId, serviceName);

        // 记录HTTP请求信息
        TraceLogger.logHttpRequest(
                request.getMethod(),
                request.getRequestURI(),
                extractHeaders(request));

        return true;
    }

    /**
     * 在请求处理之后进行拦截
     * 记录响应状态和耗时
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler, ModelAndView modelAndView) throws Exception {
        Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            TraceLogger.logHttpResponse(response.getStatus(), duration);

            // 如果耗时较长进行性能监告
            if (duration > 1000) {
                TraceLogger.logPerformance(
                        "http-handler",
                        request.getRequestURI(),
                        duration,
                        "method", request.getMethod(),
                        "status", response.getStatus());
            }
        }
    }

    /**
     * 在整个请求完成后调用
     * 清空日志上下文避免内存泄漏
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
            Object handler, Exception ex) throws Exception {
        if (ex != null) {
            TraceLogger.error(
                    "http-handler",
                    "request processing failed",
                    request.getRequestURI(),
                    ex);
        }

        // 清空日志上下文
        TraceLogger.clearContext();
    }

    /**
     * 从请求中提取HTTP头信息
     * 过滤掉大型的或敏感的头部
     */
    private java.util.Map<String, String> extractHeaders(HttpServletRequest request) {
        java.util.Map<String, String> headers = new java.util.HashMap<>();
        java.util.Enumeration<String> headerNames = request.getHeaderNames();

        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();

            // 跳过某些大型或敏感的头部
            if ("content-length".equalsIgnoreCase(headerName) ||
                    "content".equalsIgnoreCase(headerName) ||
                    "body".equalsIgnoreCase(headerName)) {
                continue;
            }

            String headerValue = request.getHeader(headerName);
            if (headerValue != null && !headerValue.isEmpty() && headerValue.length() < 256) {
                headers.put(headerName, headerValue);
            }
        }

        return headers;
    }
}
