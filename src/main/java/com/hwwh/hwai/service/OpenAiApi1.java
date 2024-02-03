package com.hwwh.hwai.service;


import com.hwwh.hwai.entry.ExecuteRet;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class OpenAiApi1 {
    @Value("${open.ai.url}")
    private String url;

    @Value("${open.ai.token}")
    private String token;

    private static final String PROXY_HOST = "127.0.0.1";
    private static final int PROXY_PORT = 7890;

    private CloseableHttpClient createHttpClient() {
        HttpHost proxy = new HttpHost(PROXY_HOST, PROXY_PORT);
        RequestConfig config = RequestConfig.custom()
                .setProxy(proxy)
                .build();
        return HttpClients.custom()
                .setDefaultRequestConfig(config)
                .build();
    }

    public ExecuteRet get(String path, Map<String, String> headers) throws IOException {
        HttpGet request = new HttpGet(url + path);
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Authorization", "Bearer " + token);
        headers.forEach(request::addHeader);

        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            String respStr = EntityUtils.toString(response.getEntity());
            boolean isSuccess = response.getStatusLine().getStatusCode() == 200;
            return new ExecuteRet(isSuccess, respStr, null, response.getStatusLine().getStatusCode());
        }
    }

    public ExecuteRet post(String path, String json, Map<String, String> headers) throws IOException {
        HttpPost request = new HttpPost(url + path);
        if (headers == null) {
            headers = new HashMap<>();
        }
        headers.put("Authorization", "Bearer " + token);
        headers.forEach(request::addHeader);
        StringEntity entity = new StringEntity(json,StandardCharsets.UTF_8);
        entity.setContentType("application/json");
        request.setEntity(entity);
        //request.setEntity((new StringEntity(json, StandardCharsets.UTF_8)));
        try (CloseableHttpClient httpClient = createHttpClient();
             CloseableHttpResponse response = httpClient.execute(request)) {
            String respStr = EntityUtils.toString(response.getEntity());
            boolean isSuccess = response.getStatusLine().getStatusCode() == 200;
            return new ExecuteRet(isSuccess, respStr, null, response.getStatusLine().getStatusCode());
        }
    }
}
