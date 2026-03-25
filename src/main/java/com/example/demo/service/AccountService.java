package com.example.demo.service;

import com.example.demo.config.SecurityConfig;
import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AccountService implements UserDetailsService {

    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public AccountService(AccountRepository accountRepository, PasswordEncoder passwordEncoder) {
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return accountRepository.findByusername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public Account addAccount(Account account) {
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        return accountRepository.save(account);
    }

    

    public Optional<Account> getAccountById(Long id) {
        return accountRepository.findById(id);
    }

    // Retrieve a product by its name
    public Optional<Account> getAccountByName(String name) { 
        return accountRepository.findByusername(name);
    }

    public Map<String, String> checkpassword(String email, String password){
        Optional<Account> a = accountRepository.findByemail(email);
        if(a.isEmpty()){
            return Map.of("status", "error",
                      "message", "email not registered");
        }
        if(!passwordEncoder.matches(password, a.get().getPassword())){
            return Map.of("status", "error",
                      "message", "wrong password");
        }
        return Map.of("status", "ok",
                "message", "login successful");
    }
}
