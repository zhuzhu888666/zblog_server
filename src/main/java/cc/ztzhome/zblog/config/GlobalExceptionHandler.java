package cc.ztzhome.zblog.config;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 * 使用 @RestControllerAdvice 统一处理 Controller 层抛出的异常，
 * 并将异常信息转换为统一的 ResponseModel 格式返回给客户端。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理参数校验异常（由 @Valid 或 @Validated 触发）
     * 当请求参数不满足 Bean 校验注解（如 @NotNull、@Size 等）时，Spring 会抛出 MethodArgumentNotValidException。
     * 此方法捕获该异常，提取第一个字段校验失败的错误消息，封装成 ResponseModel 返回。
     *
     * @param ex 参数校验失败的异常对象
     * @return 统一的错误响应模型，包含 HTTP 400 状态码和具体的校验失败信息
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseModel<?> handleValidation(MethodArgumentNotValidException ex) {
        // 从 BindingResult 中获取所有字段错误，取第一条错误消息
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())   // 获取注解中定义的默认错误消息
                .findFirst()                       // 仅返回第一个校验失败的信息
                .orElse("参数校验失败");             // 如果没有具体错误消息，使用默认提示

        // 返回错误响应模型（设 ResponseModel.error 接受错误码和错误消息）
        return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, message);
    }
}