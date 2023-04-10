package com.example.errorBook.shiro;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.support.RequestContextUtils;
import org.springframework.web.util.ServletRequestPathUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * 自定义一个Filter，用来拦截所有的请求判断是否携带Token
 * isAccessAllowed()判断是否携带了有效的JwtToken
 * onAccessDenied()是没有携带JwtToken的时候进行账号密码登录，登录成功允许访问，登录失败拒绝访问
 */

@Slf4j
@Component
public class JwtFilter extends AuthenticatingFilter {
    
    /**
     * filter中抛出的异常无法被 统一异常处理类处理
     * 原因：最终继承到了 servlet 的 Filter 类 Filter 处理是在 控制器之前的 所以  @ControllerAdvice 由spring 提供的增强控制器是无法处理这个异常的 。
     */
    
    @Autowired
    JwtUtils jwtUtils;
    
    @Override
    protected AuthenticationToken createToken(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if (StringUtils.isEmpty(jwt)) {
            return null;
        }
        
        return new JwtToken(jwt);
    }
    
    /**
     * 此方法中过滤了要开放的接口（例如登录接口）
     * @param request
     * @param response
     * @param mappedValue
     * @return
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        
        WebApplicationContext ctx = RequestContextUtils.findWebApplicationContext(httpServletRequest);
        
        RequestMappingHandlerMapping mapping = ctx.getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping.class);
        
        HandlerExecutionChain handler = null;
    
        //RequestPath previousPath = null;
        
        try {
    
            //previousPath = (RequestPath) httpServletRequest.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE);
            //log.info(previousPath.toString());
    
            ServletRequestPathUtils.parseAndCache(httpServletRequest);
            
            handler = mapping.getHandler(httpServletRequest);
            
            log.info(httpServletRequest.getAttribute(ServletRequestPathUtils.PATH_ATTRIBUTE).toString());
            
            Annotation[] declaredAnnotations = ((HandlerMethod) handler.getHandler()).getMethod().getDeclaredAnnotations();
            
            for (Annotation annotation:declaredAnnotations) {
                /**
                 *如果含有@GuestAccess注解，则认为是不需要验证是否登录，
                 *直接放行即可
                 */
                if (GuestAccess.class.equals(annotation.annotationType())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //ServletRequestPathUtils.setParsedRequestPath(previousPath, httpServletRequest);
        
        //log.warn("isAccessAllowed 方法被调用");
        //这里先让它始终返回false来使用onAccessDenied()方法
        return false;
    }
    
    /**
     * 此方法中对过滤之后的请求进行token的检验
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String jwt = request.getHeader("Authorization");
        if (StringUtils.isEmpty(jwt)) {
            log.info("请求标头Authorization为空");
            return true;
        } else {
    
            try {
                // 校验jwt
                Claims claim = jwtUtils.getClaimByToken(jwt);
                if (claim == null || jwtUtils.isTokenExpired(claim.getExpiration())) {
                    if (claim == null) {
                        log.info("jwt claim == null");
                    } else {
                        log.info("token过期");
                    }
                    /** token过期之后的处理 */
                    onLoginFail(servletResponse);
                    return false;
                }
            } catch (Exception e) {
                onLoginFail(servletResponse);
                e.printStackTrace();
                return false;
    
            }
        }
        // 执行登录
        return executeLogin(servletRequest, servletResponse);
    }
    
    /**
     * 对发过来的请求进行跨域的配置
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        
        HttpServletRequest httpServletRequest = WebUtils.toHttp(request);
        HttpServletResponse httpServletResponse = WebUtils.toHttp(response);
        httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
        httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
        // 跨域时会首先发送一个OPTIONS请求，这里我们给OPTIONS请求直接返回正常状态
        if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(org.springframework.http.HttpStatus.OK.value());
            return false;
        }
        
        return super.preHandle(request, response);
    }
    
    
    /**
     * 登录失败时默认返回 403 状态码
     */
    private void onLoginFail(ServletResponse response) throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpResponse.getWriter().write(JSONUtil.toJsonStr(Res.fail(403, "token expired,Please login again", null)));
    }
    
}