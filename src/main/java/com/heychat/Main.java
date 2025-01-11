package com.heychat;

import com.heychat.client.HeyChatWebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            HeyChatWebSocketClient client = new HeyChatWebSocketClient("your_token");
            client.connectBlocking();
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("Shutting down...");
                client.shutdown();
            }));
            
            // 保持程序运行
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("Error in main", e);
        }
    }
} 