package com.shop.fashion.controllers;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.services.OrderService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/order")
public class OrderController {
        private final OrderService orderService;

        @GetMapping("/all")
        public ApiMetaRes<List<OrderResDTO>> getAllOrders(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<OrderResDTO> ordersPage = orderService.getAllOrders(page, size);
                MetadataDTO metadata = new MetadataDTO(
                                ordersPage.getTotalElements(),
                                ordersPage.getTotalPages(),
                                ordersPage.getNumber(),
                                ordersPage.getSize());
                return ApiMetaRes.<List<OrderResDTO>>builder().code(1000).message("get orders success")
                                .result(ordersPage.getContent()).metadata(metadata)
                                .build();
        }

        @GetMapping("/filter/{orderStatus}")
        public ApiMetaRes<List<OrderResDTO>> getOrdersByOrderStatus(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @PathVariable OrderStatus orderStatus) {
                Page<OrderResDTO> ordersPage = orderService.getOrdersByOrderStatus(page, size, orderStatus);
                MetadataDTO metadata = new MetadataDTO(
                                ordersPage.getTotalElements(),
                                ordersPage.getTotalPages(),
                                ordersPage.getNumber(),
                                ordersPage.getSize());
                return ApiMetaRes.<List<OrderResDTO>>builder().code(1000).message("get orders success")
                                .result(ordersPage.getContent()).metadata(metadata)
                                .build();
        }

        @GetMapping("/detail/{id}")
        public ApiRes<DetailOrderDTO> getDetailOrder(@PathVariable Long id) {
                return ApiRes.<DetailOrderDTO>builder().code(1000).message("Create order success")
                                .result(orderService.getDetailOrder(id))
                                .build();
        }
}