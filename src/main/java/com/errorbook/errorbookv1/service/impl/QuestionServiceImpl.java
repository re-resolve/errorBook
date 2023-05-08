package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.common.dto.QuestionCollectionDto;
import com.errorbook.errorbookv1.common.exception.CustomException;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.entity.Section;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.mapper.QuestionMapper;
import com.errorbook.errorbookv1.service.*;
import com.errorbook.errorbookv1.util.RedisAnnotations.MethodAspect01;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private SectionService sectionService;
    @Autowired
    private CollectionService collectionService;
    /**
     * 根据学科、章、节的id去查名称
     * @param questionCollectionDto
     * @return
     */
    @Override
    public QuestionCollectionDto setSubChapSecName(QuestionCollectionDto questionCollectionDto) {
        log.info("setSubChapSecName");
        Subject subject = subjectService.getById(questionCollectionDto.getSubjectId());
        if (subject==null) {
            throw new CustomException("查询的学科id为"+questionCollectionDto.getSubjectId()+"的学科为空");
        }
        Chapter chapter = chapterService.getById(questionCollectionDto.getChapterId());
        if (chapter==null) {
            throw new CustomException("查询的章id为"+questionCollectionDto.getChapterId()+"的章为空");
        }
        Section section = sectionService.getById(questionCollectionDto.getSectionId());
        if (section==null) {
            throw new CustomException("查询的节id为"+questionCollectionDto.getSectionId()+"的节为空");
        }
        questionCollectionDto.setSubjectName(subject.getSubjectName());
        
        questionCollectionDto.setChapterName(chapter.getChapterName());
        
        questionCollectionDto.setSectionName(section.getSectionName());
        
        return questionCollectionDto;
    }
    
    /**
     * 查询错题列表的题目
     * @param subjectId
     * @param chapterId
     * @param sectionId
     * @return
     */
    @Override
    @MethodAspect01
    public Res listQuestionTitle(Long subjectId, Long chapterId, Long sectionId) {
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        log.info("sql操作：listSimpleQutestion");
        List<Question> list = null;
        try {
            queryWrapper.eq(subjectId != 0, Question::getSubjectId, subjectId)
                    .eq(chapterId != 0, Question::getChapterId, chapterId)
                    .eq(sectionId != 0, Question::getSectionId, sectionId)
                    .select(Question::getId, Question::getSubjectId, Question::getChapterId, Question::getSectionId, Question::getPublisher, Question::getTitle);

            list = this.list(queryWrapper);
        } catch (Exception e) {
            log.error("listSimpleQutestion报错");
            e.printStackTrace();
            throw new CustomException("listSimpleQutestion报错");
        }
        
        return Res.succ(list);
    }
    
    @Override
    @MethodAspect01
    public Res listCollectedQuestion(Long userId) {
        List<com.errorbook.errorbookv1.entity.Collection> list = collectionService.list(new LambdaQueryWrapper<com.errorbook.errorbookv1.entity.Collection>()
                .eq(com.errorbook.errorbookv1.entity.Collection::getUserId, userId).select(com.errorbook.errorbookv1.entity.Collection::getQuestionId));
        
        List<Long> questionIds = list.stream().map(com.errorbook.errorbookv1.entity.Collection::getQuestionId).collect(Collectors.toList());
        
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Question::getId, Question::getSubjectId, Question::getChapterId, Question::getSectionId, Question::getPublisher, Question::getTitle)
                .in(Question::getId,questionIds);
        
        Collection<Question> questions = this.list(queryWrapper);
        
        return questionsSetName(questions);
    }
    
    @Override
    @MethodAspect01
    public Res listQuestionInfo(Long[] questionIds) {
        if (questionIds.length == 0) {
            return Res.succ(this.list());
        }
        Collection<Question> questions = this.listByIds(Arrays.asList(questionIds));
        
        return questionsSetName(questions);
    }
    
    
    /**
     * 给错题添加学科、章、节的名称
     * @param questions
     * @return
     */
    private Res questionsSetName(Collection<Question> questions) {
        Collection<QuestionCollectionDto> dtoList = new ArrayList<>();
        
        try {
            for (Question question : questions) {
                QuestionCollectionDto questionCollectionDto = new QuestionCollectionDto();
                
                BeanUtils.copyProperties(question, questionCollectionDto);
                
                //设置学科、章、节的名称
                questionCollectionDto = this.setSubChapSecName(questionCollectionDto);
                
                dtoList.add(questionCollectionDto);
            }
        } catch (BeansException e) {
            e.printStackTrace();
            throw new CustomException("添加学科、章、节的名称时报错");
        }
        return Res.succ(dtoList);
    }
}
