package com.errorbook.errorbookv1.utils;

import ch.qos.logback.classic.Level;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.errorbook.errorbookv1.common.exception.CustomException;
import com.errorbook.errorbookv1.entity.Chapter;
import com.errorbook.errorbookv1.entity.Question;
import com.errorbook.errorbookv1.entity.Section;
import com.errorbook.errorbookv1.entity.Subject;
import com.errorbook.errorbookv1.service.ChapterService;
import com.errorbook.errorbookv1.service.SectionService;
import com.errorbook.errorbookv1.service.SubjectService;
import com.errorbook.errorbookv1.util.OBSHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFPictureData;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("JavaDoc")
@Slf4j

public class XSLUtils extends DocxToDocument {
    static {
        //华为云OBS服务的配置信息
        ACCESS_KEY_ID = "MELRHZB3PBWUUBMWJPDG";
        ACCESS_KEY_SECRET = "QBsYWvSA3CtGyp2EMBAgp9cNf6ZArAYwL8dZ7rjN";
        ENDPOINT = "obs.cn-south-1.myhuaweicloud.com";
        OBS_BUCKET_NAME = "errorbook1.0";
        OBS_HANDLER = new OBSHandler(ACCESS_KEY_ID, ACCESS_KEY_SECRET, ENDPOINT,OBS_BUCKET_NAME);
        // 通过获取slf4j日志工厂类的配置文件路径（ch.qos.logback.classic.Logger是Logback框架的核心组件之一，用于在Java应用程序中记录日志信息。）
        // 通过 getLogger 方法，可以为不同的类("com.obs")创建不同的日志记录器实例，并通过这些实例记录不同的日志消息。
        ch.qos.logback.classic.Logger obsLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger("com.obs");
        // 设置只有当warn及以上的日志级别才会打印到控制台中
        obsLogger.setLevel(Level.WARN);
    }
    /**
     * 将word中的数学公式转化为latex语言、图片转化为华为云obs的对象的下载链接的字符串，并返回OMML格式的Document对象
     *
     * @param docxFile
     * @param ommlXslPath
     * @param mmlXslPath
     * @param xslFolderPath
     * @param latexLeftSeparator
     * @param latexRightSeparator
     * @param pictureLeftSeparator
     * @param pictureRightSeparator
     * @return questions
     * @throws IOException
     * @throws InvalidFormatException
     * @throws XPathExpressionException
     * @throws SAXException
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public static List<Question> parseEquations
    (MultipartFile docxFile, String ommlXslPath, String mmlXslPath
            , String xslFolderPath, String latexLeftSeparator, String latexRightSeparator
            , String pictureLeftSeparator, String pictureRightSeparator
            , SubjectService subjectService, ChapterService chapterService, SectionService sectionService)
            throws Exception {
        log.info("docx转换成Document");
        Document ommlDoc = docx2Document(docxFile, ommlXslPath, mmlXslPath, xslFolderPath, latexLeftSeparator, latexRightSeparator, pictureLeftSeparator, pictureRightSeparator);
        
        log.info("ommlDoc转换成实体类Questions");
        List<Question> questions = ommlDoc2Questions(ommlDoc, subjectService, chapterService, sectionService);
        
        log.info("word文档格式正确，开始将图片上传至华为云OBS服务");
        // 成功转换为实体类后，才上传图片
        for (Map.Entry<String, XWPFPictureData> fileNameAndPictureData : PICTURES_MAP.entrySet()) {
            //获取图片的字节数据
            byte[] bytes = fileNameAndPictureData.getValue().getData();
            //上传图片
            OBS_HANDLER.putFileByStream(OBS_BUCKET_NAME, fileNameAndPictureData.getKey(), new ByteArrayInputStream(bytes));
        }
        return questions;
    }
    
    /**
     * OMML Doc -> List<Question>
     *
     * @param ommlDoc
     * @param subjectService
     * @param chapterService
     * @param sectionService
     * @return
     * @throws XPathExpressionException
     */
    private static List<Question> ommlDoc2Questions(Document ommlDoc, SubjectService subjectService, ChapterService chapterService, SectionService sectionService) throws XPathExpressionException {
        log.info("OMML Doc -> List<Question>");
        // 创建XPath对象并编译表达式
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        /** 遍历所有节点，用于输出*/
        //其中 local-name() 函数用于获取节点名称中的本地名称部分，避免命名空间的问题。
        XPathExpression expr = xpath.compile("//*[local-name()='p']");
        // 执行XPath表达式，获取所有的节点
        NodeList nodeList = (NodeList) expr.evaluate(ommlDoc, XPathConstants.NODESET);
        // TODO 遍历每一个节点,标记出想要提取的固定部分
        List<Integer> categoryIndexs = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String textContent = node.getTextContent();
            System.out.println(textContent);
            int length = textContent.length();
            if (length > 2) {
                if (textContent.startsWith("学科：")
                        || textContent.startsWith("章：")
                        || textContent.startsWith("节：")) {
                    categoryIndexs.add(i);
                }
            }
        }
        if (categoryIndexs.size() % 3 != 0 || categoryIndexs.size() == 0) {
            log.error("导入的word文档格式错误，每一类题都必须包含3个部分：学科、章、节(并且各自后面接上中文冒号)");
            throw new CustomException("导入的word文档格式错误，每一类题都必须包含3个部分：学科、章、节(并且各自后面接上中文冒号)");
        }
        // TODO 遍历每一个节点,标记出想要提取的固定部分
        List<Integer> indexs = new ArrayList<>();
        int order=0;
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String textContent = node.getTextContent();
            int length = textContent.length();
            if (length >= 3) {
                if(textContent.startsWith("题目：")||textContent.startsWith("错解：")||textContent.startsWith("分析：")||textContent.startsWith("正解：")){
                    if (order==0&&textContent.startsWith("题目：")) {
                        indexs.add(i);
                        ++order;
                    }
                    else if (order==1&&textContent.startsWith("错解：")) {
                        indexs.add(i);
                        ++order;
                    }
                    else if (order==2&&textContent.startsWith("分析：")) {
                        indexs.add(i);
                        ++order;
                    }
                    else if (order==3&&textContent.startsWith("正解：")) {
                        indexs.add(i);
                        order=0;
                    }
                    else{
                        log.error("导入的word文档格式错误，题目、错解、分析、正解这4个标识符的顺序错误，具体在word文档中的第" + i+"行（不包括空行）内容为：("+textContent+")的上一个标识符处出现了错误，【每一道题都必须包含4个部分：题目、错解、分析、正解(并且各自后面接上中文冒号)】");
                        throw new CustomException("导入的word文档格式错误，题目、错解、分析、正解这4个标识符的顺序错误，具体在word文档中的第" + i+"行（不包括空行）内容为：("+textContent+")的上一个标识符处出现了错误，【每一道题都必须包含4个部分：题目、错解、分析、正解(并且各自后面接上中文冒号)】");
                    }
                }
            }
        }
        if (indexs.size() % 4 != 0 || indexs.size() == 0) {
            log.error("导入的word文档格式错误(题目、错解、分析、正解这4个标识符的数量为" + indexs.size()+")，每一道题都必须包含4个部分：题目、错解、分析、正解(并且各自后面接上中文冒号)");
            throw new CustomException("导入的word文档格式错误，每一道题都必须包含4个部分：题目、错解、分析、正解(并且各自后面接上中文冒号)");
        }
        List<Question> questions = text2json(nodeList, categoryIndexs, indexs, subjectService, chapterService, sectionService);
        return questions;
    }
    
    /**
     * List<String> to List<Question>
     * 将多行字符串设置进对应的实体类Question中的位置
     *
     * @param nodeList       文档内容的节点列表
     * @param categoryIndexs 分类标签的列表
     * @param indexs         题目标签的列表
     * @param subjectService
     * @param chapterService
     * @param sectionService
     * @return
     */
    private static List<Question> text2json(NodeList nodeList, List<Integer> categoryIndexs, List<Integer> indexs
            , SubjectService subjectService, ChapterService chapterService, SectionService sectionService) {
        List<Question> questions = new ArrayList<>();
        int index = 0;
        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder wrongAnsBuilder = new StringBuilder();
        StringBuilder analysisBuilder = new StringBuilder();
        StringBuilder correctAnsBuilder = new StringBuilder();
        
        Long subjectId = null;
        String subjectName =null;
        Long chapterId = null;
        String chapterName =null;
        Long sectionId = null;
        String sectionName =null;
    
        int j = 0;
        for (int i = 0; i < categoryIndexs.size(); i++) {
            if (i % 3 == 0) {//学科
                subjectName = nodeList.item(categoryIndexs.get(i)).getTextContent().substring(3);
                Subject subjectServiceOne = subjectService.getOne(new LambdaQueryWrapper<Subject>().eq(Subject::getSubjectName, subjectName));
                if (subjectServiceOne != null) {
                    subjectId = subjectServiceOne.getId();
                }
                else {
                    throw new CustomException("导入的word中学科: "+subjectName+" 不存在，无法将文本内容转换为对象");
                }
            } else if (i % 3 == 1) {//章
                chapterName = nodeList.item(categoryIndexs.get(i)).getTextContent().substring(2);
                Chapter chapterServiceOne = chapterService.getOne(new LambdaQueryWrapper<Chapter>().eq(Chapter::getSubjectId,subjectId).eq(Chapter::getChapterName, chapterName));
                if (chapterServiceOne != null) {
                    chapterId = chapterServiceOne.getId();
                }
                else {
                    throw new CustomException("导入的word中学科为: "+subjectName+"的章："+chapterName+" 不存在，无法将文本内容转换为对象");
                }
            } else {//节
                sectionName = nodeList.item(categoryIndexs.get(i)).getTextContent().substring(2);
                Section sectionServiceOne = sectionService.getOne(new LambdaQueryWrapper<Section>().eq(Section::getChapterId,chapterId).eq(Section::getSectionName, sectionName));
                if (sectionServiceOne != null) {
                    sectionId = sectionServiceOne.getId();
                    for (; j < indexs.size(); j++) {
                        int endIndex;
                        if (j + 1 < indexs.size()) {
                            endIndex = indexs.get(j + 1);
                        } else endIndex = nodeList.getLength();

                        if (i != categoryIndexs.size() - 1) {//只要这个分类不是最后一个分类，则他后面还有新的分类
                            //如果下一个title的行数比下一个分类的学科的行数还要大，说明碰到了当前分类的最后一题的最后一个正解
                            if (indexs.get(j + 1) > categoryIndexs.get(i + 1)) endIndex = categoryIndexs.get(i + 1);
                            //如果当前这个title的行数比下一个分类的学科的行数还要大，说明已经进入了新的分类了
                            if (indexs.get(j) > categoryIndexs.get(i + 1)) {
                                break;
                            }
                        }
                        // 分别读取题目、错解、分析、正解
                        if (index == 0) {
                            index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, titleBuilder, j);
                        } else if (index == 1) {
                            index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, wrongAnsBuilder, j);
                        } else if (index == 2) {
                            index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, analysisBuilder, j);
                        } else if (index == 3) {
                            index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, correctAnsBuilder, j);
                            
                            Question question = new Question();
                            // 将本题对象添加进List中
                            question.setSubjectId(subjectId)
                                    .setChapterId(chapterId)
                                    .setSectionId(sectionId)
                                    .setTitle(titleBuilder.toString())
                                    .setWrongAns(wrongAnsBuilder.toString())
                                    .setAnalysis(analysisBuilder.toString())
                                    .setCorrectAns(correctAnsBuilder.toString());
                            questions.add(question);
                            //清空stringBuilder
                            titleBuilder.setLength(0);
                            wrongAnsBuilder.setLength(0);
                            analysisBuilder.setLength(0);
                            correctAnsBuilder.setLength(0);
                            
                        }
                    }
                }
                else{
                    throw new CustomException("导入的word中的学科为："+subjectName+" 的章为："+chapterName+" 的节: "+ sectionName+" 不存在，无法将文本内容转换为对象");
                }
            }
        }
        
        return questions;
    }
    
    /**
     * 将每个部分所包含的多行字符串都进行读取，统一append进一个string中
     *
     * @param nodeList
     * @param indexs
     * @param index
     * @param endIndex
     * @param stringBuilder
     * @param i
     * @return
     */
    private static int getIndexAndAppendStrings(NodeList nodeList, List<Integer> indexs, int index, int endIndex, StringBuilder stringBuilder, int i) {
        index = (index + 1) % 4;
        for (int j = indexs.get(i); j < endIndex; j++) {
            String content = nodeList.item(j).getTextContent();
            if (j == indexs.get(i)) {
                stringBuilder.append(content.substring(3));
                continue;
            }
            stringBuilder.append(content);
        }
        return index;
    }
    
