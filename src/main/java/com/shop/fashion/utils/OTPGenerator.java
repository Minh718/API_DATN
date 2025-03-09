package com.shop.fashion.utils;

import java.security.SecureRandom;

public class OTPGenerator {
    private static final String NUMERIC_STRING = "0123456789";
    private static final SecureRandom random = new SecureRandom();

    public static String generateNumericOTP(int length) {
        StringBuilder otp = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            otp.append(NUMERIC_STRING.charAt(random.nextInt(NUMERIC_STRING.length())));
        }
        return otp.toString();
    }
}
