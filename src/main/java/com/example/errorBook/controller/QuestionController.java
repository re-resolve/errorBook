package com.example.errorBook.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.errorBook.common.dto.IdListDto;
import com.example.errorBook.common.dto.QuestionCollectionDto;
import com.example.errorBook.common.dto.QuestionDto;
import com.example.errorBook.common.lang.Res;
import com.example.errorBook.entity.Question;
import com.example.errorBook.entity.User;
import com.example.errorBook.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.example.errorBook.utils.XSLUtils.parseEquations;

@Slf4j
@RestController
@RequestMapping("/question")
public class QuestionController {
    
    @Autowired
    private QuestionService questionService;
    
    @Autowired
    private SubjectService subjectService;
    
    @Autowired
    private ChapterService chapterService;
    
    @Autowired
    private SectionService sectionService;
    
    @Autowired
    private CollectionService collectionService;
    
    @Autowired
    private UserService userService;
    
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
    @RequiresAuthentication
    @PostMapping("/uploadQuestion")
    public Res uploadQuestion(@RequestParam Long id, @RequestParam("docxFile") File docxFile) throws
            Exception {
        
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
                result.setPublisher(id);
                log.info(result.toString());
            }
            return Res.succ("导入word成功");
        }
        return Res.fail("导入的word文档格式错误");
    }
/*    public static void main(String[] args) throws Exception {
        String filePath = XSLUtils.class.getResource("/test1.docx").getFile();
    
        File docxFile = new File(filePath);
        String ommlXslPath = "/XSLT/OMML2MML.XSL";
        String mmlXslPath = "/XSLT/mml2tex/mmltex.xsl";
        List<Question> results = parseEquations
                (docxFile, ommlXslPath, mmlXslPath, "/XSLT/mml2tex/"
                        , "<latex>", "</latex>"
                        , "<picture>", "</picture>"
                        , subjectService, chapterService, sectionService);
        if (results != null) {
            //questionService.saveBatch(results);
            for (Question result : results) {
                result.setPublisher(2L);
                log.info(result.toString());
            }
        }
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
        if (questionIds.getIds().length == 0) {
            return Res.succ(questionService.list());
        }
        Collection<Question> questions = questionService.listByIds(Arrays.asList(questionIds.getIds()));
        return Res.succ(questions);
    }
    
    /**
     * 根据一个用户id和多个题目id，查该用户对应题目信息及是否被收藏
     * 不传题目id则查询全部
     *
     * @param idListDto
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/searchQuestion")
    public Res searchQuestion(@RequestBody IdListDto idListDto) {
        User user = userService.getById(idListDto.getUserId());
        if (user == null) {
            return Res.fail("该用户不存在");
        }
        
        Collection<QuestionCollectionDto> dtoList = new ArrayList<>();
        
        Collection<Question> questions;
        
        if (idListDto.getIds().length == 0) {
            questions = questionService.list();
        } else {
            questions = questionService.listByIds(Arrays.asList(idListDto.getIds()));
        }
        
        for (Question question : questions) {
            QuestionCollectionDto questionCollectionDto = new QuestionCollectionDto();
            
            BeanUtils.copyProperties(question, questionCollectionDto);
            
            com.example.errorBook.entity.Collection collection = collectionService.getOne(new LambdaQueryWrapper<com.example.errorBook.entity.Collection>()
                    .eq(com.example.errorBook.entity.Collection::getUserId, user.getId())
                    .eq(com.example.errorBook.entity.Collection::getQuestionId, question.getId()));
            //查询用户是否收藏改题目
            questionCollectionDto.setIfCollected(collection != null);
            dtoList.add(questionCollectionDto);
        }
        return Res.succ(dtoList);
        
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
        if (sucToDel) return Res.succ("删除成功");
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
