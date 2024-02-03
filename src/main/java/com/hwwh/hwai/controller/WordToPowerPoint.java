package com.hwwh.hwai.controller;

import com.hwwh.hwai.service.WordToPowerPointService;
import io.minio.*;
import io.minio.credentials.Provider;
import io.minio.credentials.StaticProvider;
import io.minio.errors.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/convert")
public class WordToPowerPoint {

    @Autowired
    private WordToPowerPointService wordToPowerPointService;

    private final MinioClient minioClient;
    private final String endpoint = "http://192.168.2.21:9000"; // MinIO服务器地址
    private final String accessKey = "SBE109P05XJ3S923LPQW"; // MinIO访问密钥
    private final String secretKey = "83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ"; // MinIO密钥
    private final String bucketName = "libai"; // 存储桶名称
    private final String bucketFileName = "uploadfile"; // 存储桶名称

    //初始化MinIO
    public WordToPowerPoint() {
        Provider credentialProvider = new StaticProvider(accessKey, secretKey, null);
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentialsProvider(credentialProvider)
                .build();
    }


    @PostMapping("/convertWordToPPT")
    public ResponseEntity<Resource> ConverWordToPPT(@RequestParam MultipartFile wordFile) throws MinioException, IOException, NoSuchAlgorithmException, InvalidKeyException {
        wordToPowerPointService.word2pdf(wordFile,minioClient,bucketFileName);
        return wordToPowerPointService.pdf2ppt(wordFile.getOriginalFilename().split("\\.")[0]+".pdf",minioClient,bucketFileName);

    }

}
