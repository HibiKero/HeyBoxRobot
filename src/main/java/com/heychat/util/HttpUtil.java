package com.heychat.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heychat.config.Config;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class HttpUtil {
    private static final Logger logger = LoggerFactory.getLogger(HttpUtil.class);
    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    public static void sendMessage(String channelId, String roomId, String message) {
        try {
            String url = Config.getHttpUrl();
            logger.info("准备发送消息到URL: {}", url);
            
            // 构建消息体
            MessageRequest messageRequest = new MessageRequest(message, channelId, roomId);
            String jsonBody = objectMapper.writeValueAsString(messageRequest);
            logger.info("发送的消息内容: {}", jsonBody);
            
            RequestBody body = RequestBody.create(jsonBody, JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .header("token", Config.getToken())
                    .post(body)
                    .build();
            
            logger.info("开始发送HTTP请求");
            try (Response response = client.newCall(request).execute()) {
                String responseBody = response.body().string();
                if (!response.isSuccessful()) {
                    logger.error("发送消息失败: HTTP {} - {}", response.code(), responseBody);
                } else {
                    logger.info("发送消息成功，响应: {}", responseBody);
                }
            }
        } catch (Exception e) {
            logger.error("发送消息时发生错误: {}", e.getMessage(), e);
        }
    }
    
    private static class MessageRequest {
        @JsonProperty("msg")
        public String msg;
        @JsonProperty("msg_type")
        public int msgType = 4;
        @JsonProperty("channel_id")
        public String channelId;
        @JsonProperty("room_id")
        public String roomId;

        public MessageRequest(String msg, String channelId, String roomId) {
            this.msg = msg;
            this.channelId = channelId;
            this.roomId = roomId;
        }
    }
} 