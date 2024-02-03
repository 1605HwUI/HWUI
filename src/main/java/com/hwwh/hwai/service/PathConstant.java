package com.hwwh.hwai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PathConstant {
    public static class MODEL {
        //获取模型列表
        public static String MODEL_LIST = "/v1/models";
    }

    public static class COMPLETIONS {
        public static String CREATE_COMPLETION = "/v1/completions";
        //创建对话
        public static String CREATE_CHAT_COMPLETION = "/v1/chat/completions";
        /*public static String CREATE_CHAT_COMPLETION = "/v1/engines/gpt-4/completions";*/

    }
}
