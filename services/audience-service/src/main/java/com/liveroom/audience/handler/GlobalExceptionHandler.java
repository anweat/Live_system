package com.liveroom.audience.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import common.exception.BaseException;
import common.exception.BusinessException;
import common.exception.SystemException;
import common.exception.ValidationException;
import common.logger.TraceLogger;
import common.response.BaseResponse;
import common.response.ResponseUtil;

/**
 * 全局异常处理器
 * 统一处理所有服务层抛出的异常，转换为标准JSON响应
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> handleBusinessException(BusinessException e) {
        TraceLogger.warn("GlobalExceptionHandler", "handleBusinessException", 
            "业务异常: " + e.getMessage());
        
        return ResponseUtil.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理验证异常
     */
    @ExceptionHandler(ValidationException.class)
    public BaseResponse<?> handleValidationException(ValidationException e) {
        TraceLogger.warn("GlobalExceptionHandler", "handleValidationException", 
            "参数验证异常: " + e.getMessage());
        
        return ResponseUtil.error("VALIDATION_FAILED", e.getMessage());
    }

    /**
     * 处理系统异常
     */
    @ExceptionHandler(SystemException.class)
    public BaseResponse<?> handleSystemException(SystemException e) {
        TraceLogger.error("GlobalExceptionHandler", "handleSystemException", 
            "系统异常: " + e.getMessage(), e.getCause());
        
        return ResponseUtil.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public BaseResponse<?> handleException(Exception e) {
        TraceLogger.error("GlobalExceptionHandler", "handleException", 
            "未知异常: " + e.getMessage(), e);
        
        return ResponseUtil.error("SYSTEM_ERROR", "系统异常，请稍后重试");
    }
}
