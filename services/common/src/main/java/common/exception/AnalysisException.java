package common.exception;

/**
 * 数据分析模块异常
 * 继承自BusinessException，用于分析相关的业务错误
 */
public class AnalysisException extends BusinessException {

    private static final long serialVersionUID = 1L;

    public AnalysisException(String errorMessage) {
        super(5001, errorMessage);
    }

    public AnalysisException(int errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public AnalysisException(int errorCode, String errorMessage, Throwable cause) {
        super(errorCode, errorMessage, cause);
    }

    public AnalysisException(int errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}

