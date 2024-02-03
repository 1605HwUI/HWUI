package com.hwwh.hwai.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVo extends ChatMessage{
    /*List<ChatMessage> chatMessages = new ArrayList<>();*/
    //消息角色
    String role;
    //消息内容
    String content;
    //文件内容
    MultipartFile file;
}
