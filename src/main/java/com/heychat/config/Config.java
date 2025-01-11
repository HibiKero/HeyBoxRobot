package com.heychat.config;

public class Config {
    private static final String HTTP_HOST = "https://chat.xiaoheihe.cn";
    private static final String SEND_MSG_URL = "/chatroom/v2/channel_msg/send?";
    private static final String COMMON_PARAMS = "chat_os_type=bot&client_type=heybox_chat&chat_version=999.0.0";
    private static final String TOKEN_PARAM = "&token=";
    
    public static String getHttpUrl() {
        return HTTP_HOST + SEND_MSG_URL + COMMON_PARAMS + TOKEN_PARAM + token;
    }
    
    public static String getWssUrl() {
        return "wss://chat.xiaoheihe.cn/chatroom/ws/connect?" + COMMON_PARAMS + TOKEN_PARAM + token;
    }
    
    private static String token = "Nzc3MzI0NjA7MTczNjMyMzQ5NTA4NTY5ODI0NjsxNTQ2NjUyNDYwMDQwMzEwNzQ5";
    
    public static String getToken() {
        return token;
    }
    
    public static void setToken(String newToken) {
        token = newToken;
    }
} 