package com.shop.fashion.dtos.dtosReq;

public class AdminSendMess {
    private Long chatBoxId;
    private String message;

    public Long getChatBoxId() {
        return chatBoxId;
    }

    public void setChatBoxId(Long chatBoxId) {
        this.chatBoxId = chatBoxId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
