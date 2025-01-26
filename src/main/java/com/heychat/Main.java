package com.heychat;

import com.heychat.client.HeyChatWebSocketClient;
import com.heychat.config.DatabaseConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    public static void main(String[] args) {
        try {
            // 初始化数据库
            logger.info("正在初始化数据库...");
            DatabaseConfig.initialize();
            logger.info("数据库初始化完成");
            
            // 创建WebSocket客户端
            HeyChatWebSocketClient client = new HeyChatWebSocketClient("your_token");
            client.connectBlocking();
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                logger.info("正在关闭应用...");
                client.shutdown();
                DatabaseConfig.shutdown();
                logger.info("应用已关闭");
            }));
            
            // 保持程序运行
            while (true) {
                Thread.sleep(1000);
            }
        } catch (Exception e) {
            logger.error("程序运行出错", e);
            System.exit(1);
        }
    }
} 