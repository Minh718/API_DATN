package com.shop.fashion.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.EmailDetailDto;

import jakarta.mail.internet.MimeMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.NonFinal;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    @NonFinal
    private String sender;

    @Async("ThreadPoolEmail")
    public void sendSimpleMail(EmailDetailDto details) {
        try {

            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper mailMessage = new MimeMessageHelper(mimeMessage, "utf-8");
            mailMessage.setFrom(sender);
            mailMessage.setTo(details.getRecipient());
            mailMessage.setSubject(details.getEmailSubject());
            mailMessage.setText(details.getEmailBody(), true);

            javaMailSender.send(mimeMessage);
        }

        catch (Exception exception) {
            System.out.println("Error occurred while sending mail: " + exception.getMessage());
        }
    }

}