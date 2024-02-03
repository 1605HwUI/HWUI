package com.hwwh.hwai.controller;

import com.hwwh.hwai.util.MinioService;
import io.minio.MinioClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/*@RestController
@RequestMapping("/file")*/
public class AnalyseFileController {

    private MinioClient minioClient;
    private final String endpoint = "http://192.168.2.21:9000"; // MinIO服务器地址
    private final String accessKey = "SBE109P05XJ3S923LPQW"; // MinIO访问密钥
    private final String secretKey = "83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ"; // MinIO密钥
    private final String bucketName = "libai"; // 存储桶名称
    private final String bucketFileName = "uploadfile"; // 存储桶名称

    @PostMapping("/upload")
    public List<String> uploadFile(@RequestParam("file") MultipartFile file,MinioService minioService) {
        List<String> text = new ArrayList<>();
        try{
            minioService.uploadFile(bucketFileName, file.getOriginalFilename(), file);
            Path pythonInterpreterPath = minioService.downloadFileToTemp(bucketName,"python.exe");// 使用虚拟环境的Python解释器
            Path pythonScriptPath = minioService.downloadFileToTemp(bucketName,"Myenv.py");
            Path audioFilePath = minioService.downloadFileToTemp(bucketFileName,file.getOriginalFilename());
            System.out.println(file.getOriginalFilename());

            ProcessBuilder pb = new ProcessBuilder(pythonInterpreterPath.toString(), pythonScriptPath.toString(), audioFilePath.toString());
            Process p = pb.start();

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8));
            String line;
            while ((line = in.readLine()) != null) {
                text.add(line);
            }
            in.close();
            int exitCode = p.waitFor();
            System.out.println("Python script exited with code " + exitCode);
            Files.deleteIfExists(pythonInterpreterPath);
            Files.deleteIfExists(pythonScriptPath);
            Files.deleteIfExists(audioFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(text.contains("请上传正确格式的音频文件!")){
            text = null;
        }
        return text;
    }
}
