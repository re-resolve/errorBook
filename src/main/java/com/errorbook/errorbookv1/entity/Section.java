package com.errorbook.errorbookv1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 节的实体类
 */
@Data

public class Section implements Serializable {
    private static final long serialVersionUID = 2L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //章的id
    private Long chapterId;
    //节的名称
    @NotBlank(message = "节的名称不能为空")
    private String sectionName;
    
}
