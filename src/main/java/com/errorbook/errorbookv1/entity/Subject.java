package com.errorbook.errorbookv1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 学科的实体类
 */
@Data
public class Subject implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    //学科的名称
    @NotBlank(message = "学科名称不能为空")
    private String subjectName;
    
}
