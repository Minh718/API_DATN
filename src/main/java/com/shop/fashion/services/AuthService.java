package com.shop.fashion.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shop.fashion.constants.RoleUser;
import com.shop.fashion.dtos.EmailDetailDto;
import com.shop.fashion.dtos.dtosReq.UserSignin;
import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.Role;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.UserMapper;
import com.shop.fashion.repositories.RoleRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.utils.KeyGenerator;

import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RedisService redisService;
    private final TemplateService templateService;
    private final MailService mailService;
    private final JwtService jwtService;
    private final RoleRepository roleRepository;

    @NonFinal
    @Value("${security.jwt.expiration-time-access}")
    long expAccessToken;

    @NonFinal
    @Value("${security.jwt.expiration-time-refresh}")
    long expRefreshToken;

    @NonFinal
    @Value("${security.jwt.refresh-key}")
    String refreshKey;

    public void signUpUserByEmail(UserSignupByEmail userSignupByEmail) throws IOException {
        // Check if user already exists
        if (userRepository.findByEmail(userSignupByEmail.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.USER_EXISTED);
        }

        // Generate unique identifier
        final String uuid = java.util.UUID.randomUUID().toString();

        // Store email and password temporarily in Redis with a TTL of 5 minutes
        redisService.setKeyinMinutes(uuid + ":email", userSignupByEmail.getEmail(), 5);
        redisService.setKeyinMinutes(uuid + ":password", userSignupByEmail.getPassword(), 5);

        // Prepare email details
        EmailDetailDto emailDetailDto = new EmailDetailDto();
        emailDetailDto.setRecipient(userSignupByEmail.getEmail());
        emailDetailDto.setEmailSubject("Xác nhận đăng ký");
        emailDetailDto.setEmailBody(templateService.getTemplateEmail(uuid, userSignupByEmail.getEmail()));

        // Send email
        mailService.sendSimpleMail(emailDetailDto);
    }

    public void completeSignupEmail(String token) {
        Boolean isExist = redisService.checkKeyExist(token + ":email");
        if (isExist.booleanValue()) {
            String email = (String) redisService.getKey(token + ":email");
            String password = (String) redisService.getKey(token + ":password");
            redisService.delKey(token + ":email");
            redisService.delKey(token + ":password");
            PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
            String passwordHash = passwordEncoder.encode(password);
            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById(RoleUser.USER_ROLE).ifPresent(roles::add);
            User user = new User();
            Cart cart = new Cart();
            user.setCart(cart);
            user.setEmail(email);
            user.setPassword(passwordHash);
            user.setRoles(roles);
            user.setCart(cart);
            user.setKeyToken(KeyGenerator.generateRandomKey());
            userRepository.save(user);
            // addChatBoxForUser(user.getId());
            // voucherService.addVouchersToNewUser(user);
        } else
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    }

    public UserInfoToken userLoginbyUsername(UserSignin userLogin) {
        User user = userRepository.findByEmailOrPhone(userLogin.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(userLogin.getPassword(), user.getPassword());
        if (!authenticated)
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        redisService.setKeyInMilliseconds("keyToken:" + user.getId(), user.getKeyToken(), expRefreshToken);
        redisService.addLoyalUser(user.getId());
        return AttachInfoUserWithToken(user);
    }

    private UserInfoToken AttachInfoUserWithToken(User user) {
        UserInfoToken userInfo = UserMapper.INSTANCE.toUserInfoToken(user);
        userInfo.setAccessToken(jwtService.generateToken(user, user.getKeyToken(), expAccessToken));
        userInfo.setRefreshToken(jwtService.generateToken(user, refreshKey, expRefreshToken));
        return userInfo;
    }
}