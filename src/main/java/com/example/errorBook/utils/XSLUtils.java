package com.example.errorBook.utils;

import com.example.errorBook.entity.Question;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("JavaDoc")
@Slf4j
public class XSLUtils extends icu.resolve2cu.Wheel.Utils.DocxToDocument {
    /**
     * 将word中的数学公式转化为latex语言、图片转化为base64编码的字符串，并返回OMML格式的Document对象
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
    (File docxFile, String ommlXslPath, String mmlXslPath
            , String xslFolderPath, String latexLeftSeparator, String latexRightSeparator
            , String pictureLeftSeparator, String pictureRightSeparator)
            throws IOException, InvalidFormatException, XPathExpressionException, SAXException, ParserConfigurationException, TransformerException
    {
        Document ommlDoc = docx2Document(docxFile, ommlXslPath, mmlXslPath, xslFolderPath, latexLeftSeparator, latexRightSeparator, pictureLeftSeparator, pictureRightSeparator);
        List<Question> questions = ommlDoc2Questions(ommlDoc);
        return questions;
    }
    
    /**
     * OMML Doc -> List<Question>
     *
     * @param ommlDoc
     * @return
     * @throws XPathExpressionException
     */
    private static List<Question> ommlDoc2Questions(Document ommlDoc) throws XPathExpressionException {
        // 创建XPath对象并编译表达式
        XPath xpath = XPathFactory.newInstance().newXPath();
        
        /** 遍历所有节点，用于输出*/
        //其中 local-name() 函数用于获取节点名称中的本地名称部分，避免命名空间的问题。
        XPathExpression expr = xpath.compile("//*[local-name()='p']");
        // 执行XPath表达式，获取所有的节点
        NodeList nodeList = (NodeList) expr.evaluate(ommlDoc, XPathConstants.NODESET);
        
        // TODO 遍历每一个节点,标记出想要提取的固定部分
        List<Integer> indexs = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            String textContent = node.getTextContent();
            int length = textContent.length();
            if (length > 3) {
                if (textContent.startsWith("题目：")
                        || textContent.startsWith("错解：")
                        || textContent.startsWith("分析：")
                        || textContent.startsWith("正解：")) {
                    indexs.add(i);
                }
            }
        }
        if (indexs.size() % 4 != 0) {
            log.error("导入的word文档格式错误，每一道题都必须包含4个部分：题目、错解、分析、正解");
            return null;
        }
        List<Question> questions = text2json(nodeList, indexs);
        return questions;
    }
    
    /**
     * List<String> to List<Question>
     * 将多行字符串设置进对应的实体类Question中的位置
     *
     * @param nodeList
     * @param indexs
     * @return
     */
    private static List<Question> text2json(NodeList nodeList, List<Integer> indexs) {
        List<Question> questions = new ArrayList<>();
        int index = 0;
        StringBuilder titleBuilder = new StringBuilder();
        StringBuilder wrongAnsBuilder = new StringBuilder();
        StringBuilder analysisBuilder = new StringBuilder();
        StringBuilder correctAnsBuilder = new StringBuilder();
        for (int i = 0; i < indexs.size(); i++) {
            int endIndex;
            if (i + 1 < indexs.size()) {
                endIndex = indexs.get(i + 1);
            } else endIndex = nodeList.getLength();
            
            // 分别读取题目、错解、分析、正解
            if (index == 0) {
                index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, titleBuilder, i);
            } else if (index == 1) {
                index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, wrongAnsBuilder, i);
            } else if (index == 2) {
                index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, analysisBuilder, i);
            } else if (index == 3) {
                index = getIndexAndAppendStrings(nodeList, indexs, index, endIndex, correctAnsBuilder, i);
                
                Question question = new Question();
                // 将本题对象添加进List中
                question.setTitle(titleBuilder.toString())
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
    
    public static void main(String[] args) throws Exception {
        
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
        String outputPath="C:\\Users\\Think\\Desktop\\output.docx";//打印输出的word文档路径可选
        
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
    }
    
}
