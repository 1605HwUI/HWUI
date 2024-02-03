package com.hwwh.hwai.controller;


import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@RestController
@RequestMapping("/unzipWordToppt")
public class UnzipWordToppt {

    @PostMapping("/unzipWord")
    public String UnZipWord() throws IOException {
        String pptxPath = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\1.docx";
        String extractPath = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\word解压文件";
        //String extractPathXml = "C:\\Users\\Q\\Desktop\\生成的文件汇总\\解压文件\\ppt\\slides";

        unzip(pptxPath, extractPath);

        Path sourceDir = Paths.get("C:\\Users\\Q\\Desktop\\生成的文件汇总\\word解压文件");
        Path zipFile = Paths.get("C:\\Users\\Q\\Desktop\\生成的文件汇总\\压缩生成的ppt文件\\2.pptx");
        zipDirectory(sourceDir,zipFile);
        return sourceDir.getName(0)+zipFile.getName(0).toString();
    }


    // 使用 Java 内置的 Zip 实用程序解压缩 .docx 文件
    public void unzip(String fileZip, String destDir) {
        File dir = new File(destDir);

        // 如果输出目录不存在，则创建输出目录
        if(!dir.exists()) dir.mkdirs();

        byte[] buffer = new byte[1024];

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))){
            ZipEntry zipEntry = zis.getNextEntry();

            while(zipEntry != null){
                File newFile = newFile(dir, zipEntry);
                if(zipEntry.isDirectory()){
                    newFile.mkdirs();
                }else{
                    newFile.getParentFile().mkdirs();
                    try(FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }

            zis.closeEntry();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用 Java 内置的 Zip 实用程序重新压缩文件夹
    public void zipDirectory(Path sourceDir, Path zipFile) throws IOException {
        try (ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile.toFile()))) {
            Path pp = Paths.get(sourceDir.toString());
            Files.walk(pp)
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry zipEntry = new ZipEntry(pp.relativize(path).toString());
                        try {
                            zipStream.putNextEntry(zipEntry);
                            Files.copy(path, zipStream);
                            zipStream.closeEntry();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        }
    }

    // Function to prevent Zip Slip Vulnerability
    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }
}
