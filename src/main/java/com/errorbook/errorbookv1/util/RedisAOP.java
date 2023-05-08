package com.errorbook.errorbookv1.util;

import com.errorbook.errorbookv1.common.exception.CustomException;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.util.RedisAnnotations.MethodAspect01;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Configuration
@Aspect
@Slf4j
//@EnableAspectJAutoProxy
//@EnableAspectJAutoProxy(proxyTargetClass = true)
public class RedisAOP {
    
    @Resource
    //@Qualifier
    public RedisTemplate<String, Res> redisTemplate;
    
    //@Resource
    //public RedisTemplate<Long, Res> longResRedisTemplate;
    //
    @Pointcut("@annotation(com.errorbook.errorbookv1.util.RedisAnnotations.MethodAspect01)")
    public void pointcut01() {
    
    }

    /**
     * 切面01
     * 判断是否在redis中有缓存
     * key：方法名 ,hashKey：args ,value：返回结果
     * @param point
     * @return 切入点方法的返回值
     */
    @Around("pointcut01()")
    public Object aroundPointcut01(ProceedingJoinPoint point) {
        Res result;

        log.info("切面1：aroundPointcut01()调用");
        try {
            MethodSignature signature = (MethodSignature) point.getSignature();
            
            Method method = point.getTarget().getClass().getMethod(signature.getName(), signature.getMethod().getParameterTypes());
            
            MethodAspect01 annotation = method.getAnnotation(MethodAspect01.class);
            //获取设置的过期时间
            long expireTimes = annotation.expireTimes();
            //获取方法的参数
            Object[] args = point.getArgs();
            
            //1. 查询redis中是否存在（key：方法名 ,hashKey:args, value：返回结果）
            String key = point.getSignature().toShortString();
            log.info("key: " + key);
            log.info("hashKey: " + Arrays.toString(args));
            Res value = (Res) redisTemplate.opsForHash().get(key,args);
            //log.info("value: " + value);
            //2. 存在则返回
            if (value != null) {
                return value;
            }
            //3. 不存在则查询数据库(业务层方法),并存入redis
            else {
                log.info("查询数据库");
                result = (Res) point.proceed(args);
                //4. 判断请求是否成功
                if (result.getCode() == 200) {
                    redisTemplate.opsForHash().put(key,args, result);
                    redisTemplate.expire(key,expireTimes, TimeUnit.SECONDS);
                }
                //5. 请求不成果或报异常 则不存储redis
                return result;
            }
            
        } catch (Throwable e) {
            e.printStackTrace();
            throw new CustomException("缓存层发生错误");
        }
    }
    
}
