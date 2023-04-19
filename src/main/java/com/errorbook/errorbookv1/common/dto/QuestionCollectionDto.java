package com.errorbook.errorbookv1.common.dto;


import com.errorbook.errorbookv1.entity.Question;
import lombok.Data;

@Data
public class QuestionCollectionDto extends Question {
    private Boolean ifCollected;
}
