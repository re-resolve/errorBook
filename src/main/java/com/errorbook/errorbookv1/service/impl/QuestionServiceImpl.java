package com.errorbook.errorbookv1.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.errorbook.errorbookv1.common.exception.CustomException;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.entity.Section;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.mapper.QuestionMapper;
import com.errorbook.errorbookv1.service.ChapterService;
import com.errorbook.errorbookv1.service.QuestionService;
import com.errorbook.errorbookv1.service.SectionService;
import com.errorbook.errorbookv1.service.SubjectService;
import com.errorbook.errorbookv1.service.dto.QuestionCollectionDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Autowired
    private SubjectService subjectService;
    @Autowired
    private ChapterService chapterService;
    @Autowired
    private SectionService sectionService;
    
    /**
     * 根据学科、章、节的id去查名称
     * @param questionCollectionDto
     * @return
     */
    @Override
    public QuestionCollectionDto setSubChapSecName(QuestionCollectionDto questionCollectionDto) {
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
}
