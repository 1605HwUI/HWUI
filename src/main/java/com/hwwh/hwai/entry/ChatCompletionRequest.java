package com.hwwh.hwai.entry;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ChatCompletionRequest {
    String model;

    List<ChatMessage> messages;
    //String message;

    Double temperature;

    Integer n;

    Boolean stream;

    List<String> stop;

    Integer max_tokens;

    String user;
}
