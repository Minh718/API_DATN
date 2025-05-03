package com.shop.fashion.dtos.dtosRes;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderNotificationPayload {
    private String avatar;
    private String message;
}