/*    public static void main(String[] args) throws Exception {
        
        String filePath = XSLUtils.class.getResource("/test1.docx").getFile();
        
        File docxFile = new File(filePath);
        
        String ommlXslPath = "/XSLT/OMML2MML.XSL";//此处不能更改（相关使用到的xsl文件（XSLT）已存在于本jar包中）
        
        String mmlXslPath = "/XSLT/mml2tex/mmltex.xsl";//此处不能更改（相关使用到的xsl文件（XSLT）已存在于本jar包中）
        
        String xslFolderPath = "/XSLT/mml2tex/";//此处不能更改（相关使用到的xsl文件（XSLT）已存在于本jar包中）
        
        String latexLeftSeparator = "<latex>";
        String latexRightSeparator = "</latex>";
        String pictureLeftSeparator = "<picture>";
        String pictureRightSeparator = "</picture>";
        
        Document document = docx2Document(docxFile, ommlXslPath, mmlXslPath, xslFolderPath
                , latexLeftSeparator, latexRightSeparator
                , pictureLeftSeparator, pictureRightSeparator);
        //其中 local-name() 函数用于获取节点名称中的本地名称部分，避免命名空间的问题。
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//*[local-name()='p']");
        // 执行XPath表达式，获取所有的节点
        NodeList nodeList = (NodeList) expr.evaluate(document, XPathConstants.NODESET);
        String outputPath = "C:\\Users\\Think\\Desktop\\output.docx";//打印输出的word文档路径可选
        
        XWPFDocument outputDocument = new XWPFDocument();
        
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String textContent = node.getTextContent();
            System.out.println(textContent);
            outputDocument.createParagraph().createRun().setText(textContent);
        }
        FileOutputStream fos = new FileOutputStream(outputPath);
        outputDocument.write(fos);
        fos.close();
        outputDocument.close();
    }*/
    
}
