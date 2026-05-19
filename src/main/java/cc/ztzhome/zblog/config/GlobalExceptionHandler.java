package cc.ztzhome.zblog.config;

import cc.ztzhome.zblog.bean.response.ResponseModel;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseModel<?> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getDefaultMessage())
                .findFirst()
                .orElse("参数校验失败");

        return ResponseModel.error(ResponseModel.CODE_BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseModel<?> handleException(Exception ex) {
        return ResponseModel.serverError();
    }
}