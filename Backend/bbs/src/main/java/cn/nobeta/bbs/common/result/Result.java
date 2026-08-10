package cn.nobeta.bbs.common.result;

import lombok.Data;
import cn.nobeta.bbs.common.enums.ResultCode;

/**
 * 统一响应结果
 */
@Data
public class Result<T> {

    /** 响应码 */
    private Integer code;
    /** 响应消息 */
    private String msg;
    /** 业务数据 */
    private T data;

    private Result() {} // 私有构造，禁止外部 new

    // ============= 成功静态方法 =============
    public static <T> Result<T> success() {
        return build(ResultCode.SUCCESS, null);
    }

    public static <T> Result<T> success(T data) {
        return build(ResultCode.SUCCESS, data);
    }

    // ============= 失败静态方法 =============
    public static <T> Result<T> fail(ResultCode code) {
        return build(code, null);
    }

    public static <T> Result<T> fail(ResultCode resultCode, String msg) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMsg(msg);
        return result;
    }

    public static <T> Result<T> fail(Integer code, String msg) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMsg(msg);
        return result;
    }

    
    // 内部构建方法
    private static <T> Result<T> build(ResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMsg(resultCode.getMsg());
        result.setData(data);
        return result;
    }
}
