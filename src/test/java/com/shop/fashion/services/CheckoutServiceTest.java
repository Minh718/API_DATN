package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.shop.fashion.dtos.dtosReq.CheckoutReq;
import com.shop.fashion.dtos.dtosReq.ItemCheckoutReq;
import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.UserVoucher;
import com.shop.fashion.entities.UserVoucherId;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.TypeVoucher;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.UserVoucherRepository;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @InjectMocks
    private CheckoutService checkoutService;

    @Mock
    private CartProductSizeColorRepository cartProductSizeColorRepository;

    @Mock
    private RedisService redisService;

    @Mock
    private ProductSizeColorRepository productSizeColorRepository;

    @Mock
    private VoucherService voucherService;

    @Mock
    private UserVoucherRepository userVoucherRepository;

    @Test
    void testCheckoutProducts_successWithoutVoucher() {
        CheckoutReq checkoutReq = new CheckoutReq();
        ItemCheckoutReq item = new ItemCheckoutReq();
        item.setId(1L);
        item.setQuantity(2);
        checkoutReq.setItems(Set.of(item));

        Product product = Product.builder()
                .price(100)
                .percent(10)
                .build();

        ProductSize productSize = new ProductSize();
        productSize.setProduct(product);

        ProductSizeColor psc = new ProductSizeColor();
        psc.setProductSize(productSize);

        CartProductSizeColor cpsc = CartProductSizeColor.builder()
                .id(1L)
                .quantity(2)
                .productSizeColorId(1L)
                .build();

        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.getKey("productSizeColor:1")).thenReturn(10);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(1L)).thenReturn(Optional.of(psc));

        CheckoutRes res = checkoutService.checkoutProducts(checkoutReq);

        assertEquals(180.0, res.getTotalPrice()); // (100 * 0.9) * 2 = 180
        assertEquals(180.0, res.getPaymentFee());
        assertEquals(0.0, res.getDiscount());
    }

    @Test
    void testCheckoutProducts_quantityMismatch_throwsException() {
        CheckoutReq checkoutReq = new CheckoutReq();
        ItemCheckoutReq item = new ItemCheckoutReq();
        item.setId(1L);
        item.setQuantity(3);
        checkoutReq.setItems(Set.of(item));

        CartProductSizeColor cpsc = CartProductSizeColor.builder()
                .id(1L)
                .quantity(2)
                .productSizeColorId(1L)
                .build();

        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));

        CustomException exception = assertThrows(CustomException.class,
                () -> checkoutService.checkoutProducts(checkoutReq));

        assertEquals(ErrorCode.PLEASE_RELOAD_PAGE, exception.getErrorCode());
    }

    @Test
    void testCheckoutProducts_insufficientRedisQuantity_throwsException() {
        CheckoutReq checkoutReq = new CheckoutReq();
        ItemCheckoutReq item = new ItemCheckoutReq();
        item.setId(1L);
        item.setQuantity(2);
        checkoutReq.setItems(Set.of(item));

        CartProductSizeColor cpsc = CartProductSizeColor.builder()
                .id(1L)
                .quantity(2)
                .productSizeColorId(1L)
                .build();

        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.getKey("productSizeColor:1")).thenReturn(1); // insufficient

        ProductSizeColor psc = mock(ProductSizeColor.class);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(1L)).thenReturn(Optional.of(psc));

        CustomException exception = assertThrows(CustomException.class,
                () -> checkoutService.checkoutProducts(checkoutReq));

        assertEquals(ErrorCode.PLEASE_RELOAD_PAGE, exception.getErrorCode());
    }

    @Test
    void testCheckoutProducts_withVoucher_success() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("userId123", null));

        CheckoutReq checkoutReq = new CheckoutReq();
        ItemCheckoutReq item = new ItemCheckoutReq();
        item.setId(1L);
        item.setQuantity(1);
        checkoutReq.setItems(Set.of(item));
        checkoutReq.setCode("DISCOUNT10");

        Product product = Product.builder().price(200).percent(0).build();
        ProductSize ps = new ProductSize();
        ps.setProduct(product);
        ProductSizeColor psc = new ProductSizeColor();
        psc.setProductSize(ps);

        CartProductSizeColor cpsc = CartProductSizeColor.builder()
                .id(1L)
                .quantity(1)
                .productSizeColorId(1L)
                .build();

        Voucher voucher = Voucher.builder()
                .id(100L)
                .minPrice(100)
                .discount(10)
                .type(TypeVoucher.FIXED)
                .build();

        UserVoucher uv = new UserVoucher();
        uv.setUsed(false);
        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.getKey("productSizeColor:1")).thenReturn(5);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(1L)).thenReturn(Optional.of(psc));
        when(voucherService.checkVoucher("DISCOUNT10")).thenReturn(voucher);
        when(userVoucherRepository.findById(any(UserVoucherId.class)))
                .thenReturn(Optional.of(uv));
        CheckoutRes result = checkoutService.checkoutProducts(checkoutReq);

        assertEquals(200.0, result.getTotalPrice());
        assertEquals(10.0, result.getDiscount());
        assertEquals(190.0, result.getPaymentFee());
    }
}