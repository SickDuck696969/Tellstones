package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;


import java.security.SecureRandom;

import java.util.Map;

import java.util.Optional;

@Controller
public class LoginController {

    public class OtpUtil {
        private static final SecureRandom random = new SecureRandom();

        public static String generateOtp(int length) {
            StringBuilder otp = new StringBuilder();
            for (int i = 0; i < length; i++) {
                otp.append(random.nextInt(10)); // 0-9
            }
            return otp.toString();
        }
    }

    private final AccountService accountService;
    private final Emailservice themailman;

    private final Map<String, String> otpStore = new ConcurrentHashMap<>();
    private final Map<String, LocalDateTime> otpExpiry = new ConcurrentHashMap<>();


    public LoginController(AccountService accountService, Emailservice themailman) {
        this.accountService = accountService;
        this.themailman = themailman;
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

    @GetMapping("/Reset")
    public String gotoreset() {
        return "tellstone/forgot-password";
    }

    @PostMapping("/Reset")
    @ResponseBody
    public Map<String, String> sendOtp(@RequestParam("email") String email) {
        Optional<Account> a = accountService.getAccountByEmail(email);

        if (a.isEmpty()) {
            return Map.of("status", "error", "message", "Email not found");
        }

        String otp = OtpUtil.generateOtp(6);

        otpStore.put(email, otp);
        otpExpiry.put(email, LocalDateTime.now().plusMinutes(5));

        String html = """
            <p>Your OTP code is:</p>
            <h2>%s</h2>
            <p>This OTP expires in 5 minutes.</p>
        """.formatted(otp);

        try {
            themailman.sendHtmlEmail(email, "OTP for Password Reset", html);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("status", "error", "message", "Failed to send email");
        }

        return Map.of("status", "ok", "message", "OTP sent");
    }

    @PostMapping("/verify-otp")
    @ResponseBody
    public Map<String, String> verifyOtp(
            @RequestParam String email,
            @RequestParam String otp) {

        if (!otpStore.containsKey(email)) {
            return Map.of("status", "error", "message", "No OTP found");
        }

        if (LocalDateTime.now().isAfter(otpExpiry.get(email))) {
            otpStore.remove(email);
            return Map.of("status", "error", "message", "OTP expired");
        }

        if (!otpStore.get(email).equals(otp)) {
            return Map.of("status", "error", "message", "Invalid OTP");
        }

        // OTP valid → remove it
        otpStore.remove(email);

        return Map.of("status", "ok", "message", "OTP verified");
    }

    @GetMapping("/Resetpassword/{id}")
    public String showresetform() {
        return "tellstone/reset-password.html";
    }

    @GetMapping("/Reset/{id}")
    public ResponseEntity<?> showpassForm(@PathVariable Long id) {
        Account Account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + id));
        return ResponseEntity.ok(Account);
    }

    @PostMapping("/Reset/{id}")
    @ResponseBody
    public Map<String, String> setresetingpassword(
            @PathVariable Long id,
            @RequestParam("password") String password,
            @RequestParam("email") String email) {

        if (otpStore.containsKey(email)) {
            return Map.of("success", "false", "message", "OTP not verified");
        }

        Optional<Account> a = accountService.getAccountById(id);

        if (a.isEmpty()) {
            return Map.of("success", "false", "message", "Account not found");
        }

        a.get().setPassword(password);
        accountService.save(a.get());

        return Map.of("success", "true", "message", "Password updated successfully");
    }

}
