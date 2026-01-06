package common.response;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应体格式
 * 所有API响应都应使用这个格式
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 返回码 */
    private int code;

    /** 返回信息 */
    private String message;

    /** 返回数据 */
    private T data;

    /** 时间戳 */
    private long timestamp;

    /** traceId (用于问题追踪) */
    private String traceId;

    /**
     * 构造成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .code(0)
                .message("操作成功")
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造成功响应（带消息）
     */
    public static <T> BaseResponse<T> success(String message, T data) {
        return BaseResponse.<T>builder()
                .code(0)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造失败响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造失败响应（带数据）
     */
    public static <T> BaseResponse<T> error(int code, String message, T data) {
        return BaseResponse.<T>builder()
                .code(code)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构造失败响应（带traceId）
     */
    public static <T> BaseResponse<T> error(int code, String message, String traceId) {
        return BaseResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .traceId(traceId)
                .build();
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return code == 0;
    }
}
