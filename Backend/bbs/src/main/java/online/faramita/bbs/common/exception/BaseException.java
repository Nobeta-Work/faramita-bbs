package online.faramita.bbs.common.exception;

import lombok.Getter;
import online.faramita.bbs.common.enums.ResultCode;

@Getter
public class BaseException extends RuntimeException {

    // 绑定统一错误枚举
    private final ResultCode resultCode;

    // 构造1：预设枚举提示
    public BaseException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.resultCode = resultCode;
    }
    
    // 构造2：枚举码 + 自定义提示
    public BaseException(ResultCode resultCode, String message) {
        super(message);
        this.resultCode = resultCode;
    }
}
