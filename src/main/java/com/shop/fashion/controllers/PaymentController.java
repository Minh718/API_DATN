package com.shop.fashion.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.shop.fashion.dtos.dtosRes.ApiRes;
import com.shop.fashion.dtos.dtosRes.PaymentDetailDTO;
import com.shop.fashion.dtos.dtosRes.ProductDetailDTO;
import com.shop.fashion.services.OrderService;
import com.shop.fashion.services.PaymentService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.experimental.NonFinal;

@RestController
@RequestMapping("api/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @NonFinal
    @Value("${frontend_host:http://localhost:3000}")
    private String frontend_host;

    @GetMapping("/vn-pay-callback")
    public void payCallbackHandler(@RequestParam Map<String, String> reqParams,
            HttpServletResponse response) throws IOException {
        orderService.confirmPaymentOrder(reqParams);
        String status = reqParams.get("vnp_ResponseCode");
        if (status.equals("00")) {
            response.sendRedirect(frontend_host + "/payment-success/" + reqParams.get("vnp_TransactionNo"));
        } else {
            response.sendRedirect(frontend_host + "/payment-fail");
        }
    }

    @GetMapping("/{tranId}")
    public ApiRes<PaymentDetailDTO> getPaymentDetail(@PathVariable String tranId) {
        return ApiRes.<PaymentDetailDTO>builder().code(1000)
                .message("get payment detail success")
                .result(paymentService.getPaymentDetail(tranId)).build();
    }
}