package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.model.stoneskin;
import com.example.demo.service.PlayerService;
import com.example.demo.service.stoneskinService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.List;

@Controller
@RequestMapping("/")
public class HomeController {
    private final stoneskinService stoneskinservice;
    private final PlayerService playerService;

    public HomeController(stoneskinService stoneskinservice, PlayerService playerService) {
        this.stoneskinservice = stoneskinservice;
        this.playerService = playerService;
    }

    @GetMapping
    public String getAll(Authentication authentication, Model model) {
        boolean loggedIn = authentication != null
            && authentication.isAuthenticated()
            && !(authentication instanceof AnonymousAuthenticationToken);

        String avatarUrl = "/logo.png";
        Long accountId = null;
        String accountUsername = null;
        List<Long> ownedSkinIds = Collections.emptyList();
        String selectedStoneSkin = "default";
        if (loggedIn && authentication.getPrincipal() instanceof Account account) {
            if (account.getAvatar() != null && !account.getAvatar().isBlank()) {
                avatarUrl = account.getAvatar();
            }
            accountId = account.getId();
            accountUsername = account.getUsername();
            ownedSkinIds = stoneskinservice.findByBelong(account).stream()
                    .map(stoneskin::getId)
                    .toList();
            selectedStoneSkin = playerService.getSelectedSkin(account.getUsername());
        }

        model.addAttribute("loggedIn", loggedIn);
        model.addAttribute("avatarUrl", avatarUrl);
        model.addAttribute("accountId", accountId);
        model.addAttribute("accountUsername", accountUsername);
        model.addAttribute("ownedSkinIds", ownedSkinIds);
        model.addAttribute("selectedStoneSkin", selectedStoneSkin);
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
