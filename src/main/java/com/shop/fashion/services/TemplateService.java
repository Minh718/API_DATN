package com.shop.fashion.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

/**
 * Template
 */
@Component
@RequiredArgsConstructor
public class TemplateService {
    private final ResourceLoader resourceLoader;
    @Value("${frontend.host}")
    @NonFinal
    private String frontentHost;
    @Value("${backend.host}")
    @NonFinal
    private String backendHost;

    public String getTemplateEmail(String uuid, String email) throws IOException {
        Resource cpr = resourceLoader.getResource("classpath:templates/email-template.html");
        String template = new String(FileCopyUtils.copyToByteArray(cpr.getInputStream()),
                StandardCharsets.UTF_8);
        Map<String, String> valuesMap = Map.of(
                "Tên người dùng", email,
                "Tên Công Ty", "Công ty ABC",
                "Link xác nhận", backendHost + "/api/auth/email/confirm?token=" + uuid,
                "Năm hiện tại", String.valueOf(java.time.Year.now()));

        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        String emailContent = sub.replace(template);

        return emailContent;
    }
}