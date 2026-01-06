package common.exception;

/**
 * 参数验证异常
 * 用于参数验证失败的情况
 * 例如：参数格式错误、必需参数缺失等
 */
public class ValidationException extends BaseException {

    private static final long serialVersionUID = 1L;

    public ValidationException(String errorMessage) {
        super(400, errorMessage);
    }

    public ValidationException(int errorCode) {
        super(errorCode);
    }

    public ValidationException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ValidationException(int errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public ValidationException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
