package online.faramita.bbs.handler;

import java.sql.SQLIntegrityConstraintViolationException;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;
import online.faramita.bbs.constant.MessageConstant;
import online.faramita.bbs.exception.BaseException;
import online.faramita.bbs.result.Result;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<Void> exceptionHandler(BaseException ex) {
        log.info(">异常：{}<", ex.getMessage());
        return Result.error(ex.getMessage());
    }

    /**
     * SQL语句异常
     * @param ex
     * @return
     */
    @ExceptionHandler
    public Result<Void> exceptionHandler(SQLIntegrityConstraintViolationException ex) {
        String message = ex.getMessage();
        if (message.contains("Duplicate entry")) {
            String[] split = message.split(" ");
            String msg = split[2] + MessageConstant.ALREADY_EXISTS;
            return Result.error(msg);
        } else {
            return Result.error(MessageConstant.UNKNOW_ERROR);
        }
    }
}
