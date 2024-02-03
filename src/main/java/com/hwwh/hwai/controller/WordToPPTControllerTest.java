package com.hwwh.hwai.controller;

import com.hwwh.hwai.util.MinioService;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/*@RestController
@RequestMapping("/convert")
@CrossOrigin(origins = "http://localhost:8080")*/
public class WordToPPTControllerTest {
    private final MinioClient minioClient;
    private final String endpoint = "http://192.168.2.21:9000"; // MinIO服务器地址
    private final String accessKey = "SBE109P05XJ3S923LPQW"; // MinIO访问密钥
    private final String secretKey = "83WX6hrNruftj5AoQrvfarKuZabehgb3958ST7cQ"; // MinIO密钥
    private final String bucketName = "libai"; // 存储桶名称
    private final String bucketFileName = "uploadfile"; // 存储桶名称

    //初始化MinIO
    public WordToPPTControllerTest() {
        Provider credentialProvider = new StaticProvider(accessKey, secretKey, null);
        minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentialsProvider(credentialProvider)
                .build();
    }


    @PostMapping("/convertWordToPPT")
    public ResponseEntity<Resource> convertWordToPPT(@RequestParam("wordFile") MultipartFile wordfile, @RequestParam("PPTFile") MultipartFile PPTfile, MinioService minioService){
        try {
            Path pythonEnvPath = minioService.downloadFileToTemp(bucketName, "python.exe");
            Path pythonScriptPath = minioService.downloadFileToTemp(bucketName, "Test6.py");


            // Save the uploaded Word file
            Path wordFilePath = Paths.get(System.getProperty("java.io.tmpdir"), wordfile.getOriginalFilename());
            wordfile.transferTo(wordFilePath.toFile());

            Path pptTemplatePath = null;
            if(PPTfile != null && PPTfile.isEmpty()){
                pptTemplatePath = Paths.get(System.getProperty("java.io.tmpdir"), PPTfile.getOriginalFilename());
                PPTfile.transferTo(pptTemplatePath.toFile());
            }

            // Define the path for the output PPT file
            Path pptxFilePath = Paths.get(System.getProperty("java.io.tmpdir"), "converted_presentation.pptx");

            // Set up and execute the Python script
            String[] cmd = {
                    pythonEnvPath.toString(), // Adjust based on your Python environment path
                    pythonScriptPath.toString(),
                    wordFilePath.toString(),
                    pptxFilePath.toString()
            };

            Process process = Runtime.getRuntime().exec(cmd);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }

            // Wait for the process to complete and check for errors
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new RuntimeException("Conversion failed with exit code " + exitCode);
            }

            // Return the converted PPT file
            Resource fileResource = new FileSystemResource(pptxFilePath.toFile());
            return ResponseEntity.ok()
                    .header("Content-Disposition", "attachment; filename=\"" + fileResource.getFilename() + "\"")
                    .body(fileResource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}

