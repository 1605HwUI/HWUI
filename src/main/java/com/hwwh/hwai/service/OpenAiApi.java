package com.hwwh.hwai.service;

import com.hwwh.hwai.entry.ExecuteRet;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.httpclient.HttpClient;
//import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

//@RestController
@Slf4j
@Component
public class OpenAiApi {
    @Value("${open.ai.url}")
    private String url;
    @Value("${open.ai.token}")
    private String token;

    private CloseableHttpClient httpClient;

    private static final MultiThreadedHttpConnectionManager CONNECTION_MANAGER= new MultiThreadedHttpConnectionManager();

    static {
        // 默认单个host最大链接数
        CONNECTION_MANAGER.getParams().setDefaultMaxConnectionsPerHost(
                Integer.valueOf(20));
        // 最大总连接数，默认20
        CONNECTION_MANAGER.getParams()
                .setMaxTotalConnections(20);
        // 连接超时时间
        CONNECTION_MANAGER.getParams()
                .setConnectionTimeout(60000);
        // 读取超时时间
        CONNECTION_MANAGER.getParams().setSoTimeout(60000);
    }

    public ExecuteRet get(String path, Map<String, String> headers) throws IOException {
        GetMethod method = new GetMethod(url +path);
        if (headers== null) {
            headers = new HashMap<>();
        }
        headers.put("Authorization", "Bearer " + token);
        for (Map.Entry<String, String> h : headers.entrySet()) {
            method.setRequestHeader(h.getKey(), h.getValue());
        }
        return execute(method);
    }

    public ExecuteRet post(String path, String json, Map<String, String> headers) throws IOException {
        try {
            //log.info("POST Url is {} ", url + path);
            PostMethod method = new PostMethod(url +path);
            log.info(method.getURI().toString());

            // 输出传入参数
            log.info(String.format("POST JSON HttpMethod's Params = %s",json));
            StringRequestEntity entity = new StringRequestEntity(json, "application/json", "UTF-8");
            method.setRequestEntity(entity);
            if (headers== null) {
                headers = new HashMap<>();
            }
            headers.put("Authorization", "Bearer " + token);
            for (Map.Entry<String, String> h : headers.entrySet()) {
                method.setRequestHeader(h.getKey(), h.getValue());
            }
            return execute(method);
        } catch (UnsupportedEncodingException ex) {
            log.error(ex.getMessage(),ex);
        }
        return new ExecuteRet(false, "", null, -1);
    }

    public ExecuteRet execute(HttpMethod method) throws IOException {
        httpClient = HttpClients.createDefault();
        //设置代理主机名和端口号
        String proxyHost = "127.0.0.1";
        int proxyPort = 7890;
        //创建HttpHost对象，指定代理主机名和端口号
        HttpHost proxy = new HttpHost(proxyHost,proxyPort);
        //创建RequestConfig对象，并设置代理
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        //创建HttpClientBuilder对象
        HttpClientBuilder builder = HttpClients.custom();
        //使用RequestConfig配置HttpClientBuilder对象
        builder.setDefaultRequestConfig(config);
        //创建CloseableHttpClient对象
        //CloseableHttpClient httpClient = builder.build();
        //CloseableHttpClient httpClient = HttpClients.custom().setDefaultRequestConfig(config).build();
        HttpClient client = new HttpClient(CONNECTION_MANAGER);
        int statusCode = -1;
        String respStr = null;
        boolean isSuccess = false;
        try {
            client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF8");
            log.info(String.valueOf(method.getDoAuthentication()));
            statusCode = client.executeMethod(method);
            //statusCode = httpClient.execute((HttpUriRequest)method).getStatusLine().getStatusCode();
            method.getRequestHeaders();

            // log.info("执行结果statusCode = " + statusCode);
            InputStreamReader inputStreamReader = new InputStreamReader(method.getResponseBodyAsStream(), "UTF-8");
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder stringBuffer = new StringBuilder(100);
            String str;
            while ((str = reader.readLine()) != null) {
                log.debug("逐行读取String = " + str);
                stringBuffer.append(str.trim());
            }
            respStr = stringBuffer.toString();
            if (respStr != null) {
                log.info(String.format("执行结果String = %s, Length = %d", respStr, respStr.length()));
            }
            inputStreamReader.close();
            reader.close();
            // 返回200，接口调用成功
            isSuccess = (statusCode == HttpStatus.SC_OK);
        } catch (IOException ex) {
            ex.printStackTrace();
            log.error("IOException occurred: " + ex.getMessage());
        } finally {
            method.releaseConnection();
        }
        httpClient.close();
        return new ExecuteRet(isSuccess, respStr,method, statusCode);
    }
}
