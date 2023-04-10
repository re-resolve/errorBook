package com.example.errorBook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 章的实体类
 */
@Data
public class Chapter implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //学科id
    private Long subjectId;
    //章的名称
    @NotBlank(message = "章的名称不能为空")
    private String chapterName;

}
