package cn.nobeta.bbs.common.exception;

import cn.nobeta.bbs.common.enums.ResultCode;

public class BusinessException extends BaseException {

    public BusinessException(ResultCode resultCode) {
        super(resultCode);
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }
    

}
