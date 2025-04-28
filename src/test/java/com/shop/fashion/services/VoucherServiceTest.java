package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.shop.fashion.dtos.dtosReq.VoucherDTO;
import com.shop.fashion.dtos.dtosRes.VoucherResDTO;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.VoucherTargetType;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.VoucherRepository;

@ExtendWith(MockitoExtension.class)
public class VoucherServiceTest {
    @InjectMocks
    private VoucherService voucherService;
    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private RedisService redisService;

    @Test
    void checkVoucher_validVoucher_shouldReturnVoucher() {
        String code = "DISCOUNT50";
        Voucher voucher = Voucher.builder()
                .code(code)
                .isActive(true)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        Voucher result = voucherService.checkVoucher(code);

        assertEquals(voucher, result);
    }

    @Test
    void checkVoucher_voucherNotFound_shouldThrow() {
        String code = "INVALID";
        when(voucherRepository.findByCode(code)).thenReturn(Optional.empty());

        CustomException ex = assertThrows(CustomException.class, () -> voucherService.checkVoucher(code));
        assertEquals(ErrorCode.VOUCHER_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void checkVoucher_voucherInactive_shouldThrow() {
        String code = "INACTIVE";
        Voucher voucher = Voucher.builder()
                .code(code)
                .isActive(false)
                .startDate(LocalDate.now().minusDays(1))
                .endDate(LocalDate.now().plusDays(1))
                .build();

        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        CustomException ex = assertThrows(CustomException.class, () -> voucherService.checkVoucher(code));
        assertEquals(ErrorCode.VOUCHER_NOT_ACTIVE, ex.getErrorCode());
    }

    @Test
    void checkVoucher_voucherNotStartedYet_shouldThrow() {
        String code = "FUTURE";
        LocalDate startDate = LocalDate.now().plusDays(1);
        Voucher voucher = Voucher.builder()
                .code(code)
                .isActive(true)
                .startDate(startDate)
                .endDate(startDate.plusDays(5))
                .build();

        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        CustomException ex = assertThrows(CustomException.class, () -> voucherService.checkVoucher(code));
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains(startDate.toString()));
    }

    @Test
    void checkVoucher_voucherExpired_shouldThrow() {
        String code = "EXPIRED";
        LocalDate endDate = LocalDate.now().minusDays(1);
        Voucher voucher = Voucher.builder()
                .code(code)
                .isActive(true)
                .startDate(LocalDate.now().minusDays(10))
                .endDate(endDate)
                .build();

        when(voucherRepository.findByCode(code)).thenReturn(Optional.of(voucher));

        CustomException ex = assertThrows(CustomException.class, () -> voucherService.checkVoucher(code));
        assertEquals(ErrorCode.BAD_REQUEST, ex.getErrorCode());
        assertTrue(ex.getMessage().contains(endDate.toString()));
    }

    @Test
    void createVoucher_globalTarget_shouldAssignToAllUsers() {
        // Arrange
        VoucherDTO dto = new VoucherDTO();
        dto.setTargetUserType(VoucherTargetType.GLOBAL);

        User user1 = new User();
        user1.setId("user1");
        User user2 = new User();
        user2.setId("user2");

        List<User> users = List.of(user1, user2);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        VoucherResDTO result = voucherService.createVoucher(dto);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAll();
        assertFalse(result.isForNewUser()); // confirm it's not marked as new user voucher
    }

    @Test
    void createVoucher_newUserTarget_shouldMarkForNewUser() {
        // Arrange
        VoucherDTO dto = new VoucherDTO();
        dto.setTargetUserType(VoucherTargetType.NEW_USER);

        // Act
        VoucherResDTO result = voucherService.createVoucher(dto);

        // Assert
        assertTrue(result.isForNewUser());
        verify(userRepository, never()).findAll();
        verify(userRepository, never()).findAllById(any());
    }

    @Test
    void createVoucher_loyalUserTarget_shouldAssignToLoyalUsers() {
        // Arrange
        VoucherDTO dto = new VoucherDTO();
        dto.setTargetUserType(VoucherTargetType.LOYAL_CUSTOMER);

        List<Object> redisIds = List.of("user1", "user2");
        List<User> users = List.of(new User(), new User());

        when(redisService.getStringArray("loyalUser")).thenReturn(redisIds);
        when(userRepository.findAllById(List.of("user1", "user2"))).thenReturn(users);

        // Act
        VoucherResDTO result = voucherService.createVoucher(dto);

        // Assert
        assertNotNull(result);
        verify(userRepository).findAllById(List.of("user1", "user2"));
    }
}