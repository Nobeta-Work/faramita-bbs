package online.faramita.bbs.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import online.faramita.bbs.common.enums.AuditLogType;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {

    String message() default "";

    AuditLogType type() default AuditLogType.INFO;

    /**
     * Sprint SpEL
     * Example: "{'username': #p0.username, 'blogId': #p1}"
     */
    String data() default "";
    
    boolean logOnSuccess() default true;
    boolean logOnFailure() default true;
    
}
