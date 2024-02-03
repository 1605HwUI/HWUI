package com.hwwh.hwai.controller;

import com.alibaba.fastjson.JSONObject;
import com.hwwh.hwai.entry.*;
import com.hwwh.hwai.service.OpenAiApi1;
import com.hwwh.hwai.service.PathConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RestController
@RequestMapping("/ai")
public class AiController {
    @Autowired
    private OpenAiApi1 openAiApi1;

    /*Map<Integer,List<ChatMessage>> map = new TreeMap<>();*/

    List<ChatMessage> messages = new ArrayList<>();

    private void addMessage(String role,String content){
        messages.add(new ChatMessage(role,content));
    }

   /* @PostMapping("/hwwh")
    public List<Character> Hwzs(@RequestBody String text) throws IOException {
        addMessage("user",text);//添加用户消息到历史
            //ChatMessageVo systemMessage = new ChatMessage("user", text,file);
            int i = 0;
            //messages.add(new ChatMessage("user", text));
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
            *//*final String content = choices.get(0).getMessage().getContent();
            ChatMessage context = new ChatMessage(choices.get(0).getMessage().getRole(), choices.get(0).getMessage().getContent());
            messages.add(context);
            i++;*//*
            return formatResultMessage(choices.get(0).getMessage());

}
    private List<Character> formatResultMessage(ChatMessage message){
        String content = message.getContent();
        addMessage(message.getRole(),content);
        List<Character> characterList = new ArrayList<>();
        for (char c : content.toCharArray()) {
            characterList.add(c);
        }
        return characterList;
    }*/

    @PostMapping(value = "/hwwh")
    public ResponseEntity<StreamingResponseBody> Hwzs(@RequestBody String text) throws IOException {
        addMessage("user",text);//添加用户消息到历史
        //ChatMessageVo systemMessage = new ChatMessage("user", text,file);
        int i = 0;
        //messages.add(new ChatMessage("user", text));
        ChatCompletionRequest chatCompletionRequest = ChatCompletionRequest.builder()
                .model("gpt-3.5-turbo")
                .messages(messages)
                .user("testing")
                .max_tokens(3000)
                .temperature(1.0)
                .build();
        ExecuteRet executeRet = openAiApi1.post(PathConstant.COMPLETIONS.CREATE_CHAT_COMPLETION, JSONObject.toJSONString(chatCompletionRequest),
                null);
        JSONObject result = JSONObject.parseObject(executeRet.getRespStr());
        List<ChatCompletionChoice> choices = result.getJSONArray("choices").toJavaList(ChatCompletionChoice.class);
        /*final String content = choices.get(0).getMessage().getContent();
        ChatMessage context = new ChatMessage(choices.get(0).getMessage().getRole(), choices.get(0).getMessage().getContent());
        messages.add(context);
        i++;*/
        List<Character> characterList = formatResultMessage(choices.get(0).getMessage());

        StreamingResponseBody body = outputStream -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            for(Character c : characterList) {
                writer.write(c);

                // 在遇到特定字符时添加换行
                if (c == ';' || c == '{' || c == '}' || c == ',') {
                    writer.newLine();
                }
                writer.flush();
            }
        };
        /*StreamingResponseBody body = outputStream -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
            for(Character c : characterList) {
                writer.write(c);
                writer.flush();
            }
        };*/

        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.TEXT_PLAIN)
                .body(body);
    }

    private List<Character> formatResultMessage(ChatMessage message){
        String content = message.getContent();
        addMessage(message.getRole(),content);
        List<Character> characterList = new ArrayList<>();
        for (char c : content.toCharArray()) {
            characterList.add(c);
        }
        return characterList;
    }
    @PostMapping("/startNewConversation")
    public ResponseEntity<?> startNewConversation(){
        messages.clear();
        return ResponseEntity.ok("新对话已开始");
    }

    /*@PostMapping("/historyConversation")
    public ResponseEntity<?> historyConversation(){
        Integer i = 0;
        ResponseEntity<?> entity = startNewConversation();
        Object body = entity.getBody();
        if(body.equals("新对话已开始")){
            map.put(++i,messages);
        }


        return ResponseEntity.ok(map);
    }*/

}
