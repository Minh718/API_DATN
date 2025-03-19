package com.shop.fashion.services;

import java.util.HashSet;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosReq.ItemCheckoutReq;
import com.shop.fashion.dtos.dtosReq.OrderDTO;
import com.shop.fashion.dtos.dtosRes.CheckoutRes;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.entities.Cart;
import com.shop.fashion.entities.CartProductSizeColor;
import com.shop.fashion.entities.Order;
import com.shop.fashion.entities.OrderProduct;
import com.shop.fashion.entities.Payment;
import com.shop.fashion.entities.Product;
import com.shop.fashion.entities.ProductSizeColor;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.PaymentMethod;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.mappers.VoucherMapper;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.OrderRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.VoucherRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;
    private final UserRepository userRepository;
    private final CheckoutService checkoutService;
    private final CartProductSizeColorRepository cartProductSizeColorRepository;
    private final ProductSizeColorRepository productSizeColorRepository;
    private final RedisService redisService;

    public Page<OrderResDTO> getOrdersByOrderStatus(int page, int size, OrderStatus orderStatus) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllByUserIdAndOrderStatusOrderByCreatedAtDesc(userId, orderStatus,
                pageable);

        return orders
                .map(OrderMapper.INSTANCE::toOrderResDTO);
    }

    public Page<OrderResDTO> getAllOrders(int page, int size) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllByUserIdOrderByCreatedAtDesc(userId, pageable);

        return orders
                .map(OrderMapper.INSTANCE::toOrderResDTO);
    }

    public DetailOrderDTO getDetailOrder(Long id) {
        Order order = orderRepository.findByIdFetchOrderProductFetchProductSizeColorFetchProductSize(id)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        DetailOrderDTO reOrder = OrderMapper.INSTANCE.toDetailOrderDTO(order);
        if (order.getCode() != null) {
            Voucher voucher = voucherRepository.findByCode(order.getCode()).orElseThrow();
            reOrder.setVoucher(VoucherMapper.INSTANCE.toVoucherResDTO(voucher));
        }
        return reOrder;
    }

    private void validateCheckout(OrderDTO order, CheckoutRes checkoutRes) {
        if (checkoutRes.getDiscount() != order.getCheckoutRes().getDiscount() ||
                checkoutRes.getPaymentFee() != order.getCheckoutRes().getPaymentFee()
                || checkoutRes.getTotalPrice() != order.getCheckoutRes().getTotalPrice()) {
            throw new CustomException(ErrorCode.RECHECKOUT_FAILED);
        }
    }

    @Transactional(rollbackOn = CustomException.class)
    public DetailOrderDTO save(OrderDTO order, HttpServletRequest request) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByIdWithCart(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.ERROR_SYSTEM));
        Cart cart = user.getCart();
        CheckoutRes checkoutRes = checkoutService.checkoutProducts(order.getCheckoutReq());

        validateCheckout(order, checkoutRes);

        Set<OrderProduct> productOrders = new HashSet<>();

        for (ItemCheckoutReq item : order.getCheckoutReq().getItems()) {
            CartProductSizeColor cpsc = cartProductSizeColorRepository.findById(item.getId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ERROR_SYSTEM));
            Long productSizeColorId = cpsc.getProductSizeColorId();

            int retries = 10;
            int expired = 30;
            boolean productLocked = false;

            for (int i = 0; i < retries; i++) {
                productLocked = redisService.setnxProduct("lock_" + productSizeColorId,
                        i, expired);
                if (productLocked) {
                    processProductOrder(userId, productSizeColorId, cpsc, productOrders, cart);
                    redisService.deleteKey("lock_" + productSizeColorId);
                    break;
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new CustomException(ErrorCode.ERROR_SYSTEM);
                    }
                }
            }

            if (!productLocked) {
                rollbackProductOrders(productOrders);
                throw new CustomException(ErrorCode.ERROR_SYSTEM);
            }
        }
        Payment payment = new Payment();
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setUserId(userId);
        Order newOrder = createNewOrder(order, productOrders,
                user, payment);
        productOrders.forEach(orderProduct -> orderProduct.setOrder(newOrder));
        newOrder.setOrderProducts(productOrders);
        if (order.getPaymentMethod() != PaymentMethod.VNPAY) {
            newOrder.setOrderStatus(OrderStatus.CONFIRMED);
        } else {
            String urlPayment = paymentService.createVnPayPayment(request,
                    newOrder.getTotalAmount(), newOrder.getId());
            newOrder.setUrlPayment(urlPayment);
        }
        return OrderMapper.INSTANCE.toDetailOrderDTO(newOrder);
    }

    private void processProductOrder(String userId, Long productSizeColorId, CartProductSizeColor cpsc,
            Set<OrderProduct> productOrders, Cart cart) {
        int quantityStock = (int) redisService.getKey("productSizeColor:" + productSizeColorId);
        int quantityBuy = cpsc.getQuantity();
        ProductSizeColor productSizeColor = productSizeColorRepository
                .findByIdFetchProductSizeAndFetchProduct(productSizeColorId)
                .orElseThrow();
        Product product = productSizeColor.getProductSize().getProduct();
        if (quantityStock >= quantityBuy) {
            OrderProduct orderProduct = OrderProduct.builder()
                    .productSizeColor(productSizeColor)
                    .quantity(quantityBuy)
                    .price(product.getPrice() * (100 - product.getPercent()) / 100)
                    .name(product.getName())
                    .image(product.getImage())
                    .build();
            productOrders.add(orderProduct);
            redisService.incrementKey("productSizeColor:" + productSizeColor.getId(), -quantityBuy);
            redisService.incrementKey("productSize:" + productSizeColor.getProductSize().getId(), -quantityBuy);
            cartProductSizeColorRepository.delete(cpsc);
        } else {
            rollbackProductOrders(productOrders);
            throw new CustomException(ErrorCode.ERROR_SYSTEM);
        }
    }

    private void rollbackProductOrders(Set<OrderProduct> productOrders) {
        for (OrderProduct orderProduct : productOrders) {
            redisService.incrementKey("productSizeColor:" + orderProduct.getProductSizeColor().getId(),
                    orderProduct.getQuantity());
            redisService.incrementKey("productSize:" + orderProduct.getProductSizeColor().getProductSize().getId(),
                    orderProduct.getQuantity());
        }
    }

    private Order createNewOrder(OrderDTO orderDTO,
            Set<OrderProduct> productOrders, User user, Payment payment) {
        Order order = OrderMapper.INSTANCE.toOrder(orderDTO);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setPayment(payment);
        payment.setOrder(order);
        return orderRepository.save(order);

    }
}