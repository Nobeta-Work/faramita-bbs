package online.faramita.bbs.common.enums;

public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAIL(500, "服务器异常"),
    
    // 客户端错误
    UNAUTHORIZED(401, "未登陆，请先授权"),
    FORBIDDEN(403, "权限不足"),

    // 业务自定义
    USER_NOT_EXIST(400, "用户不存在")
    
    
    ;
    

    private final Integer code;
    private final String msg;

    ResultCode(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    // getter
    public Integer getCode() { return code; }
    public String getMsg() { return msg; }

}
