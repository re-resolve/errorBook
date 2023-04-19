package com.errorbook.errorbookv1.config;


import com.errorbook.errorbookv1.shiro.AccountRealm;
import com.errorbook.errorbookv1.shiro.JwtFilter;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.mgt.SessionsSecurityManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.spring.web.config.DefaultShiroFilterChainDefinition;
import org.apache.shiro.spring.web.config.ShiroFilterChainDefinition;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.crazycake.shiro.RedisCacheManager;
import org.crazycake.shiro.RedisSessionDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class shiroConfig {
    
    @Autowired
    JwtFilter jwtFilter;
    
    @Bean
    public SessionManager sessionManager(RedisSessionDAO redisSessionDAO) {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        
        // inject redisSessionDAO
        sessionManager.setSessionDAO(redisSessionDAO);
        
        // other stuff...
        
        return sessionManager;
    }
    
    @Bean
    public SessionsSecurityManager securityManager(AccountRealm accountRealm, SessionManager sessionManager, RedisCacheManager redisCacheManager) {
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager(accountRealm);
        
        //inject sessionManager
        securityManager.setSessionManager(sessionManager);
        
        // inject redisCacheManager
        securityManager.setCacheManager(redisCacheManager);
        
        // other stuff...
    
        //设置AccountRealm的Principal的主键id
        redisCacheManager.setPrincipalIdFieldName("account");
        return securityManager;
    }
    
    @Bean
    public ShiroFilterChainDefinition shiroFilterChainDefinition() {
        DefaultShiroFilterChainDefinition chainDefinition = new DefaultShiroFilterChainDefinition();
        Map<String, String> filterMap = new LinkedHashMap<>();
        // 配置不会被拦截的链接 顺序判断
        //filterMap.put("/static/**", "anon");      anon：无需认证即可访问，游客身份。  authc：必须认证（登录）才能访问。
        
        filterMap.put("/**", "jwt"); // 主要通过注解方式校验权限
        chainDefinition.addPathDefinitions(filterMap);
        return chainDefinition;
    }
    
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager,
                                                         ShiroFilterChainDefinition shiroFilterChainDefinition) {
        ShiroFilterFactoryBean shiroFilter = new ShiroFilterFactoryBean();
        
        shiroFilter.setSecurityManager(securityManager);
        
        Map<String, Filter> filters = new HashMap<>();
        
        filters.put("jwt", jwtFilter);
        
        shiroFilter.setFilters(filters);
        
        Map<String, String> filterMap = shiroFilterChainDefinition.getFilterChainMap();
    
        // 设置认证界面路径  没有登录的用户请求需要登录的页面时自动跳转到登录页面。(可能地址会携带上JSESSIONID)
        //shiroFilter.setLoginUrl("");
        
        // 设置没有权限默认跳转的页面路径
        //shiroFilter.setUnauthorizedUrl("");
        
        shiroFilter.setFilterChainDefinitionMap(filterMap);
        
        return shiroFilter;
    }
    
    @Bean
    JwtFilter JwtFilter() {
        return new JwtFilter();
    }
    
}
