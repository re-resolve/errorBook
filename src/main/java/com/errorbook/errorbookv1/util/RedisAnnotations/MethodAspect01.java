package com.errorbook.errorbookv1.util.RedisAnnotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解实现缓存
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface MethodAspect01 {
    //String key();
    
    //String value();
    
    long expireTimes() default 120L; //默认过期时间120s
    
    //int semaphoreCount() default Integer.MAX_VALUE;  //默认限制线程并发数
}
