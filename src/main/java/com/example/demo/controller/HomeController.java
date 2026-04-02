package com.example.demo.controller;

import com.example.demo.model.Account;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/")
public class HomeController {
    @GetMapping
    public String getAll(Authentication authentication, Model model) {
        boolean loggedIn = authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);

        String avatarUrl = "/logo.png";
        Long accountId = null;
        String accountUsername = null;
        if (loggedIn && authentication.getPrincipal() instanceof Account account) {
            if (account.getAvatar() != null && !account.getAvatar().isBlank()) {
                avatarUrl = account.getAvatar();
            }
            accountId = account.getId();
            accountUsername = account.getUsername();
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("accountId", accountId);
        model.addAttribute("accountUsername", accountUsername);
        return "tellstone/menu";
    }

    @GetMapping("/menu")
    public String menu() {
        return "redirect:/";
    }

    @GetMapping("/menu.html")
    public String menuHtml() {
        return "redirect:/";
    }
}
