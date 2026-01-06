package common.response;

/**
 * 响应工具类
 * 提供便捷的响应构建方法
 */
public class ResponseUtil {

    /**
     * 构建成功响应
     */
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.success(data);
    }

    /**
     * 构建成功响应 (带消息)
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
     * 构建失败响应
     */
    public static <T> BaseResponse<T> error(int code, String message) {
        return BaseResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 构建失败响应 (带数据)
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
     * 构建业务异常响应
     */
    public static <T> BaseResponse<T> businessError(String message) {
        return error(400, message);
    }

    /**
     * 构建系统异常响应
     */
    public static <T> BaseResponse<T> systemError() {
        return error(500, "系统内部错误");
    }

    /**
     * 构建未授权响应
     */
    public static <T> BaseResponse<T> unauthorized() {
        return error(401, "未授权，请先登录");
    }

    /**
     * 构建禁止访问响应
     */
    public static <T> BaseResponse<T> forbidden() {
        return error(403, "禁止访问");
    }

    /**
     * 构建资源不存在响应
     */
    public static <T> BaseResponse<T> notFound() {
        return error(404, "资源不存在");
    }
}
