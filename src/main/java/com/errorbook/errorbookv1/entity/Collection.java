package com.errorbook.errorbookv1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

/**
 * 收藏表的实体类
 */
@Data
public class Collection implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //用户id
    private Long userId;
    //题目id
    private Long questionId;
    
}
