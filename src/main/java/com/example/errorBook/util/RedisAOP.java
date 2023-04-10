package com.example.errorBook.util;


import com.example.errorBook.common.lang.Res;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Aspect
@EnableAspectJAutoProxy
public class RedisAOP {
    
    @Resource
    private RedisTemplate<String, Res> redisTemplate;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Pointcut("@annotation(com.example.errorBook.util.RedisAnnotations.MethodAspect01) && @annotation(org.springframework.web.bind.annotation.GetMapping)")
    private void pointcut01() {
    }
    
    /**
     * 切面01
     * 用于针对Get请求，判断是否在redis中有缓存
     *
     * @param point
     * @return 切入点方法的返回值
     */
    @Around("pointcut01()")
    private Object aroundPointcut01(ProceedingJoinPoint point) {
        Res result = null;
        
        try {
            Object[] args = point.getArgs();
            
            //1. 查询redis中是否存在（key：方法名 value：返回结果）
            String key = point.getSignature().toShortString();
            
            String value = stringRedisTemplate.opsForValue().get(key);
            //2. 存在则返回
            if (value != null) {
                return Res.succ(value);
            }
            //3. 不存在则查询数据库(业务层方法),并存入redis
            else {
                result = (Res) point.proceed(args);
                //4. 判断请求是否成功
                if(result.getCode()==200){
                redisTemplate.opsForValue().set(key,result);
                }
                //5. 请求不成果或报异常 则不存储redis
                return result;
            }
            
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
