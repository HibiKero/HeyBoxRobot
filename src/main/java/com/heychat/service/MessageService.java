package com.heychat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.heychat.config.Config;
import com.heychat.model.SendMessageRequest;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class MessageService {
    private static final Logger logger = LoggerFactory.getLogger(MessageService.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void sendMessage(SendMessageRequest request) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            String jsonBody = mapper.writeValueAsString(request);
            
            HttpPost httpPost = new HttpPost(Config.getHttpUrl());
            httpPost.setHeader("Content-Type", "application/json;charset=UTF-8");
            httpPost.setHeader("token", Config.getToken());
            httpPost.setEntity(new StringEntity(jsonBody, StandardCharsets.UTF_8));

            try (CloseableHttpResponse response = client.execute(httpPost)) {
                String responseBody = EntityUtils.toString(response.getEntity());
                logger.info("消息发送响应: {}", responseBody);
            }
        } catch (Exception e) {
            logger.error("发送消息失败", e);
        }
    }
} 