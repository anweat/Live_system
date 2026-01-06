package common.exception;

/**
 * 基础异常类
 * 所有业务异常都应继承此类
 */
public class BaseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** 错误码 */
    private int errorCode;

    /** 错误信息 */
    private String errorMessage;

    /**
     * 构造函数 - 仅错误码
     */
    public BaseException(int errorCode) {
        super();
        this.errorCode = errorCode;
        this.errorMessage = common.constant.ErrorConstants.getErrorMessage(errorCode);
    }

    /**
     * 构造函数 - 错误码和自定义信息
     */
    public BaseException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数 - 带原始异常
     */
    public BaseException(int errorCode, String errorMessage, Throwable cause) {
        super(errorMessage, cause);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * 构造函数 - 错误码和原始异常
     */
    public BaseException(int errorCode, Throwable cause) {
        super(common.constant.ErrorConstants.getErrorMessage(errorCode), cause);
        this.errorCode = errorCode;
        this.errorMessage = common.constant.ErrorConstants.getErrorMessage(errorCode);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "BaseException{" +
                "errorCode=" + errorCode +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
