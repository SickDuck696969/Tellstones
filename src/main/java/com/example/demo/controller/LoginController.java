package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class LoginController {

    private final AccountService accountService;

    public LoginController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/login")
    public String login() {
        return "tellstone/login";
    }

    @GetMapping("/signup")
    public String signup() {
        return "tellstone/signup";
    }

    @PostMapping("/signup")
    public String signup(Account account) {
        accountService.save(account);
        return "redirect:/login";
    }
}
