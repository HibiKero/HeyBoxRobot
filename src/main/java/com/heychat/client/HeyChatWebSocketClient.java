package com.heychat.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.heychat.config.Config;
import com.heychat.handler.EventHandler;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.Map;

public class HeyChatWebSocketClient extends WebSocketClient {
    private static final Logger logger = LoggerFactory.getLogger(HeyChatWebSocketClient.class);
    private final EventHandler eventHandler;
    private ScheduledExecutorService heartbeatExecutor;
    private final String token;
    
    public HeyChatWebSocketClient(String token) throws URISyntaxException {
        super(new URI(Config.getWssUrl()));
        this.token = token;
        this.eventHandler = new EventHandler();
        setupHeartbeat();
    }
    
    private void setupHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleAtFixedRate(this::sendPing, 0, 30, TimeUnit.SECONDS);
    }
    
    @Override
    public void onOpen(ServerHandshake handshake) {
        logger.info("Connected to server");
    }
    
    @Override
    public void onMessage(String message) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(message);
            eventHandler.handleMessage(data);
        } catch (Exception e) {
            logger.error("Error processing message", e);
        }
    }
    
    @Override
    public void onClose(int code, String reason, boolean remote) {
        logger.info("Connection closed: " + reason);
        new Thread(this::reconnect).start();
    }
    
    @Override
    public void onError(Exception ex) {
        logger.error("WebSocket error", ex);
    }
    
    public void sendMessage(String message) {
        if (message.equals("/sign")) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                String signMessage = mapper.writeValueAsString(new SignCommand());
                send(signMessage);
                logger.info("签到命令已发送");
            } catch (Exception e) {
                logger.error("发送签到命令失败", e);
            }
        } else {
            send(message);
        }
    }
    
    public void reconnect() {
        try {
            // 创建新的客户端实例
            HeyChatWebSocketClient newClient = new HeyChatWebSocketClient(token);
            newClient.connect();
            logger.info("Created new connection");
        } catch (Exception e) {
            logger.error("Failed to create new connection", e);
            // 等待一段时间后重试
            try {
                Thread.sleep(5000);
                new Thread(this::reconnect).start();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    
    public void shutdown() {
        if (heartbeatExecutor != null) {
            heartbeatExecutor.shutdown();
        }
        try {
            close();
        } catch (Exception e) {
            logger.error("Error during shutdown", e);
        }
    }
} 