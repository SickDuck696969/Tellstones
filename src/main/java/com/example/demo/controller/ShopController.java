package com.example.demo.controller;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.service.AccountService;
import com.example.demo.service.stoneskinService;
import com.example.demo.model.Account;
import com.example.demo.model.stoneskin;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;

import java.util.List;

@Controller
@RequestMapping("/shop")
public class ShopController {

    private final AccountService accountService;
    private final stoneskinService stoneskinservice;

    public ShopController(AccountService accountService, stoneskinService stoneskinservice) {
        this.accountService = accountService;
        this.stoneskinservice = stoneskinservice;
    }

    @GetMapping
    public String shopPage(@AuthenticationPrincipal UserDetails user, Model model) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("num", s.getCredit());
        model.addAttribute("fragmentCount", s.getFragment() == null ? 0 : s.getFragment());
        List<Long> ownedSkinIds = stoneskinservice.findByBelong(s).stream()
                .map(stoneskin::getId)
                .toList();
        model.addAttribute("ownedSkinIds", ownedSkinIds);
        return "tellstone/shop";
    }
    

    @GetMapping("/gem")
    public String gemshopPage(@AuthenticationPrincipal UserDetails user, Model model) {
        Account s = accountService.getAccountByUsername(user.getUsername()).get();
        model.addAttribute("num", s.getCredit());
        return "tellstone/gem";
    }
}
