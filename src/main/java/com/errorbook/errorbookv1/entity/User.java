package com.errorbook.errorbookv1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 用户实体类
 */
@Data
public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //角色id（1-管理员；2-学生；3-老师）
    private Long roleId;
    //账户
    @NotBlank(message = "账户不能为空")
    private String account;
    //密码
    @NotBlank(message = "密码不能为空")
    private String password;
}
