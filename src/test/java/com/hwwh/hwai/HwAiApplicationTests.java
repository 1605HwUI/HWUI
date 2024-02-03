package com.hwwh.hwai;

import com.alibaba.fastjson.JSONObject;
import com.hwwh.hwai.entry.ChatCompletionChoice;
import com.hwwh.hwai.entry.ChatCompletionRequest;
import com.hwwh.hwai.entry.ChatMessage;
import com.hwwh.hwai.entry.ExecuteRet;
import com.hwwh.hwai.service.OpenAiApi;
import com.hwwh.hwai.service.OpenAiApi1;
import com.hwwh.hwai.service.PathConstant;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.message.Message;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
/*
* 徽文文化有限公司
* SJF*/
@SpringBootTest
class HwAiApplicationTests {
    @Autowired
    private OpenAiApi openAiApi;

    @Autowired
    private OpenAiApi1 openAiApi1;

    List<ChatMessage> messages = new ArrayList<>();


    @Test
    public void createChatCompletion2() throws IOException {
        Scanner in = new Scanner(System.in);
        String input = in.next();
        ChatMessage systemMessage = new ChatMessage("user", input);
        List<ChatMessage> messages = new ArrayList<>();
        //messages.add(systemMessage);
        int i = 0;
        while (!"退出".equals(input)) {
            messages.add(new ChatMessage("user",input));
            ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .user("testing")
                    .max_tokens(500)
                    .temperature(1.0)
                    .build();
            ExecuteRet executeRet = openAiApi1.post(PathConstant.COMPLETIONS.CREATE_CHAT_COMPLETION, JSONObject.toJSONString(chatCompletionRequest),
                    null);
            JSONObject result = JSONObject.parseObject(executeRet.getRespStr());
            List<ChatCompletionChoice> choices = result.getJSONArray("choices").toJavaList(ChatCompletionChoice.class);
            System.out.println(choices.get(0).getMessage().getContent());
            ChatMessage context = new ChatMessage(choices.get(0).getMessage().getRole(), choices.get(0).getMessage().getContent());

            messages.add(context);
            in = new Scanner(System.in);
            input = in.next();
            i++;
        }

    }
}
