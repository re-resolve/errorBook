package com.errorbook.errorbookv1.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 题目实体类
 */
@Data
@Accessors(chain = true)
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    //学科id
    private Long subjectId;
    //章id
    private Long chapterId;
    //节id
    private Long sectionId;
    //发布者的id
    private Long publisher;
    //题目
    @NotBlank(message = "题目不能为空")
    private String title;
    //错解
    @NotBlank(message = "错解不能为空")
    private String wrongAns;
    //分析
    @NotBlank(message = "分析不能为空")
    private String analysis;
    //正解
    @NotBlank(message = "正解不能为空")
    private String correctAns;
    //更改时间
    @TableField(fill = FieldFill.INSERT_UPDATE)//插入或修改时自动修改为当前时间
    private Long updateTime;
}
