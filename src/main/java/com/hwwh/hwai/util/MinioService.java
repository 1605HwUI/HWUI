package com.hwwh.hwai.util;

import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import io.minio.http.Method;
import lombok.var;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@ComponentScan
public class MinioService {
    private MinioClient minioClient;

    public String getPresignedObjectUrl(String bucketName, String objectName) throws Exception {
        try{
            this.minioClient = MinioClient.builder()
                    .endpoint("http://192.168.2.21:9000")
                    .credentials("SBE109P05XJ3S923LPQW","83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ")
                    .build();
        }catch (Exception e){
            e.printStackTrace();
        }
        return this.minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucketName)
                        .object(objectName)
                        .expiry(3600) // 设置 URL 的有效期限
                        .build());
    }

    public void uploadFile(String bucketName, String objectName, MultipartFile file) throws IOException, MinioException {
        // 将 MultipartFile 转换为 InputStream
        try{
            this.minioClient = MinioClient.builder()
                    .endpoint("http://192.168.2.21:9000")
                    .credentials("SBE109P05XJ3S923LPQW","83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ")
                    .build();
        }catch (Exception e){
            e.printStackTrace();
        }
        try (var inputStream = file.getInputStream()) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }

    public Path downloadFileToTemp(String bucketName, String objectName) throws Exception {

        Path tempFilePath = Files.createTempFile("temp-", objectName);
        try{
            this.minioClient = MinioClient.builder()
                    .endpoint("http://192.168.2.21:9000")
                    .credentials("SBE109P05XJ3S923LPQW","83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ")
                    .build();
        }catch (Exception e){
            e.printStackTrace();
        }
        try (InputStream is = minioClient.getObject(GetObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build());
             OutputStream outputStream = Files.newOutputStream(tempFilePath)) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1){
                outputStream.write(buffer,0,bytesRead);
            }
        }
        return tempFilePath;
    }
}
