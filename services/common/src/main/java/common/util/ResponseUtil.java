package common.util;

import org.slf4j.MDC;
import common.constant.ErrorConstants;
import common.response.BaseResponse;
import common.response.PageResponse;
import java.util.List;

/**
 * API响应工具类
 * 用于生成统一的API响应
 */
public class ResponseUtil {

    /**
     * 生成成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        String traceId = MDC.get("traceId");
        BaseResponse<T> response = BaseResponse.success(data);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成成功响应（带消息）
     */
    public static <T> BaseResponse<T> success(String message, T data) {
        String traceId = MDC.get("traceId");
        BaseResponse<T> response = BaseResponse.success(message, data);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成成功响应（仅消息）
     */
    public static BaseResponse<Void> success(String message) {
        String traceId = MDC.get("traceId");
        BaseResponse<Void> response = BaseResponse.success(message, null);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成失败响应（使用错误码）
     */
    public static <T> BaseResponse<T> error(int errorCode) {
        String message = ErrorConstants.getErrorMessage(errorCode);
        String traceId = MDC.get("traceId");
        BaseResponse<T> response = BaseResponse.error(errorCode, message);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成失败响应（自定义消息）
     */
    public static <T> BaseResponse<T> error(int errorCode, String message) {
        String traceId = MDC.get("traceId");
        BaseResponse<T> response = BaseResponse.error(errorCode, message);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成失败响应（带数据）
     */
    public static <T> BaseResponse<T> error(int errorCode, String message, T data) {
        String traceId = MDC.get("traceId");
        BaseResponse<T> response = BaseResponse.error(errorCode, message, data);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成参数验证失败响应
     */
    public static <T> BaseResponse<T> validationError(String fieldName, String message) {
        String fullMessage = String.format("参数验证失败: %s - %s", fieldName, message);
        return error(ErrorConstants.VALIDATION_FAILED, fullMessage);
    }

    /**
     * 生成业务异常响应
     */
    public static <T> BaseResponse<T> businessError(int errorCode) {
        String message = ErrorConstants.getErrorMessage(errorCode);
        return error(errorCode, message);
    }

    /**
     * 生成系统异常响应
     */
    public static <T> BaseResponse<T> systemError(String message) {
        return error(ErrorConstants.SYSTEM_ERROR, message != null ? message : "系统异常");
    }

    /**
     * 生成分页响应
     */
    public static <T> PageResponse<T> pageSuccess(List<T> items, long total, int pageNo, int pageSize) {
        String traceId = MDC.get("traceId");
        PageResponse<T> response = PageResponse.of(items, total, pageNo, pageSize);
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }

    /**
     * 生成分页查询失败响应（转换为PageResponse）
     * 用于API端点返回一致的响应类型
     */
    public static <T> PageResponse<T> pageError(int errorCode, String message) {
        // 由于PageResponse不支持错误信息，此方法返回空列表
        // 在实际应用中应该返回BaseResponse<T>或自定义错误分页响应
        PageResponse<T> response = PageResponse.of(null, 0, 1, 1);
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            response.setTraceId(traceId);
        }
        return response;
    }
}
