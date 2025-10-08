package online.faramita.bbs.result;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // 成功响应
    public static <T> Result<T> success(T data) {
        return new Result<T>(1, "success", data);   
    }
    public static <T> Result<T> success() {
        return new Result<T>(1, "success", null);   
    }

    // 失败响应
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<T>(code, message, null);   
    }
    public static <T> Result<T> error(Integer code) {
        return new Result<T>(code, "error", null);   
    }
    public static <T> Result<T> error(String message) {
        return new Result<T>(0, message, null);   
    }
    public static <T> Result<T> error() {
        return new Result<T>(0, "error", null);   
    }
}
