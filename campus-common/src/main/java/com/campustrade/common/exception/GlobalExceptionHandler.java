package com.campustrade.common.exception;

import com.campustrade.common.response.ApiResponse;
import com.campustrade.common.response.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 统一处理 servlet MVC 服务（user/product/order/ai/message）抛出的异常。
 *
 * <p>各服务保持现有约定：HTTP 状态固定为 200，真正的语义放在响应体的 {@code code} 字段里。
 * 这里只负责兜底——把业务异常、常见的请求解析错误和未预期异常都转换成统一的
 * {@link ApiResponse}，避免直接把框架的 500 堆栈暴露给调用方。</p>
 *
 * <p>campus-gateway 是 WebFlux 模块，不会加载这个处理器（见自动配置的条件守卫）。</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 业务异常：使用异常自带的 code 和 message。
     */
    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusinessException(BusinessException exception) {
        log.warn("business exception: code={}, message={}", exception.getCode(), exception.getMessage());
        return ApiResponse.fail(exception.getCode(), exception.getMessage());
    }

    /**
     * 请求体 JSON 无法解析，例如格式错误或类型不匹配。
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Void> handleNotReadable(HttpMessageNotReadableException exception) {
        log.warn("malformed request body: {}", exception.getMessage());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, "request body is invalid");
    }

    /**
     * 路径变量或查询参数类型不匹配，例如 {@code /order/abc} 期望 Long。
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        log.warn("argument type mismatch: name={}, value={}", exception.getName(), exception.getValue());
        return ApiResponse.fail(ResultCode.BAD_REQUEST, "parameter '" + exception.getName() + "' is invalid");
    }

    /**
     * Bean Validation 校验失败（后续接入 {@code @Valid} 时生效）。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValidation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldError() == null
                ? "request validation failed"
                : exception.getBindingResult().getFieldError().getDefaultMessage();
        log.warn("request validation failed: {}", message);
        return ApiResponse.fail(ResultCode.BAD_REQUEST, message);
    }

    /**
     * 兜底：所有未预期的异常都转成 SYSTEM_ERROR，并记录完整堆栈方便排查。
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleUnexpected(Exception exception) {
        log.error("unexpected exception", exception);
        return ApiResponse.fail(ResultCode.SYSTEM_ERROR, "internal server error");
    }
}
