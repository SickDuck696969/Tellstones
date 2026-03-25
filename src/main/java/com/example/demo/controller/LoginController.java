package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class LoginController {

    private final AccountService accountService;
    @Autowired
    private AuthenticationManager authenticationManager;

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
    public String signup(@ModelAttribute Account account,
                        RedirectAttributes redirectAttributes) {

        try {
            // Check if username already exists
            if (accountService.getAccountByName(account.getUsername()).isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Username already taken.");
                return "redirect:/signup";
            }

            // Save account (password encoding happens inside service)
            accountService.addAccount(account);

            redirectAttributes.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Signup failed. Try again.");
            return "redirect:/signup";
        }
    }
}
