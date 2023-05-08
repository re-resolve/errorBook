package com.errorbook.errorbookv1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.errorbook.errorbookv1.common.dto.QuestionCollectionDto;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Question;

public interface QuestionService extends IService<Question> {
    /**
     * 根据学科、章、节的id去查名称
     * @param questionCollectionDto
     * @return
     */
    QuestionCollectionDto setSubChapSecName(QuestionCollectionDto questionCollectionDto);
    
    /**
     * 查询错题列表的题目
     * @param subjectId
     * @param chapterId
     * @param sectionId
     * @return
     */
    Res listQuestionTitle(Long subjectId, Long chapterId, Long sectionId);
    
    Res listCollectedQuestion(Long userId);
    
    Res listQuestionInfo(Long[] questionIds);
}
