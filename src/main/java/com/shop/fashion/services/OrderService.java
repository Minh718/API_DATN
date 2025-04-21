package com.shop.fashion.services;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
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
import com.shop.fashion.entities.RecommendProduct;
import com.shop.fashion.entities.User;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.enums.PaymentMethod;
import com.shop.fashion.enums.PaymentStatus;
import com.shop.fashion.enums.ShippingStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.mappers.VoucherMapper;
import com.shop.fashion.repositories.CartProductSizeColorRepository;
import com.shop.fashion.repositories.OrderRepository;
import com.shop.fashion.repositories.PaymentRepository;
import com.shop.fashion.repositories.ProductSizeColorRepository;
import com.shop.fashion.repositories.RecommendProductRepository;
import com.shop.fashion.repositories.UserRepository;
import com.shop.fashion.repositories.VoucherRepository;
import com.shop.fashion.utils.DateTimeUtil;

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
    private final PaymentService paymentService;
    private final PaymentRepository paymentRepository;
    private final RecommendProductRepository recommendProductRepository;
    @Autowired
    @Lazy
    private OrderService self;

    public Page<OrderResDTO> getOrdersByOrderStatus(int page, int size, OrderStatus orderStatus) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllByUserIdAndOrderStatusOrderByCreatedAtDesc(userId, orderStatus,
                pageable);

        return orders
                .map(OrderMapper.INSTANCE::toOrderResDTO);
    }

    public Page<OrderResDTO> getOrdersByOrderStatusForAdmin(int page, int size, OrderStatus orderStatus) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllByOrderStatusOrderByCreatedAtDesc(orderStatus,
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

    public Page<OrderResDTO> getAllOrdersForAdmin(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Order> orders = orderRepository.findAllOrderByCreatedAtDesc(pageable);

        return orders
                .map(OrderMapper.INSTANCE::toOrderResDTO);
    }

    public DetailOrderDTO getDetailOrder(Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = orderRepository
                .findByIdAndUserIdFetchOrderProductFetchProductSizeColorFetchProductSize(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REQUEST));
        DetailOrderDTO reOrder = OrderMapper.INSTANCE.toDetailOrderDTO(order);
        if (order.getCode() != null) {
            Voucher voucher = voucherRepository.findByCode(order.getCode()).orElseThrow();
            reOrder.setVoucher(VoucherMapper.INSTANCE.toVoucherResDTO(voucher));
        }
        return reOrder;
    }

    public DetailOrderDTO getDetailOrderAdmin(Long id) {
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
        Set<Long> idProducts = new HashSet<>();
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
                    processProductOrder(userId, productSizeColorId, cpsc, productOrders, cart, idProducts);
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
        saveForRecommendProduct(idProducts);
        return OrderMapper.INSTANCE.toDetailOrderDTO(newOrder);
    }

    private void saveForRecommendProduct(Set<Long> idProducts) {
        List<Long> productList = new ArrayList<>(idProducts); // Convert Set to List for indexing
        int length = productList.size();

        for (int i = 0; i < length; i++) {
            for (int j = i + 1; j < length; j++) {
                Long p1 = productList.get(i);
                Long p2 = productList.get(j);

                Optional<RecommendProduct> existingRp = recommendProductRepository.findByProductPair(p1, p2);

                if (existingRp.isPresent()) {
                    RecommendProduct rp = existingRp.get();
                    rp.setOccurrences(rp.getOccurrences() + 1);
                    recommendProductRepository.save(rp);
                } else {
                    RecommendProduct newRp = RecommendProduct.builder()
                            .p1(p1)
                            .p2(p2)
                            .occurrences(1L)
                            .build();
                    recommendProductRepository.save(newRp);
                }
            }
        }
    }

    private void processProductOrder(String userId, Long productSizeColorId, CartProductSizeColor cpsc,
            Set<OrderProduct> productOrders, Cart cart, Set<Long> idProducts) {
        int quantityStock = (int) redisService.getKey("productSizeColor:" + productSizeColorId);
        int quantityBuy = cpsc.getQuantity();
        ProductSizeColor productSizeColor = productSizeColorRepository
                .findByIdFetchProductSizeAndFetchProduct(productSizeColorId)
                .orElseThrow();
        Product product = productSizeColor.getProductSize().getProduct();
        idProducts.add(product.getId());
        if (quantityStock >= quantityBuy) {
            OrderProduct orderProduct = OrderProduct.builder()
                    .productSizeColor(productSizeColor)
                    .quantity(quantityBuy)
                    .price(product.getPrice() * (100 - product.getPercent()) / 100)
                    .name(product.getName())
                    .image(product.getImage())
                    .color(productSizeColor.getColor().getName())
                    .size(productSizeColor.getProductSize().getSize().getName())
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

    private Order createNewOrder(OrderDTO orderDTO, Set<OrderProduct> productOrders, User user, Payment payment) {
        Order order = OrderMapper.INSTANCE.toOrder(orderDTO);
        order.setOrderStatus(OrderStatus.PENDING);
        order.setUser(user);
        order.setPayment(payment);

        // Save order first to generate ID
        orderRepository.save(order);

        // Set order ID in payment and save it
        payment.setOrderId(order.getId());
        paymentRepository.save(payment);

        return order;
    }

    public void confirmPaymentOrder(Map<String, String> reqParams) {
        String vnp_SecureHash = reqParams.remove("vnp_SecureHash");
        if (!vnp_SecureHash.equals(paymentService.getVnpSecureHash(reqParams))) {
            throw new CustomException(ErrorCode.ERROR_PAYMENT);
        }
        Long orderId = Long.parseLong(reqParams.get("vnp_OrderInfo"));
        Order order = orderRepository.findById(orderId).orElseThrow();
        String status = reqParams.get("vnp_ResponseCode");
        if (status.equals("00")) {
            order.setOrderStatus(OrderStatus.CONFIRMED);
            Payment payment = order.getPayment();
            payment.setAmount(Long.parseLong(reqParams.get("vnp_Amount")) / 2500000);
            payment.setTransactionID(reqParams.get("vnp_TransactionNo"));
            payment.setPaymentStatus(PaymentStatus.PAID);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            payment.setCreatedAt(LocalDateTime.parse(reqParams.get("vnp_PayDate"), formatter));
            orderRepository.save(order);
        } else {
            Hibernate.initialize(order.getOrderProducts());
            self.rollbackOnOrderCancellation(order);
        }
    }

    @Async("rollBackOrder")
    public void rollbackOnOrderCancellation(Order order) {
        Set<OrderProduct> orderProducts = order.getOrderProducts();
        for (OrderProduct orderProduct : orderProducts) {
            ProductSizeColor productSizeColor = orderProduct.getProductSizeColor();
            redisService.incrementKey("productSizeColor:" + productSizeColor.getId(), orderProduct.getQuantity());
            redisService.incrementKey("productSize:" + productSizeColor.getProductSize().getId(),
                    orderProduct.getQuantity());
        }
        order.setOrderStatus(OrderStatus.CANCELED);
        orderRepository.save(order);
    }

    public long getNumberPaymentingOrder() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Order> orders = orderRepository.findAllByOrderStatusAndUserId(OrderStatus.PENDING,
                userId);
        long length = orders.size();
        for (Order order : orders) {
            if (hasPaymentTimeElapsed(order)) {
                self.rollbackOnOrderCancellation(order);
                length -= 1;
            }
        }
        return length;

    }

    private boolean hasPaymentTimeElapsed(Order order) {
        LocalDateTime now = DateTimeUtil.getCurrentVietnamTime();
        Duration duration = Duration.between(order.getCreatedAt(), now);

        return duration.toMinutes() > 15;
    }

    public OrderResDTO adminUpdateStatusOrder(Long id, OrderStatus orderStatus) {
        Order order = orderRepository.findByIdAndOrderStatusNot(id, OrderStatus.CANCELED)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        order.setOrderStatus(orderStatus);
        if (orderStatus == OrderStatus.DELIVERED)
            order.setShippingStatus(ShippingStatus.DELIVERED);
        else if (orderStatus == OrderStatus.SHIPPED)
            order.setShippingStatus(ShippingStatus.IN_TRANSIT);
        else
            order.setShippingStatus(ShippingStatus.NOT_SHIPPED);
        orderRepository.save(order);

        return OrderMapper.INSTANCE.toOrderResDTO(order);
    }

    public void adminCancelOrder(Long id) {
        Order order = orderRepository.findByIdAndOrderStatusNot(id, OrderStatus.CANCELED)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));
        order.setOrderStatus(OrderStatus.CANCELED);
        order.setShippingStatus(ShippingStatus.RETURNED);
        orderRepository.save(order);
        Hibernate.initialize(order.getOrderProducts());
        self.rollbackOnOrderCancellation(order);
    }

    public void userCancelOrder(Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (order.getOrderStatus() != OrderStatus.PENDING && order.getOrderStatus() != OrderStatus.CONFIRMED) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        order.setOrderStatus(OrderStatus.CANCELED);
        order.setShippingStatus(ShippingStatus.RETURNED);
        orderRepository.save(order);
        Hibernate.initialize(order.getOrderProducts());
        self.rollbackOnOrderCancellation(order);
    }

    public void confirmReceiptOrderSuccessfully(Long id) {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        Order order = orderRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.BAD_REQUEST));

        if (order.getShippingStatus() != ShippingStatus.DELIVERED) {
            throw new CustomException(ErrorCode.BAD_REQUEST);
        }

        order.setOrderStatus(OrderStatus.DELIVERED);
        orderRepository.save(order);
    }
}