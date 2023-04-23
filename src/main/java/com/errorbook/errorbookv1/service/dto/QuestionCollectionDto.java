package com.errorbook.errorbookv1.service.dto;


import com.errorbook.errorbookv1.entity.Question;
import lombok.Data;

@Data
public class QuestionCollectionDto extends Question {
    //学科name
    private String subjectName;
    //章name
    private String chapterName;
    //节name
    private String sectionName;
    
    //该题是否被收藏
    private Boolean ifCollected;
    
    //每题收藏的人数
    private Long number;
    
}
