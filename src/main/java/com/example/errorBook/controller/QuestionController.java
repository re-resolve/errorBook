package com.example.errorBook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.dto.QuestionDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Question;
import com.example.errorBook.service.ChapterService;
import com.example.errorBook.service.QuestionService;
import com.example.errorBook.service.SectionService;
import com.example.errorBook.service.SubjectService;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.example.errorBook.utils.XSLUtils.parseEquations;

@Slf4j
@RestController("/question")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private SectionService sectionService;
    
    /**
     * 上传word文档
     *
     * @param docxFile
     * @return
     * @throws XPathExpressionException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws InvalidFormatException
     * @throws TransformerException
     * @throws SAXException
     */
    /*@RequiresAuthentication
    @PostMapping("/uploadQuestion")
    public Res uploadQuestion(@RequestParam Long id, @RequestParam("docxFile") File docxFile) throws
            XPathExpressionException, IOException, ParserConfigurationException, InvalidFormatException, TransformerException, SAXException {
        
        //String filePath = "/test1.docx";
        //File docxFile = new File(filePath);
        String ommlXslPath = "/XSLT/OMML2MML.XSL";
        String mmlXslPath = "/XSLT/mml2tex/mmltex.xsl";
        List<Question> results = parseEquations
                (docxFile, ommlXslPath, mmlXslPath, "/XSLT/mml2tex/"
                        , "<latex>", "</latex>"
                        , "<picture>", "</picture>"
                        , subjectService, chapterService, sectionService);
        if (results != null) {
            questionService.saveBatch(results);
            for (Question result : results) {
                log.info(result.toString());
            }
            return Res.succ(results);
        }
        return Res.fail("导入的word文档格式错误");
    }*/
    
    /**
     * 根据多个id查对应题目信息
     * 不传则查询全部
     *
     * @param questionIds
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/listQuestion")
    public Res listQuestion(@RequestBody IdListDto questionIds) {
        if(questionIds.getIds().length == 0){
            return Res.succ(questionService.list());
        }
        Collection<Question> questions = questionService.listByIds(Arrays.asList(questionIds.getIds()));
        return Res.succ(questions);
    }

    /**
     * 分页+模糊查询题目
     * 传值为0则查询全部
     * 按更改时间降序
     *
     * @param page
     * @param pageSize
     * @param questionDto
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/pageQuestion")
    public Res pageQuestion(int page, int pageSize, @RequestBody QuestionDto questionDto) {
        Long subjectId = questionDto.getSubjectId();
        Long chapterId = questionDto.getChapterId();
        Long sectionId = questionDto.getSectionId();
        String title = questionDto.getTitle();
        Page<Question> questionPage = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(subjectId != 0, Question::getSubjectId, subjectId)
                .eq(chapterId != 0, Question::getChapterId, chapterId)
                .eq(sectionId != 0, Question::getSectionId, sectionId)
                .like(StringUtils.isNotEmpty(title), Question::getTitle, title);
        //查询排序为最近更改的
        queryWrapper.orderByDesc(Question::getUpdateTime);
        
        questionService.page(questionPage, queryWrapper);
        
        return Res.succ(questionPage);
    }
    
    /*    *//**
     * 新增一道题目
     * @param question
     * @return
     *//*
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PostMapping("/insert")
    public Res insert(@Validated @RequestBody Question question){
        return null;
    }*/
    
    /**
     * 删除多道题目
     *
     * @param ids
     * @return
     */
    @RequiresAuthentication
    @RequiresRoles(value = {"老师", "管理员"}, logical = Logical.OR)
    @DeleteMapping("/deleteByIds")
    public Res deleteByIds(@RequestBody IdListDto ids) {
        boolean sucToDel = questionService.removeByIds(Arrays.asList(ids.getIds()));
        if(sucToDel) return Res.succ("删除成功");
        else return Res.fail("删除失败");
    }
    
    /*    *//**
     * 修改一道题目的和名称
     * 需判断学科、章、节是否存在
     * @param question
     * @return
     *//*
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PutMapping("/update")
    private Res update(@Validated @RequestBody Question question){
        return null;
    }*/
}
