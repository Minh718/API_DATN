package com.shop.fashion.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.shop.fashion.dtos.dtosReq.CartAddProductDTO;
import com.shop.fashion.dtos.dtosRes.CartProductSizeColorDTO;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.User;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private ProductSizeColorRepository productSizeColorRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private CartProductSizeColorRepository cartProductSizeColorRepository;
    @Mock
    private SecurityContext securityContext;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private CartService cartService; // Replace with your actual service class

    @Test
    void testAddProductToCart_success() {
        // Mock SecurityContext
        String userId = "user123";
        when(authentication.getName()).thenReturn(userId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Setup mock data
        String cartId = "777L";
        Long productSizeColorId = 789L;
        int quantity = 3;
        CartAddProductDTO dto = new CartAddProductDTO();
        dto.setProductSizeColorId(productSizeColorId);
        dto.setQuantity(quantity);

        Cart cart = new Cart();
        cart.setId(cartId);

        User user = new User();
        user.setId(userId);
        user.setCart(cart);

        ProductSizeColor psc = new ProductSizeColor();
        psc.setId(productSizeColorId);

        CartProductSizeColor cartPSC = CartProductSizeColor.builder()
                .cart(cart)
                .productSizeColorId(productSizeColorId)
                .build();

        // Mock repository & redis calls
        when(userRepository.findByIdWithCart(userId)).thenReturn(Optional.of(user));
        when(productSizeColorRepository.findById(productSizeColorId)).thenReturn(Optional.of(psc));
        when(redisService.getKey("productSizeColor:" + productSizeColorId)).thenReturn(10);
        when(cartProductSizeColorRepository.findByCartIdAndProductSizeColorId(cartId, productSizeColorId))
                .thenReturn(Optional.of(cartPSC));

        // Execute
        cartService.addProductToCart(dto);

        // Verify interactions
        verify(cartProductSizeColorRepository).save(any(CartProductSizeColor.class));
    }

    @Test
    void testAddProductToCart_userNotFound_throwsException() {
        // Given
        String userId = "nonexistent_user";
        CartAddProductDTO dto = new CartAddProductDTO();
        dto.setProductSizeColorId(123);
        dto.setQuantity(1);

        // Mock SecurityContext
        when(authentication.getName()).thenReturn(userId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByIdWithCart(userId)).thenReturn(Optional.empty());

        // Then
        CustomException ex = assertThrows(CustomException.class, () -> {
            cartService.addProductToCart(dto);
        });
        assertEquals(ErrorCode.USER_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void testAddProductToCart_productSizeNotFound_throwsException() {
        // Given
        String userId = "user123";
        Long productSizeColorId = 123L;
        CartAddProductDTO dto = new CartAddProductDTO();
        dto.setProductSizeColorId(productSizeColorId);
        dto.setQuantity(1);

        Cart cart = new Cart();
        User user = new User();
        user.setId(userId);
        user.setCart(cart);

        // Mock SecurityContext
        when(authentication.getName()).thenReturn(userId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByIdWithCart(userId)).thenReturn(Optional.of(user));
        when(productSizeColorRepository.findById(productSizeColorId)).thenReturn(Optional.empty());

        // Then
        CustomException ex = assertThrows(CustomException.class, () -> {
            cartService.addProductToCart(dto);
        });
        assertEquals(ErrorCode.PRODUCT_SIZE_NOT_EXISTED, ex.getErrorCode());
    }

    @Test
    void testAddProductToCart_quantityNotEnough_throwsException() {
        // Given
        String userId = "user123";
        Long productSizeColorId = 123L;
        int quantity = 5;

        CartAddProductDTO dto = new CartAddProductDTO();
        dto.setProductSizeColorId(productSizeColorId);
        dto.setQuantity(quantity);

        Cart cart = new Cart();
        cart.setId("cart123");

        User user = new User();
        user.setId(userId);
        user.setCart(cart);

        ProductSizeColor psc = new ProductSizeColor();
        psc.setId(productSizeColorId);

        // Mock SecurityContext
        when(authentication.getName()).thenReturn(userId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByIdWithCart(userId)).thenReturn(Optional.of(user));
        when(productSizeColorRepository.findById(productSizeColorId)).thenReturn(Optional.of(psc));
        when(redisService.getKey("productSizeColor:" + productSizeColorId)).thenReturn(2); // less than requested

        // Then
        CustomException ex = assertThrows(CustomException.class, () -> {
            cartService.addProductToCart(dto);
        });
        assertEquals(ErrorCode.QUANTITY_NOT_ENOUGH, ex.getErrorCode());
    }

    @Test
    void testRemoveProductFromCart_success() {
        // Given
        String userId = "user123";
        long cartProductSizeColorId = 123L;
        String cartId = "cart456";

        Cart cart = new Cart();
        cart.setId(cartId);

        User user = new User();
        user.setId(userId);
        user.setCart(cart);

        CartProductSizeColor cartProduct = CartProductSizeColor.builder()
                .id(cartProductSizeColorId)
                .cart(cart)
                .productSizeColorId(789L)
                .build();

        // Mock SecurityContext
        when(authentication.getName()).thenReturn(userId);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        // Mocks
        when(userRepository.findByIdWithCart(userId)).thenReturn(Optional.of(user));
        when(cartProductSizeColorRepository.findByIdAndCartId(cartProductSizeColorId, cartId))
                .thenReturn(Optional.of(cartProduct));

        // When
        cartService.removeProductFromCart(cartProductSizeColorId);

        // Then
        verify(cartProductSizeColorRepository).delete(cartProduct);
    }

    @Test
    void getAllProductOfCart_success() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");

        Cart cart = new Cart();
        cart.setId("789D");
        User user = new User();
        user.setCart(cart);

        CartProductSizeColor cartItem = new CartProductSizeColor();
        cartItem.setQuantity(3);
        cartItem.setProductSizeColorId(10L);

        ProductSizeColor productSizeColor = new ProductSizeColor();
        ProductSize productSize = new ProductSize();
        productSize.setId(100L);
        productSizeColor.setProductSize(productSize);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(cartProductSizeColorRepository.findAllByCartIdOrderByUpdateAtDesc(eq("789D"), any()))
                .thenReturn(new PageImpl<>(List.of(cartItem)));
        when(redisService.getKey("productSizeColor:10")).thenReturn(5);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(10L))
                .thenReturn(Optional.of(productSizeColor));
        when(redisService.getKey("productSize:100")).thenReturn(20);

        Page<CartProductSizeColorDTO> result = cartService.getAllProductOfCart(0, 10);

        assertEquals(1, result.getTotalElements());
        verify(cartProductSizeColorRepository, never()).delete(any());
        verify(cartProductSizeColorRepository, never()).save(any());
    }

    @Test
    void getAllProductOfCart_quantityTooHigh_adjustsAndSaves() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");

        Cart cart = new Cart();
        cart.setId("1L");
        User user = new User();
        user.setCart(cart);

        CartProductSizeColor cartItem = new CartProductSizeColor();
        cartItem.setQuantity(10);
        cartItem.setProductSizeColorId(20L);

        ProductSizeColor productSizeColor = new ProductSizeColor();
        ProductSize productSize = new ProductSize();
        productSize.setId(200L);
        productSizeColor.setProductSize(productSize);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(cartProductSizeColorRepository.findAllByCartIdOrderByUpdateAtDesc(eq("1L"), any()))
                .thenReturn(new PageImpl<>(List.of(cartItem)));
        when(redisService.getKey("productSizeColor:20")).thenReturn(5);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(20L))
                .thenReturn(Optional.of(productSizeColor));
        when(redisService.getKey("productSize:200")).thenReturn(15);

        Page<CartProductSizeColorDTO> result = cartService.getAllProductOfCart(0, 10);

        assertEquals(1, result.getContent().size());
        assertEquals(5, result.getContent().get(0).getQuantity());
        verify(cartProductSizeColorRepository).save(cartItem);
    }

    @Test
    void getAllProductOfCart_quantityNull_deletesItem() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");

        Cart cart = new Cart();
        cart.setId("1L");
        User user = new User();
        user.setCart(cart);

        CartProductSizeColor cartItem = new CartProductSizeColor();
        cartItem.setQuantity(2);
        cartItem.setProductSizeColorId(30L);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(cartProductSizeColorRepository.findAllByCartIdOrderByUpdateAtDesc(eq("1L"), any()))
                .thenReturn(new PageImpl<>(List.of(cartItem)));
        when(redisService.getKey("productSizeColor:30")).thenReturn(null);

        Page<CartProductSizeColorDTO> result = cartService.getAllProductOfCart(0, 10);

        assertTrue(result.getContent().isEmpty());
        verify(cartProductSizeColorRepository).delete(cartItem);
    }

    @Test
    void getAllProductOfCart_productSizeColorNotFound_throwsException() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");

        Cart cart = new Cart();
        cart.setId("1L");
        User user = new User();
        user.setCart(cart);

        CartProductSizeColor cartItem = new CartProductSizeColor();
        cartItem.setQuantity(1);
        cartItem.setProductSizeColorId(99L);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(cartProductSizeColorRepository.findAllByCartIdOrderByUpdateAtDesc(eq("1L"), any()))
                .thenReturn(new PageImpl<>(List.of(cartItem)));
        when(redisService.getKey("productSizeColor:99")).thenReturn(3);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(99L))
                .thenReturn(Optional.empty());

        assertThrows(CustomException.class, () -> cartService.getAllProductOfCart(0, 10));
    }
}
