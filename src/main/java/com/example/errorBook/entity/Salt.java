package com.example.errorBook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 管理员账号的盐值表
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Salt {
    private static final long serialVersionUID = 1L;
    /**主键id */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    
    /**对应用户的账号 */
    private String account;
    /**盐值 */
    private String saltValue;
}
