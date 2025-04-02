package com.shop.fashion.utils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashValueUtil {

    public static String hashString(String input) {
        try {
            // Tạo một instance của MessageDigest với thuật toán SHA-256
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            // Chuyển đổi chuỗi đầu vào thành mảng byte và thực hiện hashing
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // Chuyển đổi mảng byte thành một giá trị dạng chuỗi hex
            BigInteger number = new BigInteger(1, hashBytes);
            StringBuilder hexString = new StringBuilder(number.toString(16));

            // Đảm bảo chuỗi hex có đủ 32 ký tự (vì SHA-256 có độ dài 256 bit)
            while (hexString.length() < 64) {
                hexString.insert(0, '0');
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}