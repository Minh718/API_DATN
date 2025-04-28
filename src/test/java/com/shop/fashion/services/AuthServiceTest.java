package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.*;

import java.io.IOException;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.shop.fashion.constants.RoleUser;
import com.shop.fashion.dtos.EmailDetailDto;
import com.shop.fashion.dtos.dtosReq.GetTokenGoogleReq;
import com.shop.fashion.dtos.dtosReq.UserSignin;
import com.shop.fashion.dtos.dtosReq.UserSignupByEmail;
import com.shop.fashion.dtos.dtosReq.UserSignupByPhone;
import com.shop.fashion.dtos.dtosRes.GetTokenGoogleRes;
import com.shop.fashion.dtos.dtosRes.OutBoundInfoUser;
import com.shop.fashion.dtos.dtosRes.UserInfoToken;
import com.shop.fashion.entities.Role;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.ChatBoxRepository;
import com.shop.fashion.repositories.RoleRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.httpClient.OutboundIdentityClientGoogle;
import com.shop.fashion.repositories.httpClient.OutboundInfoUserGoogle;

import io.jsonwebtoken.Jwts;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OutboundIdentityClientGoogle outboundIdentityClientGoogle;

    @Mock
    private OutboundInfoUserGoogle outboundInfoUserGoogle;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private TemplateService templateService;

    @Mock
    private MailService mailService;

    @Mock
    private JwtService jwtService;

    @Mock
    private VoucherService voucherService;

    @Mock
    private RoleRepository roleRepository; // <-- Add this line

    @Mock
    private ChatBoxRepository chatBoxRepository; // <-- Add this line

    @Spy
    @InjectMocks
    private AuthService authService;

    @Test
    void testHandleUserSignupByEmail_success() throws IOException {
        // Given
        UserSignupByEmail request = new UserSignupByEmail();
        request.setEmail("test@example.com");
        request.setPassword("securepassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(templateService.getTemplateEmail(anyString(), eq("test@example.com")))
                .thenReturn("Welcome email body");

        // When
        authService.handleUserSignupByEmail(request);

        // Then
        verify(userRepository).findByEmail("test@example.com");
        verify(redisService).setKeyinMinutes(contains(":email"), eq("test@example.com"), eq(5));
        verify(redisService).setKeyinMinutes(contains(":password"), eq("securepassword"), eq(5));
        verify(templateService).getTemplateEmail(anyString(), eq("test@example.com"));
        verify(mailService).sendSimpleMail(any(EmailDetailDto.class));
    }

    @Test
    void testHandleUserSignupByEmail_userAlreadyExists_throwsException() {
        // Given
        UserSignupByEmail request = new UserSignupByEmail();
        request.setEmail("existing@example.com");
        request.setPassword("pass");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User())); // user already
                                                                                                      // exists

        // When / Then
        CustomException thrown = assertThrows(CustomException.class, () -> {
            authService.handleUserSignupByEmail(request);
        });

        assertEquals(ErrorCode.USER_EXISTED, thrown.getErrorCode());
        verify(userRepository).findByEmail("existing@example.com");
        verifyNoMoreInteractions(redisService, templateService, mailService);
    }

    @Test
    void testHandleUserSignupByPhone_success() {
        // Given
        UserSignupByPhone request = new UserSignupByPhone();
        request.setPhone("0123456789");
        request.setOtp("123456");
        request.setPassword("securePass");

        when(redisService.getKey("0123456789")).thenReturn("123456");
        when(roleRepository.findById(RoleUser.USER_ROLE)).thenReturn(Optional.of(new Role()));
        // When
        authService.handleUserSignupByPhone(request);

        // Then
        verify(redisService).getKey("0123456789");
        verify(userRepository).save(any(User.class));
        verify(voucherService).addVouchersToNewUser(any(User.class));
    }

    @Test
    void testHandleUserSignupByPhone_otpExpired_throwsException() {
        // Given
        UserSignupByPhone request = new UserSignupByPhone();
        request.setPhone("0123456789");
        request.setOtp("123456");
        request.setPassword("securePass");

        when(redisService.getKey("0123456789")).thenReturn(null);

        // Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.handleUserSignupByPhone(request);
        });

        assertEquals(ErrorCode.OTP_IS_EXPIRED, exception.getErrorCode());
        verify(redisService).getKey("0123456789");
        verifyNoMoreInteractions(userRepository, voucherService);
    }

    @Test
    void testHandleUserSignupByPhone_invalidOtp_throwsException() {
        // Given
        UserSignupByPhone request = new UserSignupByPhone();
        request.setPhone("0123456789");
        request.setOtp("000000");
        request.setPassword("securePass");

        when(redisService.getKey("0123456789")).thenReturn("123456");

        // Then
        CustomException exception = assertThrows(CustomException.class, () -> {
            authService.handleUserSignupByPhone(request);
        });

        assertEquals(ErrorCode.INVALID_OTP, exception.getErrorCode());
        verify(redisService).getKey("0123456789");
        verifyNoMoreInteractions(userRepository, voucherService);
    }

    @Test
    void testUserSigninByGoogle_createsNewUser_success() {
        // Given
        String code = "auth_code";
        String accessToken = "access_token";

        GetTokenGoogleRes tokenRes = GetTokenGoogleRes.builder()
                .accessToken(accessToken)
                .build();

        OutBoundInfoUser infoUser = new OutBoundInfoUser(
                "google_id_123", // id
                "testuser@example.com", // email
                true, // verifiedEmail
                "User", // familyName
                "Test User", // name
                "Test", // givenName
                "http://image.url", // picture
                "example.com" // hd
        );

        when(outboundIdentityClientGoogle.getToken(any(GetTokenGoogleReq.class)))
                .thenReturn(tokenRes);

        when(outboundInfoUserGoogle.getInfoUser(eq("json"), eq(accessToken)))
                .thenReturn(infoUser);

        when(userRepository.findByidUserGoogle("google_id_123"))
                .thenReturn(Optional.empty());

        Role mockRole = new Role();
        mockRole.setName(RoleUser.USER_ROLE);
        when(roleRepository.findById(RoleUser.USER_ROLE))
                .thenReturn(Optional.of(mockRole));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        when(userRepository.save(userCaptor.capture()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Stub AttachInfoUserWithToken (if needed)
        doReturn(new UserInfoToken()).when(authService).AttachInfoUserWithToken(any(User.class));

        // When
        UserInfoToken result = authService.userSigninByGoogle(code);

        // Then
        verify(outboundIdentityClientGoogle).getToken(any());
        verify(outboundInfoUserGoogle).getInfoUser("json", accessToken);
        verify(userRepository).save(any(User.class));
        verify(redisService).setKeyInMilliseconds(startsWith("keyToken:"), anyString(), anyLong());

        assertNotNull(result);
        User savedUser = userCaptor.getValue();
        assertEquals("google_id_123", savedUser.getIdUserGoogle());
        assertEquals("Test User", savedUser.getName());
    }

    @Test
    void testUserSigninByGoogle_existingUser_success() {
        // Given
        String code = "auth_code";
        String accessToken = "access_token";

        GetTokenGoogleRes tokenRes = GetTokenGoogleRes.builder()
                .accessToken(accessToken)
                .build();

        OutBoundInfoUser infoUser = new OutBoundInfoUser(
                "google_id_123", // id
                "testuser@example.com", // email
                true, // verifiedEmail
                "User", // familyName
                "Test User", // name
                "Test", // givenName
                "http://image.url", // picture
                "example.com" // hd
        );
        User existingUser = User.builder()
                .id("lkm")
                .idUserGoogle("google_id_123")
                .name("Existing User")
                .keyToken("existingKey")
                .build();

        when(outboundIdentityClientGoogle.getToken(any())).thenReturn(tokenRes);
        when(outboundInfoUserGoogle.getInfoUser("json", accessToken)).thenReturn(infoUser);
        when(userRepository.findByidUserGoogle("google_id_123")).thenReturn(Optional.of(existingUser));

        doReturn(new UserInfoToken()).when(authService).AttachInfoUserWithToken(existingUser);

        // When
        UserInfoToken result = authService.userSigninByGoogle(code);

        // Then
        verify(userRepository, never()).save(any());
        verify(redisService).setKeyInMilliseconds(
                eq("keyToken:" + existingUser.getId()),
                eq("existingKey"),
                anyLong());
        verify(redisService).addLoyalUser(existingUser.getId());
        assertNotNull(result);
    }

    @Test
    void testUserSigninByUsername_success() {
        // Given
        String username = "test@example.com";
        String rawPassword = "password123";
        String encodedPassword = new BCryptPasswordEncoder(10).encode(rawPassword);

        User user = User.builder()
                .id("user123")
                .email(username)
                .password(encodedPassword)
                .keyToken("random-key-token")
                .build();

        UserSignin userSignin = new UserSignin();
        userSignin.setUsername(username);
        userSignin.setPassword(rawPassword);

        when(userRepository.findByEmailOrPhone(username)).thenReturn(Optional.of(user));
        doReturn(new UserInfoToken()).when(authService).AttachInfoUserWithToken(user);

        // When
        UserInfoToken result = authService.userSigninbyUsername(userSignin);

        // Then
        verify(userRepository).findByEmailOrPhone(username);
        verify(redisService).setKeyInMilliseconds(eq("keyToken:" + user.getId()), eq(user.getKeyToken()), anyLong());
        verify(redisService).addLoyalUser(user.getId());
        verify(authService).AttachInfoUserWithToken(user);
        assertNotNull(result);
    }
}