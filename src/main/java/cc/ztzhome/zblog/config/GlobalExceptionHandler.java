package cc.ztzhome.zblog.config;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * <p>
 * 使用 {@link RestControllerAdvice} 注解，统一处理控制器层抛出的异常，
 * 并返回统一格式的响应模型 {@link ResponseModel}。
 * </p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验失败异常
     * <p>
     * 当使用 {@code @Valid} 注解的实体类参数校验不通过时，会抛出 {@link MethodArgumentNotValidException}。
     * 该方法提取第一个校验失败的字段错误信息作为响应消息，并返回状态码为 {@code 400} 的错误响应。
     * </p>
     *
     * @param ex 参数校验异常对象
     * @return 包含错误信息的统一响应模型
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseModel<?> handleValidation(MethodArgumentNotValidException ex) {
        // 从所有字段错误中获取第一个错误消息，若没有则使用默认消息
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage()) // 提取每个字段错误的默认消息
                .findFirst()                     // 只取第一条错误信息
                .orElse("参数校验失败");          // 兜底消息

        // 返回 BAD_REQUEST 状态的错误响应
        return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, message);
    }

    /**
     * 处理所有未被捕获的通用异常
     * <p>
     * 作为兜底处理器，处理系统中未明确定义的其他异常，避免敏感信息泄露给客户端。
     * 通常返回服务器内部错误（500）的通用响应。
     * </p>
     *
     * @param ex 异常对象
     * @return 服务器内部错误的统一响应模型
     */
    @ExceptionHandler(Exception.class)
    public ResponseModel<?> handleException(Exception ex) {
        // 返回预定义的服务器错误响应（一般为500状态码）
        return ResponseModel.serverError();
    }
}