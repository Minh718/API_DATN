package com.shop.fashion.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.entities.Order;
import com.shop.fashion.entities.Voucher;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.mappers.VoucherMapper;
import com.shop.fashion.repositories.OrderRepository;
import com.shop.fashion.repositories.VoucherRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final VoucherRepository voucherRepository;

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
}