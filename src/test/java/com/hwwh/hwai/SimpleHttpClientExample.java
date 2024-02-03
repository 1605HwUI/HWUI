package com.hwwh.hwai;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SimpleHttpClientExample {
    public static void main(String[] args) {
        String targetUrl = "https://api.openai.com"; // 替换为您的目标URL
        String proxyHost = "127.0.0.1";
        int proxyPort = 7890;

        // 设置代理
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();

        // 创建HttpClient实例
        try (CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build()) {
            // 创建HttpGet请求
            HttpGet request = new HttpGet(targetUrl);

            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                // 输出响应状态
                System.out.println(response.getStatusLine());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}