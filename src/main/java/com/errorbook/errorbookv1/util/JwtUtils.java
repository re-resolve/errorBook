package com.errorbook.errorbookv1.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * jwt工具类
 */
@Slf4j
@Data
@Component
@ConfigurationProperties(prefix = "config.jwt")
public class JwtUtils {
    
    // 以下3个变量都由配置文件中决定，在此处赋值是没用的
    private String secret;
    private long expire ;
    private String header;
    
    /**
     * 生成jwt token
     */
    public String generateToken(String account, Long roleId) {
        long currentTimeMillis = System.currentTimeMillis();
        //Date nowDate = new Date();
        //过期时间
        Date expireDate = new Date(currentTimeMillis + expire);
        //System.out.println(expireDate);
    
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                //.setSubject(account+"")
                .claim("account", account)
                .claim("roleId", roleId)
                .setIssuedAt(new Date(currentTimeMillis))
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }
    
    /**
     * 获取JWT的信息
     */
    public Claims getClaimByToken(String token) {
        Claims claims = null;
        try {
            claims = Jwts.parser()
                    .setSigningKey(secret)
                    .parseClaimsJws(token)
                    .getBody();
            
        } catch (ExpiredJwtException e) {
            log.warn("token expired error ", e);
            claims = e.getClaims();
        }
        return claims;
    }
    
    /**
     * token是否过期
     *
     * @return true：过期
     */
    public boolean isTokenExpired(Date expiration) {
        return expiration.before(new Date(System.currentTimeMillis()));
    }
}