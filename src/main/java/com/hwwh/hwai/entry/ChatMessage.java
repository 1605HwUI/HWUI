package com.hwwh.hwai.entry;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    //消息角色
    private String role;
    //消息内容
    private String content;

}
