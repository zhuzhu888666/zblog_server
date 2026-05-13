package cc.ztzhome.zblog.bean.response;

import lombok.Getter;
import lombok.Setter;

/**
 * 通用响应模型，用于封装API返回的数据、状态码和消息
 *
 * @param <T> 响应数据的类型
 * @author ztzhome
 */
@Setter
@Getter
public class ResponseModel<T> {

    // ==================== 状态码常量定义 ====================

    /** 请求处理成功 */
    public static final int CODE_SUCCESS = 200;

    /** 请求参数错误 */
    public static final int CODE_BAD_REQUEST = 400;

    /** 未认证（未登录） */
    public static final int CODE_UNAUTHORIZED = 401;

    /** 无权限访问 */
    public static final int CODE_FORBIDDEN = 403;

    /** 请求的资源不存在 */
    public static final int CODE_NOT_FOUND = 404;

    /** 服务器内部错误 */
    public static final int CODE_INTERNAL_ERROR = 500;

    // ==================== 响应字段 ====================

    /** 响应状态码，参考上述 CODE_* 常量 */
    private int code;

    /** 响应消息，用于描述处理结果或错误信息 */
    private String message;

    /** 响应携带的数据，泛型类型由调用方指定 */
    private T data;

    // ==================== 构造方法 ====================

    /**
     * 无参构造（通常配合 setter 使用）
     */
    public ResponseModel() {
    }

    /**
     * 全参构造
     *
     * @param code    状态码
     * @param message 消息
     * @param data    数据
     */
    public ResponseModel(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 静态工厂方法（便捷构造成功/失败响应） ====================

    /**
     * 创建一个成功响应（无数据）
     *
     * @param <T> 数据类型
     * @return 成功响应对象
     */
    public static <T> ResponseModel<T> success() {
        return success(null);
    }

    /**
     * 创建一个成功响应（带数据）
     *
     * @param data 响应数据
     * @param <T>  数据类型
     * @return 成功响应对象
     */
    public static <T> ResponseModel<T> success(T data) {
        return success("操作成功", data);
    }

    /**
     * 创建一个成功响应（自定义消息及数据）
     *
     * @param message 成功消息
     * @param data    响应数据
     * @param <T>     数据类型
     * @return 成功响应对象
     */
    public static <T> ResponseModel<T> success(String message, T data) {
        return new ResponseModel<>(CODE_SUCCESS, message, data);
    }

    /**
     * 创建一个错误响应（自定义状态码和消息）
     *
     * @param code    错误状态码
     * @param message 错误消息
     * @param <T>     数据类型（通常为null）
     * @return 错误响应对象
     */
    public static <T> ResponseModel<T> error(int code, String message) {
        return new ResponseModel<>(code, message, null);
    }

    /**
     * 创建一个默认错误响应，参数错误（自定义消息）
     *
     * @param message 错误消息
     * @param <T>     数据类型（通常为null）
     * @return 错误响应对象
     */
    public static <T> ResponseModel<T> error( String message) {
        return new ResponseModel<>(CODE_BAD_REQUEST, message, null);
    }

    /**
     * 创建一个默认的服务器内部错误响应
     *
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ResponseModel<T> serverError() {
        return error(CODE_INTERNAL_ERROR, "服务器内部错误，请稍后再试");
    }

    /**
     * 创建一个资源未找到的响应
     *
     * @param <T> 数据类型
     * @return 错误响应对象
     */
    public static <T> ResponseModel<T> notFound() {
        return error(CODE_NOT_FOUND, "请求的资源不存在");
    }
}