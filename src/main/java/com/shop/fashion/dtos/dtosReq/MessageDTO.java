package com.shop.fashion.dtos.dtosReq;

public class MessageDTO {

    private String text;

    private String to;

    // No-arg constructor (Jackson needs this)
    public MessageDTO() {
    }

    // // Add a constructor if needed
    // public MessageDTO(String text, String to) {
    // this.text = text;
    // this.to = to;
    // }

    public MessageDTO(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}