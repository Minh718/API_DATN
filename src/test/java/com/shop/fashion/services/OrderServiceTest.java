package com.shop.fashion.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

import com.shop.fashion.dtos.dtosReq.CheckoutReq;
import com.shop.fashion.dtos.dtosReq.ItemCheckoutReq;
import com.shop.fashion.dtos.dtosReq.OrderDTO;
import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.Color;
import com.shop.fashion.entities.Order;
import com.shop.fashion.entities.Payment;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSize;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.Size;
import com.shop.fashion.entities.User;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.PaymentMethod;
import com.shop.fashion.enums.PaymentStatus;
import com.shop.fashion.enums.ShippingStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.OrderRepository;
import com.shop.fashion.repositories.PaymentRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.RecommendProductRepository;
import com.shop.fashion.repositories.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.*;
import org.junit.jupiter.api.Test;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    @InjectMocks
    private OrderService orderService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private CheckoutService checkoutService;
    @Mock
    private CartProductSizeColorRepository cartProductSizeColorRepository;
    @Mock
    private RedisService redisService;
    @Mock
    private ProductSizeColorRepository productSizeColorRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentService paymentService;
    @Mock
    private RecommendProductRepository recommendProductRepository;

    @Mock
    private HttpServletRequest request;

    @BeforeEach
    void setupSecurityContext() {
        Authentication auth = Mockito.mock(Authentication.class);
        SecurityContext securityContext = Mockito.mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        when(auth.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);
        OrderService spyService = Mockito.spy(orderService);
        spyService.setSelf(spyService); // inject the spy as 'self'
        this.orderService = spyService;
    }

    private User createMockUser() {
        User user = new User();
        Cart cart = new Cart();
        user.setCart(cart);
        return user;
    }

    private CartProductSizeColor createMockCartProductSizeColor(Long pscId) {
        CartProductSizeColor cpsc = new CartProductSizeColor();
        cpsc.setId(1L);
        cpsc.setQuantity(2);
        cpsc.setProductSizeColorId(pscId);
        return cpsc;
    }

    private ProductSizeColor createMockProductSizeColor(Long id) {
        Product product = new Product();
        product.setId(10L);
        product.setName("Shirt");
        product.setPrice(100);
        product.setPercent(0);
        product.setImage("img.png");

        Size size = new Size();
        size.setName("M");

        ProductSize productSize = new ProductSize();
        productSize.setSize(size);
        productSize.setProduct(product);
        productSize.setId(200L);

        Color color = new Color();
        color.setName("Red");

        ProductSizeColor psc = new ProductSizeColor();
        psc.setId(id);
        psc.setProductSize(productSize);
        psc.setColor(color);
        return psc;
    }

    private OrderDTO createMockOrderDTO(PaymentMethod method) {
        ItemCheckoutReq item = new ItemCheckoutReq();
        item.setId(1L);
        item.setQuantity(2);
        CheckoutReq req = new CheckoutReq(null, Set.of(item));
        CheckoutRes res = new CheckoutRes(0.0, 0.0, 200.0);

        OrderDTO dto = new OrderDTO();
        dto.setCheckoutReq(req);
        dto.setCheckoutRes(res);
        dto.setPaymentMethod(method);
        return dto;
    }

    @Test
    void testSaveOrder_successWithoutVnPay() {
        OrderDTO orderDTO = createMockOrderDTO(PaymentMethod.CASH_ON_DELIVERY);
        User user = createMockUser();
        CheckoutRes checkoutRes = orderDTO.getCheckoutRes();
        CartProductSizeColor cpsc = createMockCartProductSizeColor(100L);
        ProductSizeColor productSizeColor = createMockProductSizeColor(100L);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(checkoutService.checkoutProducts(any())).thenReturn(checkoutRes);
        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.setnxProduct(anyString(), any(), anyLong())).thenReturn(true);
        when(redisService.getKey("productSizeColor:100")).thenReturn(10);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(100L))
                .thenReturn(Optional.of(productSizeColor));

        Order mockOrder = new Order();
        mockOrder.setId(1L);
        mockOrder.setTotalAmount(200L);
        when(orderRepository.save(any())).thenReturn(mockOrder);

        DetailOrderDTO result = orderService.save(orderDTO, request);

        assertNotNull(result);
        verify(orderRepository).save(any());
        verify(paymentRepository).save(any());
    }

    @Test
    void testSaveOrder_successWithVnPay() {
        OrderDTO orderDTO = createMockOrderDTO(PaymentMethod.VNPAY);
        User user = createMockUser();
        CheckoutRes checkoutRes = orderDTO.getCheckoutRes();
        CartProductSizeColor cpsc = createMockCartProductSizeColor(100L);
        ProductSizeColor productSizeColor = createMockProductSizeColor(100L);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(checkoutService.checkoutProducts(any())).thenReturn(checkoutRes);
        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.setnxProduct(anyString(), any(), anyLong())).thenReturn(true);
        when(redisService.getKey("productSizeColor:100")).thenReturn(10);
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(100L))
                .thenReturn(Optional.of(productSizeColor));

        when(orderRepository.save(any())).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(123L);
            savedOrder.setTotalAmount(999d);
            return savedOrder;
        });

        when(paymentService.createVnPayPayment(any(), anyDouble(), anyLong()))
                .thenReturn("vnPayUrl");
        DetailOrderDTO result = orderService.save(orderDTO, request);

        assertNotNull(result);
        verify(paymentService).createVnPayPayment(any(), anyDouble(), anyLong());
    }

    @Test
    void testSaveOrder_checkoutMismatch_shouldThrow() {
        OrderDTO orderDTO = createMockOrderDTO(PaymentMethod.CASH_ON_DELIVERY);
        CheckoutRes wrongRes = new CheckoutRes(0, 0, 0);
        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(createMockUser()));
        when(checkoutService.checkoutProducts(any())).thenReturn(wrongRes);

        assertThrows(CustomException.class, () -> orderService.save(orderDTO, request));
    }

    @Test
    void testSaveOrder_productLockFails_shouldThrow() {
        OrderDTO orderDTO = createMockOrderDTO(PaymentMethod.CASH_ON_DELIVERY);
        User user = createMockUser();
        CartProductSizeColor cpsc = createMockCartProductSizeColor(100L);
        ProductSizeColor productSizeColor = createMockProductSizeColor(100L);

        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(checkoutService.checkoutProducts(any())).thenReturn(orderDTO.getCheckoutRes());
        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(100L))
                .thenReturn(Optional.of(productSizeColor));
        when(redisService.setnxProduct(anyString(), any(), anyLong())).thenReturn(false);

        CustomException exception = assertThrows(CustomException.class, () -> {
            orderService.save(orderDTO, request);
        });

        assertEquals(ErrorCode.ERROR_SYSTEM, exception.getErrorCode());
    }

    @Test
    void testSaveOrder_insufficientStock_shouldThrow() {
        OrderDTO orderDTO = createMockOrderDTO(PaymentMethod.CASH_ON_DELIVERY);
        User user = createMockUser();
        CartProductSizeColor cpsc = createMockCartProductSizeColor(100L);
        ProductSizeColor productSizeColor = createMockProductSizeColor(100L);
        when(userRepository.findByIdWithCart("user123")).thenReturn(Optional.of(user));
        when(checkoutService.checkoutProducts(any())).thenReturn(orderDTO.getCheckoutRes());
        when(cartProductSizeColorRepository.findById(1L)).thenReturn(Optional.of(cpsc));
        when(redisService.setnxProduct(anyString(), any(), anyLong())).thenReturn(true);

        when(redisService.getKey("productSizeColor:100")).thenReturn(1); // not
        when(productSizeColorRepository.findByIdFetchProductSizeAndFetchProduct(100L))
                .thenReturn(Optional.of(productSizeColor));

        assertThrows(CustomException.class, () -> orderService.save(orderDTO,
                request));
    }

    @Test
    void testConfirmPaymentOrder_success_shouldUpdateOrderAndPayment() {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("vnp_SecureHash", "secureHash123");
        reqParams.put("vnp_OrderInfo", "123");
        reqParams.put("vnp_ResponseCode", "00");
        reqParams.put("vnp_Amount", "5000000"); // 2.0 amount
        reqParams.put("vnp_TransactionNo", "TXN12345");
        reqParams.put("vnp_PayDate", "20250421091500"); // yyyyMMddHHmmss

        Map<String, String> rawParams = new HashMap<>(reqParams);
        String correctHash = "secureHash123";

        Payment payment = new Payment();
        Order order = new Order();
        order.setId(123L);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setPayment(payment);

        when(paymentService.getVnpSecureHash(anyMap())).thenReturn(correctHash);
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.confirmPaymentOrder(rawParams);

        assertEquals(OrderStatus.CONFIRMED, order.getOrderStatus());
        assertEquals(PaymentStatus.PAID, payment.getPaymentStatus());
        assertEquals("TXN12345", payment.getTransactionID());
        assertEquals(2, payment.getAmount());
        assertNotNull(payment.getCreatedAt());
    }

    @Test
    void testConfirmPaymentOrder_invalidHash_shouldThrow() {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("vnp_SecureHash", "wrongHash");
        reqParams.put("vnp_OrderInfo", "123");

        Map<String, String> rawParams = new HashMap<>(reqParams);

        when(paymentService.getVnpSecureHash(anyMap())).thenReturn("correctHash");

        CustomException ex = assertThrows(CustomException.class, () -> orderService.confirmPaymentOrder(rawParams));

        assertEquals(ErrorCode.ERROR_PAYMENT, ex.getErrorCode());
    }

    @Test
    void testConfirmPaymentOrder_failureStatus_shouldRollback() {
        Map<String, String> reqParams = new HashMap<>();
        reqParams.put("vnp_SecureHash", "secureHash123");
        reqParams.put("vnp_OrderInfo", "123");
        reqParams.put("vnp_ResponseCode", "24"); // not 00
        reqParams.put("vnp_Amount", "5000000");
        reqParams.put("vnp_TransactionNo", "TXN12345");
        reqParams.put("vnp_PayDate", "20250421091500");

        Map<String, String> rawParams = new HashMap<>(reqParams);
        String correctHash = "secureHash123";

        Order order = new Order();
        order.setId(123L);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setOrderProducts(new HashSet<>());

        when(paymentService.getVnpSecureHash(anyMap())).thenReturn(correctHash);
        when(orderRepository.findById(123L)).thenReturn(Optional.of(order));

        // Spy on self to allow verification
        doNothing().when(orderService).rollbackOnOrderCancellation(order);

        orderService.confirmPaymentOrder(rawParams);

        verify(orderService).rollbackOnOrderCancellation(order);
    }

    @Test
    void userCancelOrder_shouldCancelSuccessfully() {
        // Arrange
        Long orderId = 100L;
        String userId = "user123";

        User user = new User();
        user.setId(userId);

        Order order = new Order();
        order.setId(orderId);
        order.setUser(user);
        order.setOrderStatus(OrderStatus.PENDING); // must be cancelable
        order.setShippingStatus(ShippingStatus.NOT_SHIPPED);
        order.setOrderProducts(new HashSet<>());
        when(orderRepository.findByIdAndUserId(orderId, userId)).thenReturn(Optional.of(order));

        // Act
        orderService.userCancelOrder(orderId);

        // Assert
        assertEquals(OrderStatus.CANCELED, order.getOrderStatus());
        assertEquals(ShippingStatus.RETURNED, order.getShippingStatus());
        verify(orderRepository, atLeastOnce()).save(order);
        verify(orderService).rollbackOnOrderCancellation(order);
    }
}
