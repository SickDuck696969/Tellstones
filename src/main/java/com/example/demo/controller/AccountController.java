package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final Path avatarUploadPath;

    public AccountController(AccountService accountService,
            @Value("${app.upload-dir:uploads}") String uploadDir) {
        this.accountService = accountService;
        this.avatarUploadPath = Paths.get(uploadDir, "avatars").toAbsolutePath().normalize();
    }

    @GetMapping
    public String accountPage(@AuthenticationPrincipal UserDetails user, Model model) {
        if (user == null) {
            return "redirect:/login";
        }
        Account account = accountService.getAccountByUsername(user.getUsername()).orElse(null);
        if (account == null) {
            return "redirect:/";
        }
        model.addAttribute("account", account);
        return "tellstone/account";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails user,
            @RequestParam("displayName") String displayName,
            @RequestParam(value = "avatarFile", required = false) MultipartFile avatarFile,
            RedirectAttributes redirectAttributes) {
        if (user == null) {
            return "redirect:/login";
        }

        Account account = accountService.getAccountByUsername(user.getUsername()).orElse(null);
        if (account == null) {
            return "redirect:/";
        }

        account.setDisplayName(displayName == null ? null : displayName.trim());

        if (avatarFile != null && !avatarFile.isEmpty()) {
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("profileError", "Avatar must be an image file.");
                return "redirect:/account";
            }

            try {
                Files.createDirectories(avatarUploadPath);
                String originalName = avatarFile.getOriginalFilename() == null ? "avatar" : avatarFile.getOriginalFilename();
                String extension = getFileExtension(originalName);
                String fileName = UUID.randomUUID() + extension;
                Path targetPath = avatarUploadPath.resolve(fileName);
                Files.copy(avatarFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
                account.setAvatar("/uploads/avatars/" + fileName);
            } catch (IOException ex) {
                redirectAttributes.addFlashAttribute("profileError", "Could not save avatar right now.");
                return "redirect:/account";
            }
        }

        accountService.save(account);
        redirectAttributes.addFlashAttribute("profileSuccess", "Profile updated successfully.");
        return "redirect:/account";
    }

    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return fileName.substring(lastDot);
    }
}
