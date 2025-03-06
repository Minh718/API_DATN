package com.shop.fashion.utils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class DateTimeUtil {
    public static LocalDateTime getCurrentVietnamTime() {
        try {
            ZonedDateTime vietnamTime = ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));
            return vietnamTime.toLocalDateTime();
        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return null; // Handle error appropriately in your application
        }
    }
}