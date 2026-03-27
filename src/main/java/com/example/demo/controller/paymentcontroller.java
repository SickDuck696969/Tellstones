package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.AccountService;
import com.example.demo.service.ProductService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import com.example.demo.payment.vnpay.PaymentDTO;
import com.example.demo.config.payment.VNPAYConfig;
import com.example.demo.util.VNPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import com.example.demo.payment.vnpay.PaymentService;
import com.example.demo.response.ResponseObject;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import com.example.demo.model.Account;

import java.util.*;

@RestController
@RequestMapping("/moolah")
public class paymentcontroller {

    private final PaymentService paymentService;
    private final AccountService accountService;
    public int howmany = 0;

    public paymentcontroller(PaymentService paymentService, AccountService accountService) {
        this.paymentService = paymentService;
        this.accountService = accountService;
    }

    @GetMapping("/vnpay")
    public PaymentDTO.VNPayResponse createPayment(HttpServletRequest request) {
        return paymentService.createVnPayPayment(request);
    }

    @PostMapping("/keeptabs")
    public ResponseEntity<?> handlePayment(@RequestBody Map<String, Integer> request, @AuthenticationPrincipal UserDetails user) {
        System.out.println("Username: " + user.getUsername());
        int gems = request.get("gems");
        howmany = gems;
        return ResponseEntity.ok().body("Received!");
    }

    @GetMapping("/vn-pay-callback")
    public String payCallbackHandler(HttpServletRequest request, @AuthenticationPrincipal UserDetails user) {
        String status = request.getParameter("vnp_ResponseCode");
        if (status.equals("00")) {
            Account s = accountService.getAccountByUsername(user.getUsername()).get();
            s.setCredit(s.getCredit() + howmany);
            accountService.save(s);
            return "redirect:/tellstone/shop";
        } else {
            return "redirect:/tellstone/shop";
        }
    }
}
