package com.hwwh.hwai.service;

import com.aspose.pdf.Document;
import com.aspose.pdf.SaveFormat;
import com.documents4j.api.DocumentType;
import com.documents4j.api.IConverter;
import com.documents4j.job.LocalConverter;
import io.minio.*;
import io.minio.errors.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class WordToPowerPointService {

    public static void word2pdf(MultipartFile wordFile, MinioClient miniClient, String bucketFileName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        //long old = System.currentTimeMillis();
        InputStream inputStream = wordFile.getInputStream();
        byte[] pdfBytes = null;
        String[] split = wordFile.getOriginalFilename().split("\\.");
        String objectName = split[0]+".pdf";
        try {
            IConverter converter = LocalConverter.builder().build();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            boolean execute = converter.convert(inputStream)
                    .as(DocumentType.DOCX)
                    .to(byteArrayOutputStream)
                    .as(DocumentType.PDF)
                    .schedule()
                    .get();
            pdfBytes = byteArrayOutputStream.toByteArray();
            byteArrayOutputStream.close();
            inputStream.close();

            //log.info("转换完毕 targetPath = {}", outputFile.getAbsolutePath());
            if (execute == true) {
                System.out.println("转换完毕 targetPath = " + pdfBytes);
                converter.shutDown();
            } else {
                System.out.println("word转pdf失败...");
            }
        } catch (Exception e) {
            //log.error("[documents4J] word转pdf失败:{}", e.toString());
            System.out.println("word转pdf失败...");
        }

        boolean bucketExists = miniClient.bucketExists(BucketExistsArgs.builder().bucket(bucketFileName).build());
        if (!bucketExists) {
            miniClient.makeBucket(MakeBucketArgs.builder().bucket(bucketFileName).build());
        }
        miniClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketFileName)
                        .object(objectName)
                        .stream(new ByteArrayInputStream(pdfBytes),pdfBytes.length,-1)
                        .contentType("application/pdf")
                        .build());

    }

    public ResponseEntity<Resource> pdf2ppt(String objectName, MinioClient minioClient, String bucketFileName) throws MinioException, IOException, NoSuchAlgorithmException {
        long old = System.currentTimeMillis();
        //String originalFilename = pdfPath.getOriginalFilename();
        File pdfFile = downloadPdfFromMinIO(objectName,minioClient,bucketFileName);
        String pdfFilePath = pdfFile.getAbsolutePath();
        try {
            ByteArrayOutputStream pptOutPutStream = new ByteArrayOutputStream();
            //新建一个PPT文档
            String PPTPath = pdfFilePath.substring(0, pdfFilePath.lastIndexOf(".")) + ".pptx";
            FileOutputStream os = new FileOutputStream(PPTPath);
            //doc是将要被转化的word文档
            Document pdfDoc = new Document(pdfFilePath);
            //全面支持DOC, DOCX, OOXML, RTF HTML, OpenDocument, PDF, EPUB, XPS, SWF 相互转换
            pdfDoc.save(os, SaveFormat.Pptx);
            os.close();
            /*File pptFile = File.createTempFile("converted",".pptx");
            try(FileOutputStream pptFileOutputStream = new FileOutputStream(pptFile)) {
                pptFileOutputStream.write(pptOutPutStream.toByteArray());
            }*/
            File pptFile = new File(PPTPath);
            FileInputStream pptFileInputStream = new FileInputStream(pptFile);
            byte[] pptBytes = new byte[(int) pptFile.length()];
            pptFileInputStream.read(pptBytes);
            pptFileInputStream.close();
            //删除临时下载的文件
            pdfFile.delete();
            //转化用时
            long now = System.currentTimeMillis();
            System.out.println("Pdf 转 PPT 共耗时：" + ((now - old) / 1000.0) + "秒");
            ByteArrayResource resource = new ByteArrayResource(pptBytes);
            return ResponseEntity.ok().body(resource);
        } catch (Exception e) {
            System.out.println("Pdf 转 PPT 失败...");
            e.printStackTrace();
            return null;
        }
    }
    private static File downloadPdfFromMinIO(String objectName,MinioClient minioClient,String bucketFileName) throws IOException, MinioException, NoSuchAlgorithmException {
        try {

            // Check if the bucket exists
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketFileName).build());
            if (!bucketExists) {
                throw new MinioException("MinIO bucket does not exist: " + bucketFileName);
            }

            // Download PDF from MinIO to a local temporary file
            String uniqueId = UUID.randomUUID().toString();
            String fileName = "download" + uniqueId + ".pdf";
            File pdfFile = new File(fileName);
            while(pdfFile.exists()){
                uniqueId = UUID.randomUUID().toString();
                fileName = "download" + uniqueId + ".pdf";
                pdfFile = new File(fileName);
            }
            minioClient.downloadObject(
                    DownloadObjectArgs.builder()
                            .bucket(bucketFileName)
                            .object(objectName)
                            .filename(pdfFile.getAbsolutePath())
                            .build());

            return pdfFile;
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }

    }

}
