package com.errorbook.errorbookv1.utils;

import cn.hutool.core.lang.UUID;
import com.errorbook.errorbookv1.common.exception.CustomException;
import com.errorbook.errorbookv1.util.OBSHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DocxToDocument {
    public static String ACCESS_KEY_ID ;
    public static String ACCESS_KEY_SECRET ;
    public static String ENDPOINT ;
    public static String OBS_BUCKET_NAME ;
    public static OBSHandler OBS_HANDLER ;
    public static final ConcurrentHashMap<String, XWPFPictureData> PICTURES_MAP = new ConcurrentHashMap<>();
    /*static {
        System.setProperty("javax.xml.parsers.DocumentBuilderFactory", "com.sun.org.apache.xerces.internal.jaxp.DocumentBuilderFactoryImpl");
        System.setProperty("javax.xml.parsers.SAXParserFactory", "com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl");
        System.setProperty("org.apache.xml.jaxp.properties", "com.sun.org.apache.xml.internal.serializer.ToXMLSAXHandler");
        System.setProperty("org.apache.logging.log4j.simplelog.logFile", "System.out");
        System.setProperty("org.apache.logging.log4j.simplelog.log.com.obs.services.internal.ObsProperties", "warn");
        
        
    }*/
    
    /**
     * docx file -> XWPFDocument -> Document -> Latex2Context && Picture2Context -> Document
     *
     * @param docxFile              docx文件
     * @param ommlXslPath           将OMML转化为MML的XSLT文件的路径
     * @param mmlXslPath            将MML转化为Latex的XSLT文件的路径
     * @param xslFolderPath         将MML转化为Latex的其余使用到的一些XSLT文件的文件夹路径
     * @param latexLeftSeparator    转化成latex语言后，它的左分隔符
     * @param latexRightSeparator   转化成latex语言后，它的右分隔符
     * @param pictureLeftSeparator  转化成图片的base64字符串后，它的左分隔符
     * @param pictureRightSeparator 转化成图片的base64字符串后，它的右分隔符
     * @return ommlDoc 返回的结果为内容是OMML格式的Document对象
     * @throws InvalidFormatException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws XPathExpressionException
     * @throws TransformerException
     */
    protected static Document docx2Document(MultipartFile docxFile, String ommlXslPath, String mmlXslPath
            , String xslFolderPath, String latexLeftSeparator, String latexRightSeparator
            , String pictureLeftSeparator, String pictureRightSeparator) throws Exception {
        
        //1、 TODO: 读取Word文档 -> XWPFDocument
        log.info("读取Word文档 -> XWPFDocument");
        InputStream inputStream = docxFile.getInputStream();
        OPCPackage pkg = OPCPackage.open(inputStream);
        XWPFDocument doc = new XWPFDocument(pkg);
        
        //2、 TODO: XWPFDocument -> Document
        log.info("XWPFDocument -> Document");
        Document ommlDoc = DocxToDocument.XWPF2Document(doc);
        
        //3、 TODO: 处理每一个数学公式节点
        log.info("处理数学公式");
        DocxToDocument.setLatex2Context(ommlXslPath, mmlXslPath, xslFolderPath, latexLeftSeparator, latexRightSeparator, ommlDoc);
        
        //4、 TODO 遍历图像节点
        //将图片->bytes->encode(base64)
        log.info("处理图像");
        /**
         * XWPFDocument 对象的 getAllPictures 方法会返回所有不同的图片，不会有重复的图片。
         * 如果多个段落或者表格单元格中使用了同一张图片，它们都会指向同一个 XWPFPictureData 对象。这个方法返回的是所有不同的 XWPFPictureData 对象。
         */
        List<String> pictures = DocxToDocument.pictures2OBS_Link(doc);
        DocxToDocument.setPicture2Context(pictureLeftSeparator, pictureRightSeparator, pictures, ommlDoc);
        return ommlDoc;
    }
    
    /**
     * XWPFDocument -> Document
     *
     * @param doc
     * @return ommlDoc
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    private static Document XWPF2Document(XWPFDocument doc) throws ParserConfigurationException, SAXException, IOException {
        // Get the bytes of the XWPF document
        byte[] docBytes = doc.getDocument().getBody().xmlText().getBytes();
        
        // Convert the bytes to an InputStream
        InputStream docInputStream = new ByteArrayInputStream(docBytes);
        
        // Create a new DocumentBuilderFactory
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        
        // Create a new DocumentBuilder
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        
        // Parse the InputStream and return the Document
        return dBuilder.parse(docInputStream);
    }
    
    /**
     * picture -> context
     *
     * @param pictureLeftSeparator  图片的左分隔符
     * @param pictureRightSeparator 图片的右分隔符
     * @param pictures              图片字符串list
     * @param ommlDoc               操作的文档（omml）
     * @throws XPathExpressionException
     */
    private static void setPicture2Context(String pictureLeftSeparator, String pictureRightSeparator, List<String> pictures, Document ommlDoc) throws XPathExpressionException {
        log.info("picture -> context");
        try {
            // 创建XPath对象并编译表达式
            XPath xpath = XPathFactory.newInstance().newXPath();
        
            int pictureIndex = 0;
            //其中 local-name() 函数用于获取节点名称中的本地名称部分，避免命名空间的问题。
            XPathExpression expr2 = xpath.compile("//*[local-name()='drawing']");
            // 执行XPath表达式，获取所有的节点
            NodeList nodeList2 = (NodeList) expr2.evaluate(ommlDoc, XPathConstants.NODESET);
            log.info("word文档中有 "+nodeList2.getLength()+" 张图片");
            if(nodeList2.getLength()!=pictures.size()){
                throw new CustomException("word文档中存在了表格等无法被读取的内容，请转换成图片再导入");
            }
            // 遍历每一个节点
            for (int i = 0; i < nodeList2.getLength(); i++) {
                Node node = nodeList2.item(i);
                //将picture格式的字符串重新set到节点的文本内容,并添加分隔符
                node.setTextContent(pictureLeftSeparator + pictures.get(pictureIndex++) + pictureRightSeparator);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("读取word文档中的图片时发生异常");
        }
    }
    
    /**
     * omml -> mml -> latex -> context
     *
     * @param ommlXslPath
     * @param mmlXslPath
     * @param xslFolderPath
     * @param latexLeftSeparator  latex的左分隔符
     * @param latexRightSeparator latex的右分隔符
     * @param ommlDoc             操作的文档（omml）
     * @throws XPathExpressionException
     * @throws TransformerException
     */
    private static void setLatex2Context(String ommlXslPath, String mmlXslPath, String xslFolderPath, String latexLeftSeparator, String latexRightSeparator, Document ommlDoc) throws XPathExpressionException, TransformerException {
        log.info("omml -> mml -> latex -> context");
        try {
            // TODO: 遍历数学节点
            // 创建XPath对象并编译表达式
            XPath xpath = XPathFactory.newInstance().newXPath();
            //其中 local-name() 函数用于获取节点名称中的本地名称部分，避免命名空间的问题。
            XPathExpression expr = xpath.compile("//*[local-name()='oMath']");
            // 执行XPath表达式，获取所有的数学公式节点
            NodeList nodeList = (NodeList) expr.evaluate(ommlDoc, XPathConstants.NODESET);
        
            log.info("获取数学公式节点的XML内容(OMML格式的内容)");
            log.info("OMML to MML to Latex");
        
            // 遍历每一个数学公式节点
            for (int i = 0; i < nodeList.getLength(); i++) {
                // TODO: 处理每一个数学公式节点
                Node oMathNode = nodeList.item(i);
                
                String omml = DocxToDocument.getOMML(oMathNode);
                //log.info("omml: " + omml);
                String mml = DocxToDocument.xslConvert(omml, ommlXslPath, null);
                //log.info("mml: " + mml);
                String latex = DocxToDocument.convertMML2Latex(mml, mmlXslPath, xslFolderPath);
                //log.info("latex: " + latex);
                
                //将latex格式的字符串重新set到节点的文本内容,并添加分隔符
                oMathNode.setTextContent(latexLeftSeparator + latex + latexRightSeparator);
            }
        } catch (XPathExpressionException | DOMException | TransformerException e) {
            e.printStackTrace();
            throw new CustomException("处理word文档中的数学公式时发生异常");
        }
    }
    
    /**
     * 获取数学公式节点的XML内容(OMML格式的内容)
     *
     * @param oMathNode
     * @return
     * @throws TransformerException
     */
    private static String getOMML(Node oMathNode) throws TransformerException {
        
        // 获取数学公式节点的XML内容
        TransformerFactory tf = TransformerFactory.newInstance();
        
        Transformer transformer = tf.newTransformer();
        
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
        StringWriter writer = new StringWriter();
        
        transformer.transform(new DOMSource(oMathNode), new StreamResult(writer));
        
        return writer.getBuffer().toString().replaceAll("[\n\r]", "");
    }
    
    /**
     * Picture encode to Base_64 && toString
     *
     * @param doc
     * @return
     */
    private static List<String> pictures2Base64(XWPFDocument doc) {
        log.info("Picture encode to Base_64 && toString");
        List<String> res = new ArrayList<>();
        List<XWPFPictureData> pictures = doc.getAllPictures();
        for (XWPFPictureData picture : pictures) {
            byte[] bytes = picture.getData();
            
            String base64Str = Base64.getEncoder().encodeToString(bytes);
            res.add(base64Str);
        }
        return res;
    }
    
    /**
     * Picture -> HuaweiOBS_Link
     *
     * @param doc
     * @return
     */
    private static List<String> pictures2OBS_Link(XWPFDocument doc) {
        log.info("将图片暂存");
        List<String> res = new ArrayList<>();
        try {
            List<XWPFPictureData> pictures = new ArrayList<>();
            // 遍历所有段落
            for (XWPFParagraph para : doc.getParagraphs()) {
                // 遍历段落中的所有run
                for (XWPFRun run : para.getRuns()) {
                    // 如果run是XWPFPicture，获取它的图片数据
                    if (run.getEmbeddedPictures() != null && run.getEmbeddedPictures().size() != 0) {
                        List<XWPFPicture> embeddedPictures = run.getEmbeddedPictures();
                        for (XWPFPicture picture : embeddedPictures) {
                            pictures.add(picture.getPictureData());
                        }
                    }
                }
            }
            log.info("XWPFDocument共有 "+pictures.size()+" 张不同的图片");
    
            for (XWPFPictureData picture : pictures) {
                // TODO 将picture暂存
                String fileName = "picture/" + UUID.randomUUID() + ".jpg";

                String url = OBS_HANDLER.getUrl(fileName);
                
                PICTURES_MAP.put(fileName,picture);
                
                res.add(url);
                log.info(url);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new CustomException("将图片上传到OBS时发生异常");
        }
        return res;
    }
    
    /**
     * MML to Latex
     *
     * @param mml           MML字符串
     * @param xslPath
     * @param xslFolderPath
     * @return latex
     */
    private static String convertMML2Latex(String mml, String xslPath, String xslFolderPath) {
        mml = mml.substring(mml.indexOf("?>") + 2); //去掉xml的头节点
        //设置xls依赖文件的路径
        URIResolver r = (href, base) -> {
            InputStream inputStream = DocxToDocument.class.getClassLoader().getResourceAsStream(xslFolderPath + href);
            return new StreamSource(inputStream);
        };
        String latex = DocxToDocument.xslConvert(mml, xslPath, r);
        if (latex != null && latex.length() > 1) {
            latex = latex.substring(1, latex.length() - 1);
        }
        return latex;
    }
    
    /**
     * (OMML to MML)
     *
     * @param s           将要被转换的字符串
     * @param xslPath     要使用的XSLT的路径（例如：OMML2MML.XSL和 mmltex.xsl）
     * @param uriResolver 要使用的其他多个XSLT的文件夹路径（解决外部资源的引用问题）
     * @return
     */
    private static String xslConvert(String s, String xslPath, URIResolver uriResolver) {
        TransformerFactory tFac = TransformerFactory.newInstance();
        if (uriResolver != null) tFac.setURIResolver(uriResolver);
    
        InputStream resourceAsStream = DocxToDocument.class.getClassLoader().getResourceAsStream(xslPath);
        if(resourceAsStream==null)log.warn("无法读取要使用的XSLT的路径（：OMML2MML.XSL和 mmltex.xsl）");
        StreamSource xslSource = new StreamSource(resourceAsStream);
        StringWriter writer = new StringWriter();
        try {
            Transformer t = tFac.newTransformer(xslSource);
            Source source = new StreamSource(new StringReader(s));
            Result result = new StreamResult(writer);
            t.transform(source, result);
        } catch (TransformerException e) {
            log.error(e.getMessage(), e);
            throw new CustomException("xslConvert错误");
        }
        return writer.getBuffer().toString();
    }
}
