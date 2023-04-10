package com.example.errorBook.shiro;

import com.example.errorBook.entity.Role;
import com.example.errorBook.service.RoleService;
import com.example.errorBook.service.UserService;
import com.example.errorBook.util.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
//AccountRealm是shiro进行登录或者权限校验的逻辑所在，算是核心了，我们需要重写3个方法，分别是
//        supports：为了让realm支持jwt的凭证校验
//        doGetAuthorizationInfo：权限校验
//        doGetAuthenticationInfo：登录认证校验

@Component
@Slf4j
public class AccountRealm extends AuthorizingRealm {
    
    @Autowired
    JwtUtils jwtUtils;
    
    
    @Autowired
    UserService userService;
    
    @Autowired
    RoleService roleService;
    
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }
    
    /**
     * 获取用户角色并授权（实际只做了角色管理，没有做权限管理）
     *  （一个用户只用一次此方法)
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        //获取通过AuthenticationInfo传过来的profile
        AccountProfile profile = (AccountProfile) principals.getPrimaryPrincipal();
        
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        
        //从数据库获取该用户角色  (实际上，角色和用户之间是多对1的关系，此项目中是1对1)
        Role role = roleService.getById(profile.getRoleId());
        
        String roleName = role.getRoleName();
        
        /** 将该用户的角色添加进simpleAuthorizationInfo
         *  则可以指定角色的名称*/
        simpleAuthorizationInfo.addRole(roleName);

        return simpleAuthorizationInfo;
    }
    
    /**
     * 登录验证,根据token获取用户信息
     * （调用要验证的接口就会用此方法）
     *
     * @param token
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
    
        JwtToken jwtToken = (JwtToken) token;
        
        Claims claims = jwtUtils.getClaimByToken((String) jwtToken.getPrincipal());
        //用户账号
        String account = claims.get("account", String.class);
        //角色id
        Long roleId = claims.get("roleId",Long.class);
        
        //用于返回给下一步去获取用户对应的角色
        AccountProfile profile = new AccountProfile();

        profile.setAccount(account);
    
        profile.setRoleId(roleId);
    
        log.info("account: "+account);
        log.info("roleId: "+roleId);
    
        return new SimpleAuthenticationInfo(profile, jwtToken.getCredentials(), getName());
    }
}