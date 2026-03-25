package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import com.example.demo.payment.vnpay.PaymentDTO;
import com.example.demo.config.payment.VNPAYConfig;
import com.example.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import com.example.demo.payment.vnpay.PaymentService;
import com.example.demo.response.ResponseObject;

import java.util.List;

@RestController
@RequestMapping("/moolah")
@RequiredArgsConstructor
public class paymentcontroller {

    private final PaymentService paymentService;

    @GetMapping("/vnpay")
    public PaymentDTO.VNPayResponse createPayment(HttpServletRequest request) {
        return paymentService.createVnPayPayment(request);
    }

    @GetMapping("/vn-pay-callback")
    public ResponseObject<PaymentDTO.VNPayResponse> payCallbackHandler(HttpServletRequest request) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            return new ResponseObject<>(
                HttpStatus.OK,
                "Success",
                PaymentDTO.VNPayResponse.builder()
                    .code("00")
                    .message("Success")
                    .paymentUrl("")
                    .build()
            );
        } else {
            return new ResponseObject<>(HttpStatus.BAD_REQUEST, "Failed", null);
        }
    }
}