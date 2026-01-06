package com.liveroom.finance.handler;

import common.exception.BusinessException;
import common.exception.ValidationException;
import common.response.BaseResponse;
import common.response.ResponseUtil;
import common.logger.TraceLogger;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<Void> handleBusinessException(BusinessException e) {
        TraceLogger.warn("GlobalExceptionHandler", "handleBusinessException", 
                "业务异常: " + e.getMessage());
        return ResponseUtil.error(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常处理
     */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleValidationException(ValidationException e) {
        TraceLogger.warn("GlobalExceptionHandler", "handleValidationException", 
                "参数校验异常: " + e.getMessage());
        return ResponseUtil.error(400, e.getMessage());
    }

    /**
     * 方法参数校验异常处理
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数校验失败";
        TraceLogger.warn("GlobalExceptionHandler", "handleMethodArgumentNotValidException", 
                "方法参数校验异常: " + message);
        return ResponseUtil.error(400, message);
    }

    /**
     * 绑定异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError() != null
                ? e.getBindingResult().getFieldError().getDefaultMessage()
                : "参数绑定失败";
        TraceLogger.warn("GlobalExceptionHandler", "handleBindException", 
                "绑定异常: " + message);
        return ResponseUtil.error(400, message);
    }

    /**
     * 约束违反异常处理
     */
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public BaseResponse<Void> handleConstraintViolationException(ConstraintViolationException e) {
        TraceLogger.warn("GlobalExceptionHandler", "handleConstraintViolationException", 
                "约束违反异常: " + e.getMessage());
        return ResponseUtil.error(400, "参数校验失败: " + e.getMessage());
    }

    /**
     * 通用异常处理
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public BaseResponse<Void> handleException(Exception e) {
        TraceLogger.error("GlobalExceptionHandler", "handleException", 
                "系统异常", e);
        return ResponseUtil.error(500, "系统异常，请稍后重试");
    }
}
