package com.shop.fashion.controllers;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosReq.OrderDTO;
import com.shop.fashion.dtos.dtosRes.ApiMetaRes;
import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.DetailOrderDTO;
import com.shop.fashion.dtos.dtosRes.MetadataDTO;
import com.shop.fashion.dtos.dtosRes.OrderResDTO;
import com.shop.fashion.entities.Order;
import com.shop.fashion.enums.OrderStatus;
import com.shop.fashion.exceptions.CustomException;
import com.shop.fashion.exceptions.ErrorCode;
import com.shop.fashion.mappers.OrderMapper;
import com.shop.fashion.services.OrderService;

import jakarta.servlet.http.HttpServletRequest;
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

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @GetMapping("/admin/all")
        public ApiMetaRes<List<OrderResDTO>> getAllOrdersForAdmin(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size) {
                Page<OrderResDTO> ordersPage = orderService.getAllOrdersForAdmin(page, size);
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

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @PutMapping("/{id}/{orderStatus}")
        public ApiRes<OrderResDTO> adminUpdateStatusOrder(
                        @PathVariable Long id,

                        @PathVariable OrderStatus orderStatus) {
                return ApiRes.<OrderResDTO>builder().code(1000).message("update status order success")
                                .result(orderService.adminUpdateStatusOrder(id, orderStatus))
                                .build();
        }

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @GetMapping("/admin/cancel/{id}")
        public ApiRes<Void> adminCancelOrder(
                        @PathVariable Long id) {
                orderService.adminCancelOrder(id);
                return ApiRes.<Void>builder().code(1000).message("cancel order success")
                                .build();
        }

        @GetMapping("/cancel/{id}")
        public ApiRes<Void> userCancelOrder(
                        @PathVariable Long id) {
                orderService.userCancelOrder(id);
                return ApiRes.<Void>builder().code(1000).message("cancel order success")
                                .build();
        }

        @GetMapping("/confirm/{id}")
        public ApiRes<Void> confirmReceiptOrderSuccessfully(
                        @PathVariable Long id) {
                orderService.confirmReceiptOrderSuccessfully(id);
                return ApiRes.<Void>builder().code(1000).message("cancel order success")
                                .build();
        }

        @PreAuthorize("hasRole('ROLE_ADMIN')")
        @GetMapping("/admin/filter/{orderStatus}")
        public ApiMetaRes<List<OrderResDTO>> getOrdersByOrderStatusForAdmin(@RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "5") int size,
                        @PathVariable OrderStatus orderStatus) {
                Page<OrderResDTO> ordersPage = orderService.getOrdersByOrderStatusForAdmin(page, size, orderStatus);
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

        @PreAuthorize("hasRole('ROLE_ADMIN')")

        @GetMapping("/admin/detail/{id}")
        public ApiRes<DetailOrderDTO> getDetailOrderAdmin(@PathVariable Long id) {
                return ApiRes.<DetailOrderDTO>builder().code(1000).message("Create order success")
                                .result(orderService.getDetailOrderAdmin(id))
                                .build();
        }

        @PostMapping("/save")
        public ApiRes<DetailOrderDTO> save(@RequestBody OrderDTO order, HttpServletRequest request) throws IOException {
                return ApiRes.<DetailOrderDTO>builder().code(1000).message("Create order success")
                                .result(orderService.save(order, request))
                                .build();
        }

        @GetMapping("/number-pending")
        public ApiRes<Long> getNumberPaymentingOrder() {
                return ApiRes.<Long>builder().code(1000).message("Create order success")
                                .result(orderService.getNumberPaymentingOrder())
                                .build();
        }

}