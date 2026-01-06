package common.exception;

/**
 * 系统异常类
 * 用于表示系统级的异常
 * 例如：数据库连接失败、配置错误、远程服务调用失败等
 */
public class SystemException extends BaseException {

    private static final long serialVersionUID = 1L;

    public SystemException(int errorCode) {
        super(errorCode);
    }

    public SystemException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public SystemException(int errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public SystemException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
