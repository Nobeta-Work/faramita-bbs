package online.faramita.bbs.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "服务器异常"),
    
    // =========== 登陆认证模块 ===========
    UNAUTHORIZED(401, "未登陆，请先授权"),
    FORBIDDEN(403, "权限不足"),
    USERNAME_NOT_FOUND(400, "用户名或密码错误"),
    PASSWORD_NOT_MATCHES(400, "用户名或密码错误"), 
    ACCOUNT_FORBBIDEN(403, "该账户已被封禁")
    
    
    
    ;
    

    private final Integer code;
    private final String msg;

}
