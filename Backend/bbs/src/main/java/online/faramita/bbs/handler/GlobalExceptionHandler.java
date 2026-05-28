package online.faramita.bbs.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.common.enums.ResultCode;
import online.faramita.bbs.common.exception.BaseException;
import online.faramita.bbs.common.result.Result;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler(BaseException.class)
    public Result<Void> exceptionHandler(BaseException ex) {
        log.warn("异常：{}<", ex.getMessage());
        return Result.fail(ex.getResultCode(), ex.getMessage());
    }

    /**
     * 捕获全局异常
     * @param e
     * @return
     */
    public Result<Void> handleException(Exception e) {
        log.error("未知异常", e);
        return Result.fail(ResultCode.FAIL);
    }
}
