package com.example.errorBook.common.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class QuestionDto implements Serializable {
    private static final long serialVersionUID = 1L;
    //学科id
    private Long subjectId;
    //章id
    private Long chapterId;
    //节id
    private Long sectionId;
    //题目
    private String title;
}
