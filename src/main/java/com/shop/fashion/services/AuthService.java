package com.shop.fashion.services;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.shop.fashion.constants.RoleUser;
import com.shop.fashion.dtos.EmailDetailDto;
import com.shop.fashion.dtos.dtosReq.GetTokenGoogleReq;
import com.shop.fashion.dtos.dtosReq.RefreshTokenDTO;
import com.shop.fashion.dtos.dtosReq.UserSignin;
import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.dtos.dtosReq.UserSignupByPhone;
import com.shop.fashion.dtos.dtosRes.GetTokenGoogleRes;
import com.shop.fashion.dtos.dtosRes.OutBoundInfoUser;
import com.shop.fashion.dtos.dtosRes.TokenPair;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.ChatBox;
import com.shop.fashion.entities.Role;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.UserMapper;
import com.shop.fashion.repositories.ChatBoxRepository;
import com.shop.fashion.repositories.RoleRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.httpClient.OutboundIdentityClientGoogle;
import com.shop.fashion.repositories.httpClient.OutboundInfoUserGoogle;
import com.shop.fashion.utils.HashValueUtil;
import com.shop.fashion.utils.KeyGenerator;
import com.shop.fashion.utils.OTPGenerator;
import com.shop.fashion.utils.RamdomKeyUtil;

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
    private final SmsService smsService;
    private final ChatBoxRepository chatBoxRepository;
    private final OutboundInfoUserGoogle outboundInfoUserGoogle;
    private final OutboundIdentityClientGoogle outboundIdentityClientGoogle;
    private final VoucherService voucherService;

    @NonFinal
    @Value("${security.jwt.expiration-time-access}")
    long expAccessToken;

    @NonFinal
    @Value("${security.jwt.expiration-time-refresh}")
    long expRefreshToken;

    @NonFinal
    @Value("${security.jwt.expiration-time-access-admin}")
    long expAccessTokenAdmin;

    @NonFinal
    @Value("${security.jwt.expiration-time-refresh-admin}")
    long expRefreshTokenAdmin;

    @NonFinal
    @Value("${security.jwt.refresh-key-admin}")
    String refreshKeyAdmin;

    @NonFinal
    @Value("${security.jwt.refresh-key}")
    String refreshKey;

    @NonFinal
    @Value("${auth.google.client-id}")
    String CLIENT_ID;

    @NonFinal
    @Value("${frontend.host}")
    String FRONT_END;

    @NonFinal
    @Value("${auth.google.client-secret}")
    String CLIENT_SECRET;

    @NonFinal
    @Value("${auth.google.redirect-uri}")
    String REDIRECT_URI;

    @NonFinal
    String GRANT_TYPE = "authorization_code";

    public void handleUserSignupByEmail(UserSignupByEmail userSignupByEmail) throws IOException {
        // Check if user already exists
        if (userRepository.findByEmail(userSignupByEmail.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.USER_EXISTED);
        }

        // Generate unique identifier
        final String uuid = java.util.UUID.randomUUID().toString();
        System.out.println(uuid);
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

    public void handleGetOtpForPhoneNumber(String phone) {
        userRepository.findByPhone(phone).orElseThrow(() -> new CustomException(ErrorCode.PHONE_REGISTERED));
        Boolean isExistPhone = redisService.checkKeyExist(phone);
        if (isExistPhone.booleanValue()) {
            throw new CustomException(ErrorCode.OTP_IS_SENDING);
        }
        String otp = OTPGenerator.generateNumericOTP(6);
        smsService.sendSms(phone, "Your verification code is" + otp);
        redisService.setKeyinMinutes(phone, otp, 6);
    }

    public void handleUserSignupByPhone(UserSignupByPhone userSignupByPhone) {
        String phone = userSignupByPhone.getPhone();
        String otpFromRedis = (String) redisService.getKey(phone);

        if (otpFromRedis == null) {
            throw new CustomException(ErrorCode.OTP_IS_EXPIRED);
        }

        if (!otpFromRedis.equals(userSignupByPhone.getOtp())) {
            throw new CustomException(ErrorCode.INVALID_OTP);
        }

        User newUser = createNewUser(userSignupByPhone.getPassword(), null, phone);
        userRepository.save(newUser);
        addChatBoxForUser(newUser.getId());
        voucherService.addVouchersToNewUser(newUser);
    }

    public TokenPair completeSignupEmail(String token) {
        Boolean isExist = redisService.checkKeyExist(token + ":email");
        if (isExist.booleanValue()) {
            String email = (String) redisService.getKey(token + ":email");
            String password = (String) redisService.getKey(token + ":password");
            redisService.delKey(token + ":email");
            redisService.delKey(token + ":password");
            User newUser = createNewUser(password, email, null);
            userRepository.save(newUser);
            addChatBoxForUser(newUser.getId());
            voucherService.addVouchersToNewUser(newUser);
            return generateTokens(newUser, false);
        } else
            throw new CustomException(ErrorCode.TOKEN_EXPIRED);
    }

    private User createNewUser(String password, String email, String phone) {
        User user = new User();
        Cart cart = new Cart();
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        String passwordHash = passwordEncoder.encode(password);
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(RoleUser.USER_ROLE).ifPresent(roles::add);
        user.setCart(cart);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPassword(passwordHash);
        user.setRoles(roles);
        user.setCart(cart);
        user.setKeyToken(KeyGenerator.generateRandomKey());
        return user;
    }

    public UserInfoToken userSigninbyUsername(UserSignin userSignin) {
        User user = userRepository.findByEmailOrPhone(userSignin.getUsername())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(userSignin.getPassword(), user.getPassword());
        if (!authenticated)
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        redisService.setKeyInMilliseconds("keyToken:" + user.getId(), user.getKeyToken(), expRefreshToken);
        redisService.addLoyalUser(user.getId());
        return AttachInfoUserWithToken(user);
    }

    public UserInfoToken adminSignin(UserSignin userSignin) {
        User user = userRepository.findByEmailAndRole(userSignin.getUsername(), RoleUser.ADMIN_ROLE)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(10);
        boolean authenticated = passwordEncoder.matches(userSignin.getPassword(), user.getPassword());
        if (!authenticated)
            throw new CustomException(ErrorCode.UNAUTHENTICATED);
        redisService.setKeyInMilliseconds("keyToken:" + user.getId(), user.getKeyToken(), expRefreshToken);
        return AttachInfoUserWithToken(user);
    }

    public UserInfoToken AttachInfoUserWithToken(User user) {
        UserInfoToken userInfo = UserMapper.INSTANCE.toUserInfoToken(user);
        userInfo.setAccessToken(jwtService.generateToken(user, user.getKeyToken(), expAccessToken));
        userInfo.setRefreshToken(jwtService.generateToken(user, refreshKey, expRefreshToken));
        return userInfo;
    }

    public UserInfoToken userSigninByGoogle(String code) {
        GetTokenGoogleReq getTokenGoogleReq = GetTokenGoogleReq.builder()
                .code(code)
                .clientId(CLIENT_ID)
                .clientSecret(CLIENT_SECRET)
                .redirectUri(FRONT_END + REDIRECT_URI)
                .grantType(GRANT_TYPE)
                .build();
        GetTokenGoogleRes getTokenGoogleRes = outboundIdentityClientGoogle.getToken(
                getTokenGoogleReq);
        OutBoundInfoUser infoUser = outboundInfoUserGoogle.getInfoUser("json", getTokenGoogleRes.getAccessToken());
        User user = userRepository.findByidUserGoogle(infoUser.id()).orElseGet(() -> {
            User newUser = User.builder()
                    .idUserGoogle(infoUser.id())
                    .name(infoUser.name())
                    .picture(infoUser.picture())
                    // .typeLogin(TypeLogin.OAUTH2)
                    .isactive(true)
                    .build();
            HashSet<Role> roles = new HashSet<>();
            roleRepository.findById(RoleUser.USER_ROLE).ifPresent(roles::add);
            newUser.setRoles(roles);
            newUser.setKeyToken(KeyGenerator.generateRandomKey());
            Cart cart = new Cart();
            newUser.setCart(cart);
            userRepository.save(newUser);
            addChatBoxForUser(newUser.getId());
            return newUser;
        });
        redisService.setKeyInMilliseconds("keyToken:" + user.getId(), user.getKeyToken(), expRefreshToken);
        redisService.addLoyalUser(user.getId());
        return AttachInfoUserWithToken(user);
    }

    private void addChatBoxForUser(String userId) {
        ChatBox chatBox = new ChatBox();
        // String adminId = userService.getIdAdmin();
        chatBox.setUserId(userId);
        // chatBox.setAdminId(adminId);
        chatBoxRepository.save(chatBox);
    }

    public TokenPair refreshTokenUser(RefreshTokenDTO refreshTokenDTO) {
        return refreshToken(refreshTokenDTO, refreshKey, false);
    }

    private TokenPair refreshToken(RefreshTokenDTO refreshTokenDTO, String refreshKey, boolean isAdmin) {
        validateRefreshToken(refreshTokenDTO, refreshKey);
        User user = findUserByToken(refreshTokenDTO.refreshToken(), refreshKey, isAdmin);

        String hashToken = HashValueUtil.hashString(refreshTokenDTO.refreshToken());
        if (isTokenUsed(user, hashToken)) {
            handleUsedToken(user);
            throw new CustomException(ErrorCode.INVALID_REFRESHTOKEN);
        }

        updateUserWithNewRefreshToken(user, hashToken);
        return generateTokens(user, isAdmin);
    }

    private void handleUsedToken(User user) {
        String newKey = RamdomKeyUtil.generateRandomKey();
        user.setKeyToken(newKey);
        user.setRefreshTokenUsed(new HashSet<>());
        redisService.setKey("keyToken:" + user.getId(), newKey);
        userRepository.save(user);
    }

    private TokenPair generateTokens(User user, boolean isAdmin) {
        TokenPair tokens = new TokenPair();
        tokens.setAccessToken(
                jwtService.generateToken(user, user.getKeyToken(), isAdmin ? expAccessTokenAdmin : expAccessToken));
        tokens.setRefreshToken(jwtService.generateToken(user, isAdmin ? refreshKeyAdmin : refreshKey,
                isAdmin ? expRefreshTokenAdmin : expRefreshToken));
        return tokens;
    }

    private void updateUserWithNewRefreshToken(User user, String hashToken) {
        Set<String> refreshTokens = user.getRefreshTokenUsed();
        refreshTokens.add(hashToken);
        user.setRefreshTokenUsed(refreshTokens);
        userRepository.save(user);
    }

    private boolean isTokenUsed(User user, String hashToken) {
        return user.getRefreshTokenUsed().contains(hashToken);
    }

    private User findUserByToken(String token, String refreshKey, boolean isAdmin) {
        String userId = jwtService.extractIdUser(token, refreshKey);
        if (isAdmin) {
            return userRepository.findByIdAndRole(userId, RoleUser.ADMIN_ROLE)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        } else {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_EXISTED));
        }
    }

    private void validateRefreshToken(RefreshTokenDTO refreshTokenDTO, String refreshKey) {
        if (!jwtService.isTokenValid(refreshTokenDTO.refreshToken(), refreshKey)) {
            throw new CustomException(ErrorCode.INVALID_REFRESHTOKEN);
        }
    }

}