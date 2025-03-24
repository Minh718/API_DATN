package com.shop.fashion.dtos.dtosReq;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserSendMess {
    @NotNull(message = "Message is required")
    private String message;
}
