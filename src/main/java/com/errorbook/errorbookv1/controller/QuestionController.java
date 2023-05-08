package com.errorbook.errorbookv1.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.errorbook.errorbookv1.common.dto.IdListDto;
import com.errorbook.errorbookv1.common.dto.QuestionCollectionDto;
import com.errorbook.errorbookv1.common.lang.Res;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.entity.User;
import com.errorbook.errorbookv1.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static com.errorbook.errorbookv1.utils.XSLUtils.parseEquations;


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
    public Res uploadQuestion(@RequestParam("publisher") Long id, @RequestParam MultipartFile docxFile) throws
            Exception {
        
        String ommlXslPath = "XSLT/OMML2MML.XSL";
        String mmlXslPath = "XSLT/mml2tex/mmltex.xsl";
        List<com.errorbook.errorbookv1.entity.Question> results = parseEquations
                (docxFile, ommlXslPath, mmlXslPath, "XSLT/mml2tex/"
                        , "", ""
                        , "<picture>", "<picture>"
                        , subjectService, chapterService, sectionService);
        if (results.size() != 0) {
            for (Question result : results) {
                result.setPublisher(id);
                log.info(result.toString());
            }
            questionService.saveBatch(results);
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
        if (results.size() != 0) {
            //questionService.saveBatch(results);
            for (Question result : results) {
                result.setPublisher(2L);
                log.info(result.toString());
            }
        }
    }
    */
    
    /**
     * 根据多个id查错题的信息
     * 不传则查询全部
     * 一般用于查某到具体题目的信息。
     *
     * @param questionIds
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/listQuestionInfo")
    public Res listQuestionInfo(Long[] questionIds) {
        log.info("根据多个id查错题的信息");
        
        return questionService.listQuestionInfo(questionIds);
    }
    
    /**
     * 查询错题列表的题目
     * 传值为0则查询全部
     * select部分字段
     *
     * @param subjectId 学科id
     * @param chapterId 章id
     * @param sectionId 节id
     * @return
     */
    @RequiresAuthentication
    @GetMapping("/searchQuestionTitle")
    public Res searchQuestionTitle(Long subjectId, Long chapterId, Long sectionId) {
        log.info("查询错题列表的题目");
        
        return questionService.listQuestionTitle(subjectId, chapterId, sectionId);
    }
    
    /**
     * 查询收藏夹中错题的题目
     * @param userId
     * @return
     */
    @RequiresAuthentication
    @GetMapping("/listCollectedQuestion")
    public Res listCollectedQuestion(Long userId) {
        log.info("查询收藏夹中错题的题目");
        return questionService.listCollectedQuestion(userId);
    }
    
    
    
    /**
     * 分页+分类+题目模糊 查询题目的收藏人数
     * 不传则查询全部
     *
     * @param page
     * @param pageSize
     * @param question
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/pageQuestionCollection")
    public Res pageQuestionCollection(int page, int pageSize, @RequestBody Question question) {
        Long subjectId = question.getSubjectId();
        Long chapterId = question.getChapterId();
        Long sectionId = question.getSectionId();
        if (question.getTitle() == null) question.setTitle("");
        String title = question.getTitle();
        
        Page<QuestionCollectionDto> questionCollectionDtoPage = new Page<>();
        
        Page<Question> questionPage = new Page<>(page, pageSize);
        
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        //select部分字段
        queryWrapper.eq(subjectId != 0, Question::getSubjectId, subjectId)
                .eq(chapterId != 0, Question::getChapterId, chapterId)
                .eq(sectionId != 0, Question::getSectionId, sectionId)
                .like(StringUtils.isNotEmpty(title), Question::getTitle, title)
                .select(Question::getId, Question::getSubjectId, Question::getChapterId, Question::getSectionId, Question::getPublisher, Question::getTitle);
        
        questionService.page(questionPage, queryWrapper);
        
        BeanUtils.copyProperties(questionPage, questionCollectionDtoPage, "records");
        
        List<QuestionCollectionDto> dtoList = questionPage.getRecords().stream().map((item) -> {
            QuestionCollectionDto questionCollectionDto = new QuestionCollectionDto();
            
            int count = collectionService.count(new LambdaQueryWrapper<com.errorbook.errorbookv1.entity.Collection>()
                    .eq(com.errorbook.errorbookv1.entity.Collection::getQuestionId, item.getId()));
            BeanUtils.copyProperties(item, questionCollectionDto);
            questionCollectionDto.setNumber((long) count);
            questionCollectionDto.setIfCollected(count != 0);
            
            //设置学科、章、节的名称
            questionCollectionDto = questionService.setSubChapSecName(questionCollectionDto);
            
            return questionCollectionDto;
        }).collect(Collectors.toList());
        // 向列表添加元素,对列表进行排序
        Collections.sort(dtoList, (q1, q2) -> (int) (q2.getNumber() - q1.getNumber()));
        
        questionCollectionDtoPage.setRecords(dtoList);
        
        return Res.succ(questionCollectionDtoPage);
    }
    
    /**
     * 根据一个用户id和多个题目id，查该用户对应题目信息及是否被收藏
     * 不传题目id则查询全部
     * （具体功能：点击题目）
     *
     * @param idListDto
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/searchQuestion")
    public Res searchQuestion(@RequestBody IdListDto idListDto) {
        log.info(String.valueOf(idListDto));
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
            
            com.errorbook.errorbookv1.entity.Collection collection = collectionService.getOne(new LambdaQueryWrapper<com.errorbook.errorbookv1.entity.Collection>()
                    .eq(com.errorbook.errorbookv1.entity.Collection::getUserId, user.getId())
                    .eq(com.errorbook.errorbookv1.entity.Collection::getQuestionId, question.getId()));
            //查询用户是否收藏改题目
            questionCollectionDto.setIfCollected(collection != null);
            //设置学科、章、节的名称
            questionCollectionDto = questionService.setSubChapSecName(questionCollectionDto);
            
            dtoList.add(questionCollectionDto);
        }
        return Res.succ(dtoList);
        
    }
    
    /**
     * 分页+模糊查询题目+查询题目对应是否收藏
     * 传值为0则查询全部
     * <p>
     * select部分字段
     * (用户查询题目列表)旧
     *
     * @param page
     * @param pageSize
     * @param question
     * @return
     */
    @RequiresAuthentication
    @PostMapping("/pageQuestion")
    public Res pageQuestion(int page, int pageSize, Long userId, @RequestBody Question question) {
        Long subjectId = question.getSubjectId();
        Long chapterId = question.getChapterId();
        Long sectionId = question.getSectionId();
        if (question.getTitle() == null) question.setTitle("");
        String title = question.getTitle();
        Page<Question> questionPage = new Page<>(page, pageSize);
        
        Page<QuestionCollectionDto> questionDtoPage = new Page<>(page, pageSize);
        List<QuestionCollectionDto> dtoList;
        
        LambdaQueryWrapper<Question> queryWrapper = new LambdaQueryWrapper<>();
        
        queryWrapper.eq(subjectId != 0, Question::getSubjectId, subjectId)
                .eq(chapterId != 0, Question::getChapterId, chapterId)
                .eq(sectionId != 0, Question::getSectionId, sectionId)
                .like(StringUtils.isNotEmpty(title), Question::getTitle, title)
                .select(Question::getId, Question::getSubjectId, Question::getChapterId, Question::getSectionId, Question::getPublisher, Question::getTitle);
        //查询排序为最近更改的
        //queryWrapper.orderByDesc(com.errorbook.errorbookv1.entity.Question::getUpdateTime);
        
        questionService.page(questionPage, queryWrapper);
        
        BeanUtils.copyProperties(questionPage, questionDtoPage, "records");
        
        dtoList = questionPage.getRecords().stream().map((item) -> {
            QuestionCollectionDto questionCollectionDto = new QuestionCollectionDto();
            
            BeanUtils.copyProperties(item, questionCollectionDto);
            
            com.errorbook.errorbookv1.entity.Collection collection = collectionService.getOne(new LambdaQueryWrapper<com.errorbook.errorbookv1.entity.Collection>()
                    .eq(com.errorbook.errorbookv1.entity.Collection::getUserId, userId)
                    .eq(com.errorbook.errorbookv1.entity.Collection::getQuestionId, item.getId()));
            //查询用户是否收藏改题目
            questionCollectionDto.setIfCollected(collection != null);
            //设置学科、章、节的名称
            questionCollectionDto = questionService.setSubChapSecName(questionCollectionDto);
            
            return questionCollectionDto;
        }).collect(Collectors.toList());
        
        questionDtoPage.setRecords(dtoList);
        
        return Res.succ(questionDtoPage);
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
    
    /*
     * 修改一道题目的和名称
     * 需判断学科、章、节是否存在
     * @param question
     * @return
     *//*
    @RequiresAuthentication
    @RequiresRoles(value = {"老师","管理员"},logical = Logical.OR)
    @PutMapping("/update")
    public Res update(@Validated @RequestBody Question question){
        return null;
    }*/
}
