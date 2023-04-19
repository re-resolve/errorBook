package com.errorbook.errorbookv1.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 *  认证用户的实体类
 */
@Data
public class AccountProfile implements Serializable {
    /**
     * 账户
     */
    private String account;
    
    /**
     * 角色ID/权限
     */
    private Long roleId;
    
    //设置AccountProfile的主体id
    public String getId() {
        return account;
    }
    
    
    
}