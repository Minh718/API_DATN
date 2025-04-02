package com.shop.fashion.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class RamdomKeyUtil {
    public static String generateRandomKey() {
        SecureRandom secureRandom = new SecureRandom();

        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String base64String = Base64.getEncoder().encodeToString(randomBytes);

        return base64String;
    }
}
