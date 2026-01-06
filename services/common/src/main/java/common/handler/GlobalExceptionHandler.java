package common.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.slf4j.MDC;
import common.exception.BaseException;
import common.exception.BusinessException;
import common.exception.ValidationException;
import common.exception.SystemException;
import common.constant.ErrorConstants;
import common.response.BaseResponse;
import common.logger.AppLoggerExt;

/**
 * 全局异常处理器
 * 拦截所有异常并返回统一的错误响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

        /**
         * 处理业务异常
         */
        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<BaseResponse<Void>> handleBusinessException(BusinessException ex) {
                AppLoggerExt.logBusinessError(ex.getErrorCode(), ex.getMessage());
                String traceId = MDC.get("traceId");

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        /**
         * 处理参数验证异常
         */
        @ExceptionHandler(ValidationException.class)
        public ResponseEntity<BaseResponse<Void>> handleValidationException(ValidationException ex) {
                AppLoggerExt.logValidationError(ex.getMessage());
                String traceId = MDC.get("traceId");

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * 处理系统异常
         */
        @ExceptionHandler(SystemException.class)
        public ResponseEntity<BaseResponse<Void>> handleSystemException(SystemException ex) {
                AppLoggerExt.logSystemError(ex.getErrorCode(), ex.getMessage(), ex.getCause());
                String traceId = MDC.get("traceId");

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }

        /**
         * 处理自定义BaseException
         */
        @ExceptionHandler(BaseException.class)
        public ResponseEntity<BaseResponse<Void>> handleBaseException(BaseException ex) {
                String traceId = MDC.get("traceId");

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ex.getErrorCode())
                                .message(ex.getMessage())
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.OK).body(response);
        }

        /**
         * 处理Spring MVC参数验证异常（@Valid）
         */
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<BaseResponse<Void>> handleMethodArgumentNotValidException(
                        MethodArgumentNotValidException ex) {
                BindingResult bindingResult = ex.getBindingResult();
                String defaultMessage = bindingResult.getFieldErrors().isEmpty()
                                ? "参数验证失败"
                                : bindingResult.getFieldErrors().get(0).getDefaultMessage();

                AppLoggerExt.logValidationError(defaultMessage);
                String traceId = MDC.get("traceId");

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ErrorConstants.VALIDATION_FAILED)
                                .message(defaultMessage)
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        /**
         * 处理所有其他异常
         */
        @ExceptionHandler(Exception.class)
        public ResponseEntity<BaseResponse<Void>> handleException(Exception ex) {
                AppLoggerExt.logSystemError(ErrorConstants.SYSTEM_ERROR, "未知异常", ex);
                String traceId = MDC.get("traceId");

                String errorMessage = ex.getMessage() != null ? ex.getMessage() : "系统异常";

                BaseResponse<Void> response = BaseResponse.<Void>builder()
                                .code(ErrorConstants.SYSTEM_ERROR)
                                .message("系统异常: " + errorMessage)
                                .timestamp(System.currentTimeMillis())
                                .traceId(traceId)
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
}
