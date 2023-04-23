package com.errorbook.errorbookv1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.service.dto.QuestionCollectionDto;

public interface QuestionService extends IService<Question> {
    /**
     * 根据学科、章、节的id去查名称
     * @param questionCollectionDto
     * @return
     */
    QuestionCollectionDto setSubChapSecName(QuestionCollectionDto questionCollectionDto);
}
