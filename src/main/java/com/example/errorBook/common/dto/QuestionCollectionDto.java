package com.example.errorBook.common.dto;

import com.example.errorBook.entity.Question;
import lombok.Data;

@Data
public class QuestionCollectionDto extends Question {
    private Boolean ifCollected;
}
