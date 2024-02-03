package com.hwwh.hwai.controller;

import io.minio.*;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.errors.*;
import io.minio.http.Method;
import org.apache.poi.sl.draw.DrawPaint;
import org.apache.poi.sl.usermodel.PaintStyle;
import org.apache.poi.sl.usermodel.PictureData;
import org.apache.poi.sl.usermodel.TextParagraph;
import org.apache.poi.xslf.usermodel.*;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URLConnection;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
/*word转PPT功能
* SJF 2024.01.05*/
/*@RestController
@RequestMapping("/convert")
@CrossOrigin(origins = "http://localhost:8080")*/
public class WordToPPTController {

    private final MinioClient minioClient;
    private final String endpoint = "http://192.168.2.21:9000"; // MinIO服务器地址
    private final String accessKey = "SBE109P05XJ3S923LPQW"; // MinIO访问密钥
    private final String secretKey = "83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ"; // MinIO密钥
    private final String bucketName = "libai"; // 存储桶名称
    private final String bucketFileName = "uploadfile"; // 存储桶名称
    final int MAX_CONTENT_PER_SLIDE = 10;

    //初始化MinIO
    public WordToPPTController() {
        Provider credentialProvider = new StaticProvider(accessKey, secretKey, null);
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentialsProvider(credentialProvider)
                .build();
    }


    @PostMapping("/convertWordToPPT")
    public ResponseEntity<String> convertWordToPPT(@RequestParam("wordFile") MultipartFile wordFilePath) throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 加载Word文档
        XWPFDocument wordDoc = new XWPFDocument(wordFilePath.getInputStream());
        // 创建一个PPT文档
        XMLSlideShow ppt = new XMLSlideShow();

        XSLFSlide slide = null;
        XSLFTextShape shape = null;
        int contentCount = 0;

        //1.获取ppt幻灯片长和宽
        int witdhInPoints = 1024 * 72 / 96;
        int heightInPoints = 768 * 72 / 96;
        Dimension pageSize = new Dimension(witdhInPoints,heightInPoints);
        ppt.setPageSize(pageSize);
        double pptwidth = pageSize.getWidth();
        double pptheight = pageSize.getHeight();


        for (IBodyElement element : wordDoc.getBodyElements()) {
            //检查是否需要新的幻灯片
            if(slide == null || contentCount >= MAX_CONTENT_PER_SLIDE){
                // 为每个Word段落创建一个新的PPT幻灯片
                slide = ppt.createSlide();
                // 在幻灯片中创建文本框
                shape = slide.createTextBox();
                shape.setAnchor(new Rectangle2D.Double(0,0,pptwidth,pptheight));
                //contentCount = 0; // 重置内容计数器
            }

            if(element instanceof XWPFParagraph){
                XWPFParagraph para = (XWPFParagraph) element;
                String paragraphText = para.getText();
                boolean containsUnderscore = paragraphText.contains("____");
                boolean isNewLineRequired = !paragraphText.matches("^[ABCD]\\)|\\(\\d+\\)|.*[，,.].*") && !containsUnderscore && !paragraphText.endsWith("。");
                XSLFTextParagraph xslfParagraph = shape.addNewTextParagraph();
                xslfParagraph.setTextAlign(TextParagraph.TextAlign.LEFT);
                for (XWPFRun run : para.getRuns()) {
                    if(run.getEmbeddedPictures().size() > 0){
                        //处理图片
                        for (XWPFPicture pic : run.getEmbeddedPictures()) {
                            XWPFPictureData picData = pic.getPictureData();
                            byte[] picBytes = picData.getData();

                            String extension = picData.suggestFileExtension().toLowerCase();
                            if("webp".equals(extension)){
                                picBytes = convertWebPToJPEG(picBytes);
                                extension = "jpeg";//更新扩展名为转换后的格式
                            }
                            //PictureData.PictureType pictureType = PictureData.PictureType.valueOf(picData.suggestFileExtension().toUpperCase());
                            PictureData.PictureType pictureType = getPictureType(extension);
                            XSLFPictureData xslfPictureData = ppt.addPicture(picBytes, pictureType);

                            XSLFPictureShape picShape = slide.createPicture(xslfPictureData);
                            // 设置图片位置和大小
                            //获取word文件图片长和宽
                            BufferedImage image = ImageIO.read(new ByteArrayInputStream(picBytes));
                            int picwidth = image.getWidth()/2;
                            int picheight = image.getHeight()/2;
                            double picX = (pptwidth - picwidth)/2;
                            double picY = (pptheight - picheight)/2;
                            picShape.setAnchor(new Rectangle2D.Double(picX, picY, picwidth, picheight));
                            //contentCount++;
                        }
                    }else{
                        //处理文本
                        //处理段落文本

                        //检查段落是否以特定模式开始（如ABCD选项，括号内的文本等）
                        if(!run.text().isEmpty()){
                            if(isNewLineRequired){
                                xslfParagraph  = shape.addNewTextParagraph();
                                xslfParagraph.setTextAlign(TextParagraph.TextAlign.LEFT);
                            }
                            XSLFTextParagraph xslfTextParagraph = shape.addNewTextParagraph();
                            //xslfTextParagraph.setTextAlign(TextParagraph.TextAlign.CENTER);
                            XSLFTextRun xslfRun = xslfTextParagraph.addNewTextRun();

                            xslfRun.setText(run.text());
                            xslfRun.setFontSize(run.getFontSizeAsDouble()); // 设置字体大小
                            xslfRun.setBold(run.isBold());                 // 设置粗体
                            xslfRun.setItalic(run.isItalic());             // 设置斜体
                            if(run.getUnderline() != UnderlinePatterns.NONE){
                                xslfRun.setUnderlined(true);
                            }
                            // 设置字体类型（如果需要）
                            String fontFamily = run.getFontFamily();
                            if (fontFamily != null) {
                                xslfRun.setFontFamily(fontFamily);
                            }
                            // 可以添加更多格式设置，如字体颜色等
                            String colorStr = run.getColor();
                            Color color = null;
                            if (colorStr != null) {
                                int r = Integer.valueOf(colorStr.substring(0, 2), 16);
                                int g = Integer.valueOf(colorStr.substring(2, 4), 16);
                                int b = Integer.valueOf(colorStr.substring(4, 6), 16);
                                color = new Color(r, g, b);
                            }

                            if (color != null) {
                                PaintStyle paintStyle = DrawPaint.createSolidPaint(color);
                                xslfRun.setFontColor(paintStyle);
                            }

                            //contentCount++;
                        }

                    }

                }
                /*// 只有当段落不是以特定格式结束时，才增加内容计数
                if(isNewLineRequired){
                    contentCount++;//增加内容计数
                }*/

            }

            //TODO
            //设置字体颜色大小等

        }

