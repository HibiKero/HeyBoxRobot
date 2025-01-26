package com.heychat.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heychat.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AdminCommandService {
    private static final Logger logger = LoggerFactory.getLogger(AdminCommandService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // 角色ID常量
    private static final String ADMIN_ROLE_ID = "你的管理员角色ID"; // 需要替换为实际的角色ID
    
    /**
     * 检查用户是否是管理员
     */
    public boolean isAdmin(String userId, String roomId) {
        // TODO: 实现管理员检查逻辑
        // 可以通过API获取用户角色列表，检查是否包含管理员角色
        return true; // 临时返回true，需要实现实际的检查逻辑
    }
    
    /**
     * 授予用户管理员权限
     */
    public void grantAdmin(String operatorId, String targetUserId, String roomId, String channelId) {
        if (!isAdmin(operatorId, roomId)) {
            sendMessage(channelId, roomId, "你没有权限执行此操作");
            return;
        }
        
        try {
            String url = "/chatroom/v2/room_role/grant";
            String requestBody = objectMapper.createObjectNode()
                .put("to_user_id", Long.parseLong(targetUserId))
                .put("role_id", ADMIN_ROLE_ID)
                .put("room_id", roomId)
                .toString();
                
            String response = HttpUtil.postJson(url, requestBody);
            JsonNode responseJson = objectMapper.readTree(response);
            
            if ("ok".equals(responseJson.get("status").asText())) {
                sendMessage(channelId, roomId, String.format("已成功将用户 %s 设为管理员", targetUserId));
            } else {
                sendMessage(channelId, roomId, "设置管理员失败：" + responseJson.get("msg").asText());
            }
        } catch (Exception e) {
            logger.error("授予管理员权限时出错", e);
            sendMessage(channelId, roomId, "设置管理员失败：" + e.getMessage());
        }
    }
    
    /**
     * 撤销用户管理员权限
     */
    public void revokeAdmin(String operatorId, String targetUserId, String roomId, String channelId) {
        if (!isAdmin(operatorId, roomId)) {
            sendMessage(channelId, roomId, "你没有权限执行此操作");
            return;
        }
        
        try {
            String url = "/chatroom/v2/room_role/revoke";
            String requestBody = objectMapper.createObjectNode()
                .put("to_user_id", Long.parseLong(targetUserId))
                .put("role_id", ADMIN_ROLE_ID)
                .put("room_id", roomId)
                .toString();
                
            String response = HttpUtil.postJson(url, requestBody);
            JsonNode responseJson = objectMapper.readTree(response);
            
            if ("ok".equals(responseJson.get("status").asText())) {
                sendMessage(channelId, roomId, String.format("已成功撤销用户 %s 的管理员权限", targetUserId));
            } else {
                sendMessage(channelId, roomId, "撤销管理员权限失败：" + responseJson.get("msg").asText());
            }
        } catch (Exception e) {
            logger.error("撤销管理员权限时出错", e);
            sendMessage(channelId, roomId, "撤销管理员权限失败：" + e.getMessage());
        }
    }
    
    /**
     * 发送消息
     */
    private void sendMessage(String channelId, String roomId, String message) {
        try {
            HttpUtil.sendMessage(channelId, roomId, message);
        } catch (Exception e) {
            logger.error("发送消息失败", e);
        }
    }
} 