package cn.nobeta.bbs.common.aspect;

import java.lang.reflect.Method;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;


import lombok.RequiredArgsConstructor;
import cn.nobeta.bbs.common.annotation.AuditLog;
import cn.nobeta.bbs.common.enums.AuditLogType;
import cn.nobeta.bbs.common.util.AuditLogUtil;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditLogAspect {

    private final AuditLogUtil auditLogUtil;

    @Around("@annotation(auditLog)")
    public Object around(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String module = signature.getDeclaringType().getSimpleName() + "." + method.getName();

        try {
            Object result = joinPoint.proceed();

            if (auditLog.logOnSuccess()) {
                
                auditLogUtil.printLog(method, joinPoint.getArgs(), auditLog, auditLog.type(), module, null);
                
            }

            return result;
        } catch (Throwable ex) {
            if (auditLog.logOnFailure()) {

                auditLogUtil.printLog(method, joinPoint.getArgs(), auditLog, AuditLogType.WARN, module, ex);

            }

            throw ex;
        }
    }
}
