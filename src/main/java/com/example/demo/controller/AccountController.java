package com.example.demo.controller;

import com.example.demo.model.Account;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/account")
public class AccountController {

    @GetMapping
    public String accountPage(@AuthenticationPrincipal Account account, Model model) {
        model.addAttribute("account", account);
        return "tellstone/account";
    }
}
