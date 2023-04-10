package com.example.errorBook.shiro;

import lombok.Data;

import java.io.Serializable;

/**
 *  认证用户的实体类
 */
@Data
public class AccountProfile implements Serializable {

    /**
     * 角色ID/权限
     */
    private Long roleId;

    /**
     * 账户
     */
    private String account;


}