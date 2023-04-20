package com.errorbook.errorbookv1.common.exception;


import com.errorbook.errorbookv1.common.lang.Res;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.ShiroException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 数据库报错
     * @param e
     * @return
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = MyBatisSystemException.class)
    public Res hander(MyBatisSystemException e){
        log.error("数据库发生错误：------{}",e);
        return Res.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(),e.getMessage(),null);
    }
    
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(value = ShiroException.class)
    public Res handler(ShiroException e) {
        log.error("登录或验证时异常：----------------{}", e);
        return Res.fail(401, e.getMessage(), null);
    }
    // 不具有对应的角色
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthorizedException.class)
    public Res handler(UnauthorizedException e) {
        return Res.fail(401, "你当前的角色没有权限访问", null);
    }
    
    // 捕捉shiro的异常（角色权限）
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(UnauthenticatedException.class)
    public Res handler(UnauthenticatedException e) {
        return Res.fail(401, "你没有权限访问", null);
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public Res handler(MethodArgumentNotValidException e) {
        log.error("实体校验异常：----------------{}", e);
        BindingResult bindingResult = e.getBindingResult();
        ObjectError objectError = bindingResult.getAllErrors().stream().findFirst().get();

        return Res.fail(objectError.getDefaultMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = IllegalArgumentException.class)
    public Res handler(IllegalArgumentException e) {
        log.error("Assert异常：----------------{}", e);
        return Res.fail(e.getMessage());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = RuntimeException.class)
    public Res handler(RuntimeException e) {
        log.error("运行时异常：----------------{}", e);
        return Res.fail(e.getMessage());
    }
    
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(CustomException.class)
    public Res exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        
        return Res.fail(500,ex.getMessage(),null);
    }
    
}
