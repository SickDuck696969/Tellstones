package com.example.demo.controller;

import com.example.demo.model.Account;
import com.example.demo.service.AccountService;
import com.example.demo.service.Emailservice;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Security;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.Optional; 
import java.util.Map;


@Controller
@RequestMapping("/login")
@RequiredArgsConstructor
public class LoginController {

    private final AccountService accountService;
    private final Emailservice themailman;

    @GetMapping("/Login")
    public String backtologin() {
        return "forward:/login/Login.html";
    }

    @PostMapping("/Login")
    @ResponseBody
    public Map<String, String> loginin(@RequestParam("email") String email, @RequestParam("password") String password, HttpSession session) {
        Optional<Account> a = accountService.getAccountByemail(email);
        session.setAttribute("user", a.get().getEmail());
        session.setAttribute("userId", a.get().getId());
        return accountService.checkpassword(email, password);
    }

    @GetMapping("/Reset")
    public String gotoreset() {
        return "forward:/login/Reset.html";
    }

    @PostMapping("/Reset")
    @ResponseBody
    public Map<String, String> resetingpassword(@RequestParam("email") String email) {
        Optional<Account> a = accountService.getAccountByemail(email);
        if(a.isEmpty()){
            return Map.of("status", "error",
                      "message", "email not found");
        }
        String html = """
            <p>Dear Ms. Finding</p>
            <p>Reset your password here:</p>
            <a href="http://localhost:8080/login/Resetpassword/%d">Reset Password</a>
            <p>This link expires in 2 hours.</p>
        """.formatted(a.get().getId());
        themailman.sendHtmlEmail(
            email,
            "Password Reset",
            html
        );
        return Map.of("status", "ok",
                "message", "sent");
    }

    // For adding a new Account
    @GetMapping("/Register")
    public String showAddForm(Model model) {
        return "forward:/login/Register.html";
    }

    // Process the form for adding a new Account
    @PostMapping
    public ResponseEntity<?> addAccount(@Valid @RequestBody Account account) {
        account.setIsAdmin(false);
        Account saved = accountService.addAccount(account);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/Profile")
    public String showrprofile(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return "redirect:/login/Login";
        }
        Long id = (Long) session.getAttribute("userId");
        return "redirect:/login/Profile/" + id;
    }

    @GetMapping("/Profile/{id}")
    public String showrprofile() {
        return "forward:/login/Profile.html";
    }

    @GetMapping("/Changepassword/{id}")
    public String showrchangepasswordform() {
        return "forward:/login/Changepassword.html";
    }
    
    @GetMapping("/Resetpassword/{id}")
    public String showresetform() {
        return "forward:/login/Resetpassword.html";
    }

    @GetMapping("/Reset/{id}")
    public ResponseEntity<?> showpassForm(@PathVariable Long id) {
        Account Account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + id));
        return ResponseEntity.ok(Account);
    }

    @PostMapping("/Reset/{id}")
    @ResponseBody
    public Map<String, String> setresetingpassword(@PathVariable Long id,@RequestParam("password") String password) {
        Optional<Account> a = accountService.getAccountById(id);
        if (a.isEmpty()) {
            return Map.of("success","false",
                        "message","Account not found");
        }
        a.get().setPassword(password);
        accountService.updateAccount(a.get());
        return Map.of("success","true",
                    "message","Password updated successfully");
    }

    @PostMapping("/change-password")
    @ResponseBody
    public Map<String, String> changepassword(
            HttpSession session,
            @RequestBody Map<String, String> request) {

        if (session.getAttribute("userId") == null) {
            return Map.of(
                    "success", "false",
                    "message", "Unauthorized");
        }

        String password = request.get("currentPassword");
        String newpassword = request.get("newPassword");

        Long id = (Long) session.getAttribute("userId");
        Optional<Account> a = accountService.getAccountById(id);

        if (a.isEmpty()) {
            return Map.of(
                    "success", "false",
                    "message", "Account not found");
        }

        if (!accountService.checkpassword(a.get().getEmail(), password)) {
            return Map.of(
                    "success", "false",
                    "message", "Wrong current password");
        }

        a.get().setPassword(newpassword);
        accountService.updateAccount(a.get());

        return Map.of(
                "success", "true",
                "message", "Password changed successfully");
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> dashboard(HttpSession session) {
        if (session.getAttribute("userId") == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        Long id = (Long) session.getAttribute("userId");
        Account Account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + id));
        return ResponseEntity.ok(Account);
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login/Login";
    }

    // For editing a Account
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Account Account = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + id));
        model.addAttribute("Account", Account);
        return "/Accounts/update-Account";
    }

    // Process the form for updating a Account
    @PostMapping("/update/{id}")
    public String updateAccount(@PathVariable Long id,
                                @Valid Account Account,
                                BindingResult result,
                                @RequestParam("image") MultipartFile image,
                                Model model) {

        if (result.hasErrors()) {
            Account.setId(id);
            return "/Accounts/update-Account";
        }

        Account existingAccount = accountService.getAccountById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Account Id:" + id));

        // ✅ Handle image upload
        if (!image.isEmpty()) {
            try {
                String uploadDir = "src/main/resources/static/Accounts/";
                String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();

                Path path = Paths.get(uploadDir, fileName);
                Files.createDirectories(path.getParent());
                Files.write(path, image.getBytes());

                Account.setImage_string("/Accounts/" + fileName);

            } catch (Exception e) {
                throw new RuntimeException("Image upload failed", e);
            }
        } else {
            // ✅ Keep old image if no new one uploaded
            Account.setImage_string(existingAccount.getImage_string());
        }

        Account.setId(id);
        accountService.updateAccount(Account);

        return "redirect:/Accounts";
    }


    // Handle request to delete a Account
    @PostMapping("/delete/{id}")
    public String deleteAccount(@PathVariable Long id) {
        accountService.deleteAccountById(id);
        return "redirect:/Accounts";
    }
}