package com.shop.fashion.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.shop.fashion.dtos.dtosReq.MessageDTO;
import com.shop.fashion.dtos.dtosRes.OrderNotificationPayload;

@Controller
public class SocketController {

    @Autowired
    SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/private")
    public void sendToSpecificUser(@Payload MessageDTO message) {
        simpMessagingTemplate.convertAndSendToUser(message.getTo(), "/specific", message.getText());
    }

    public void notifyOrderSuccess(OrderNotificationPayload payload) {
        simpMessagingTemplate.convertAndSend("/topic/order-notification", payload);
    }

}