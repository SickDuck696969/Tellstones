package com.example.demo.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.service.AccountService;
import com.example.demo.model.Account;

import lombok.RequiredArgsConstructor;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;

@Controller
@RequestMapping("/shop")
@RequiredArgsConstructor
public class ShopController {

    private final AccountService accountService;

    @GetMapping
    public String shopPage(@AuthenticationPrincipal UserDetails user, Model model) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("num", s.getCredit());
        return "tellstone/shop";
    }
    

    @GetMapping("/gem")
    public String gemshopPage(@AuthenticationPrincipal UserDetails user, Model model) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("num", s.getCredit());
        return "tellstone/gem";
    }
}
