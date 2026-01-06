package common.exception;

/**
 * 业务异常类
 * 用于表示业务逻辑中的错误
 * 例如：用户不存在、余额不足、重复操作等
 */
public class BusinessException extends BaseException {

    private static final long serialVersionUID = 1L;

    public BusinessException(String errorMessage) {
        super(500, errorMessage);
    }

    public BusinessException(int errorCode) {
        super(errorCode);
    }

    public BusinessException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public BusinessException(int errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public BusinessException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
