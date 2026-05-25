package online.faramita.bbs.common.exception;

import online.faramita.bbs.common.enums.ResultCode;

public class BusinessException extends BaseException {

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
    

}
