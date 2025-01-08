package com.shop.fashion.services;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.entities.User;
import com.shop.fashion.repositories.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public void SignUpUserByEmail(UserSignupByEmail userSignupByEmail) throws IOException {
        Optional<User> user = userRepository.findByEmail(userSignupByEmail.getEmail())
                .ifPresent(() -> new CustomException(ErrorCode.USER_EXISTED));
        ;
        if (user.isPresent())
            throw new CustomException(ErrorCode.USER_EXISTED);

        String uuid = java.util.UUID.randomUUID().toString();

        redisService.setKeyinMinutes(uuid + ":email", userSignupByEmail.getEmail(), 5);
        redisService.setKeyinMinutes(uuid + ":password", userSignupByEmail.getPassword(), 5);
        EmailDetailsDto emailDetailsDto = new EmailDetailsDto();
        emailDetailsDto.setRecipient(userSignupByEmail.getEmail());
        emailDetailsDto.setEmailSubject("xác nhận đăng ký");
        emailDetailsDto
                .setEmailBody(template.getTemplateEmail(uuid, userSignupByEmail.getEmail()));
        mailService.sendSimpleMail(emailDetailsDto);
    }

}