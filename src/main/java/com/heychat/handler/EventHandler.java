package com.heychat.handler;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.heychat.util.HttpUtil;
import com.heychat.service.SignInService;
import com.heychat.model.SignInRecord;

public class EventHandler {
    private static final Logger logger = LoggerFactory.getLogger(EventHandler.class);
    private static final String MSG_TYPE_USECOMMAND = "USE_COMMAND";
    private final SignInService signInService;

    public EventHandler() {
        this.signInService = new SignInService();
    }

    public void handleMessage(JsonNode data) {
        if (!data.has("type")) {
            logger.warn("收到的消息没有type字段");
            return;
        }

        logger.info("收到消息: {}", data.toString());
        String type = data.get("type").asText();
        JsonNode messageData = data.get("data");

        logger.info("消息类型: {}", type);
        if (messageData != null) {
            logger.info("消息数据: {}", messageData.toString());
        }

        // 处理消息类型
        if ("5".equals(type) && messageData != null) {
            // 检查是否是签到命令
            if ("/sign".equals(messageData.get("msg").asText())) {
                logger.info("收到签到命令，消息数据: {}", messageData.toString());
                String channelId = messageData.get("channel_id").asText();
                String roomId = messageData.get("room_id").asText();
                
                // 直接从消息数据中获取用户信息
                if (messageData.has("user_id") && messageData.has("nickname")) {
                    String userId = messageData.get("user_id").asText();
                    String nickname = messageData.get("nickname").asText();
                    
                    // 处理签到
                    SignInRecord record = signInService.signIn(userId, nickname);
                    
                    // 生成签到消息
                    String message = signInService.generateSignInMessage(record);
                    
                    // 发送消息
                    HttpUtil.sendMessage(channelId, roomId, message);
                    logger.info("签到消息已发送");
                } else {
                    logger.error("消息中缺少用户ID或昵称");
                    HttpUtil.sendMessage(channelId, roomId, "签到失败：无法获取用户信息");
                }
            } else {
                // 其他类型的消息
                handleHeartbeat();
            }
        } else if (MSG_TYPE_USECOMMAND.equals(type)) {
            handleCommand(messageData);
        } else {
            logger.info("未处理的消息类型: {}", type);
        }
    }

    private void handleCommand(JsonNode data) {
        try {
            logger.info("开始处理命令: {}", data.toString());
            
            if (!data.has("command_info")) {
                logger.warn("命令数据中没有command_info字段");
                return;
            }

            JsonNode commandInfo = data.get("command_info");
            String commandId = commandInfo.get("id").asText();
            logger.info("命令ID: {}", commandId);

            // 获取用户信息
            if (!data.has("user_info")) {
                logger.warn("命令数据中没有user_info字段");
                return;
            }

            JsonNode userInfo = data.get("user_info");
            String userId = userInfo.get("user_id").asText();
            String nickname = userInfo.get("nickname").asText();
            logger.info("用户ID: {}, 昵称: {}", userId, nickname);

            if (data.has("channel_base_info") && data.has("room_base_info")) {
                String channelId = data.get("channel_base_info").get("channel_id").asText();
                String roomId = data.get("room_base_info").get("room_id").asText();
                
                logger.info("频道ID: {}, 房间ID: {}", channelId, roomId);
                
                // 处理签到命令
                if (commandId.equals("1877275511199731712")) {
                    logger.info("收到签到命令，准备处理");
                    
                    // 处理签到
                    SignInRecord record = signInService.signIn(userId, nickname);
                    
                    // 生成签到消息
                    String message = signInService.generateSignInMessage(record);
                    
                    // 发送消息
                    HttpUtil.sendMessage(channelId, roomId, message);
                    logger.info("签到消息已发送");
                    } else {
                    logger.info("未知的命令ID: {}", commandId);
                }
            } else {
                logger.warn("命令数据缺少channel_base_info或room_base_info字段");
            }
        } catch (Exception e) {
            logger.error("处理命令时出错: {}", e.getMessage(), e);
        }
    }

    private void handleHeartbeat() {
        logger.debug("收到心跳响应");
    }
} 