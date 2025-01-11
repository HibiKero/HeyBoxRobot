package com.heychat.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SendMessageRequest {
    private String msg;
    @JsonProperty("msg_type")
    private String msgType;
    @JsonProperty("channel_id")
    private String channelId;
    @JsonProperty("room_id")
    private String roomId;

    public SendMessageRequest(String msg, String channelId, String roomId) {
        this.msg = msg;
        this.msgType = "MDTEXT";  // 默认使用 MDTEXT 类型
        this.channelId = channelId;
        this.roomId = roomId;
    }

    // getters and setters
    public String getMsg() { return msg; }
    public void setMsg(String msg) { this.msg = msg; }
    public String getMsgType() { return msgType; }
    public void setMsgType(String msgType) { this.msgType = msgType; }
    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
} 