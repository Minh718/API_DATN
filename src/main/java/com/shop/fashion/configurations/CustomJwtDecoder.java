package com.shop.fashion.configurations;

import javax.crypto.spec.SecretKeySpec;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.services.JwtService;
import com.shop.fashion.services.RedisService;

import io.jsonwebtoken.io.Decoders;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomJwtDecoder implements JwtDecoder {
    private final RedisService redisService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    public Jwt decode(String token) throws JwtException {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new CustomException(ErrorCode.INVALID_REQUEST);
        }
        HttpServletRequest request = attributes.getRequest();

        String idUser = request.getHeader("x-user-id"); // connect for website

        if (idUser == null || idUser.isEmpty()) {
            idUser = jwtService.extractId(token); // connect swagger
        }
        String keyToken = null;
        if (redisService.checkKeyExist("keyToken:" + idUser)) {
            keyToken = (String) redisService.getKey("keyToken:" + idUser);
        } else {
            User user = userRepository.findById(idUser)
                    .orElseThrow(() -> new CustomException(ErrorCode.INVALID_TOKEN));
            keyToken = user.getKeyToken();
            redisService.setKey("keyToken:" + idUser, keyToken);
        }
        byte[] keyBytes = Decoders.BASE64.decode(keyToken);
        SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, "HS256");
        NimbusJwtDecoder nimbusJwtDecoder = NimbusJwtDecoder.withSecretKey(secretKeySpec)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        return nimbusJwtDecoder.decode(token);
    }
}
