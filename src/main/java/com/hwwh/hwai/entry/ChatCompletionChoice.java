package com.hwwh.hwai.entry;

import lombok.Data;

//接收chatgpt返回的数据
@Data
public class ChatCompletionChoice {
    Integer index;

    ChatMessage message;

    String finishReason;
}
