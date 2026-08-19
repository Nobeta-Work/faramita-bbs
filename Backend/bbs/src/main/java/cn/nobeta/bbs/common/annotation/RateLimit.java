package cn.nobeta.bbs.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cn.nobeta.bbs.common.enums.Scene;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    Scene scene() default Scene.WRITE;    // 场景
    int capacity() default -1;      // 桶容量，允许的突发请求数
    int refill() default -1;         // 每秒补充
}