        // 保存PPT文件到服务器的临时目录
        String convertedFileName = "converted_"+ UUID.randomUUID()+".pptx";
        File pptTempFile = new File(System.getProperty("java.io.tmpdir"), convertedFileName/*pptFilePath.getOriginalFilename()*/);
        FileOutputStream out = new FileOutputStream(pptTempFile);
        ppt.write(out);
        out.close();

        //上传到MinIO
            //检查桶是否已经存在
            boolean isExist = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketFileName).build());
            if(!isExist){
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketFileName).build());
            }
        // 上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder().bucket(bucketFileName).object(convertedFileName).stream(
                        new FileInputStream(pptTempFile), pptTempFile.length(), -1)
                        .contentType("application/vnd.openxmlformats-officedocument.presentationml.presentation")// 直接指定 MIME 类型
                        .build()
        );
        pptTempFile.delete(); // 删除临时文件


        // 关闭文档
        ppt.close();
        wordDoc.close();
        return ResponseEntity.ok(convertedFileName);
    }

    @PostMapping("/applyPptTemplate")
    public ResponseEntity<String> applyPptTemplate(@RequestParam("pptTemplate") MultipartFile pptTemplateFile,
                                              @RequestParam("convertedFileName") String convertedFileName) {
        try {
            // 从 MinIO 下载之前转换的PPT文件
            InputStream convertedPptStream = minioClient.getObject(
                    GetObjectArgs.builder().bucket(bucketFileName).object(convertedFileName).build()
            );
            // 读取之前转换的PPT文件
            XMLSlideShow convertedPpt = new XMLSlideShow(convertedPptStream);

            // 读取PPT模板文件
            XMLSlideShow templatePpt = new XMLSlideShow(pptTemplateFile.getInputStream());

            // 应用模板到转换后的PPT
            // 所有模板幻灯片的布局应用到转换后的PPT的每个幻灯片
            for (XSLFSlide templateSlide : templatePpt.getSlides()) {
                // 创建新的幻灯片并复制布局
                //XSLFBackground background = templatePpt.getSlides().get(0).getBackground();
//                templateSlide.setFollowMasterBackground(true);
                XSLFSlide newSlide = convertedPpt.createSlide(templateSlide.getSlideLayout());

                // 复制文本框
                for (XSLFShape shape : templateSlide.getShapes()) {
                    if (shape instanceof XSLFTextShape) {
                        XSLFTextShape txtShape = (XSLFTextShape) shape;
                        XSLFTextShape newTxtShape = newSlide.createTextBox();
                        newTxtShape.setText(txtShape.getText());

                        // 复制文本框的样式
                        newTxtShape.setAnchor(txtShape.getAnchor());
                        // 这里可以添加更多的样式复制逻辑，如字体样式、颜色等
                    }
                }
                //复制背景图

            }
                File finalPptFile = new File(System.getProperty("java.io.tmpdir"), convertedFileName);
                FileOutputStream out = new FileOutputStream(finalPptFile); // 覆盖或创建新文件
                convertedPpt.write(out);
                out.close();

                String contentType = URLConnection.guessContentTypeFromName(convertedFileName);
                if(contentType == null){
                    contentType = "application/octet-stream";
                }

            // 再次上传到 MinIO
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketFileName).object(convertedFileName).stream(
                            new FileInputStream(finalPptFile), finalPptFile.length(), -1)
                            .contentType(contentType)
                            .build()
            );
            finalPptFile.delete(); // 删除临时文件

            // 生成预签名的下载链接
            String downloadUrl = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketFileName)
                            .object(convertedFileName)
                            .build()
            );

                // 返回成功响应
                return ResponseEntity.ok(downloadUrl);
            } catch(Exception e){
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("处理PPT模板时发生错误");
            }

    }

        private PictureData.PictureType getPictureType(String extension){
        switch (extension){
            case "jpeg":
            case "jpg":
                return PictureData.PictureType.JPEG;
            case "png":
                return PictureData.PictureType.PNG;
            default:
                throw new IllegalArgumentException("Unsupported picture type: " + extension);
        }
    }

    private byte[] convertWebPToJPEG(byte[] webpData) throws IOException {
        //BufferedImage image = ImageIO.read(new ByteArrayInputStream(webpData));
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(webpData));
        if (image == null) {
            throw new IOException("无法读取图片数据，可能是不支持的图片格式");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image,"jpeg",baos);
        return baos.toByteArray();
    }
}