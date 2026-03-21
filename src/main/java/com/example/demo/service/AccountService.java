package com.example.demo.service;

import com.example.demo.model.Account;
import com.example.demo.repository.AccountRepository;

import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service; 
import org.springframework.transaction.annotation.Transactional; 
 
import jakarta.validation.constraints.NotNull; 
import java.util.List; 
import java.util.Optional; 

import java.util.Map;
 
@Service 
@RequiredArgsConstructor
@Transactional 
public class AccountService { 
    private final AccountRepository Accrepo; 
    @Autowired
    public Security security;
    // Retrieve all products from the database 
    public List<Account> getAllAccounts() { 
        return Accrepo.findAll(); 
    } 
 
    // Retrieve a product by its id 
    public Optional<Account> getAccountById(Long id) { 
        return Accrepo.findById(id); 
    } 

    // Retrieve a product by its name
    public Optional<Account> getAccountByName(String name) { 
        return Accrepo.findByname(name);
    } 

    public Optional<Account> getAccountByemail(String email){
        return Accrepo.findByemail(email);
    }
 
    // Add a new product to the database 
    public Account addAccount(Account product) { 
        Account yn = new Account();
        yn.setName(product.getName());
        yn.setBirthday(product.getBirthday());
        yn.setEmail(product.getEmail());
        yn.setPassword(security.passwordEncoder().encode(product.getPassword()));
        yn.setIsAdmin(product.getIsAdmin());
        return Accrepo.save(yn); 
    } 
 
    // Update an existing product 
    public Account updateAccount(@NotNull Account product) {
        Account existingProduct = Accrepo.findById(product.getId())
            .orElseThrow(() -> new IllegalStateException("Account with ID " + product.getId() + " does not exist."));

        existingProduct.setName(product.getName());
        existingProduct.setBirthday(product.getBirthday());
        existingProduct.setEmail(product.getEmail());
        existingProduct.setPassword(security.passwordEncoder().encode(product.getPassword()));
        existingProduct.setIsAdmin(product.getIsAdmin());

        return Accrepo.save(existingProduct);
    }

    public Map<String, String> checkpassword(String email, String password){
        Optional<Account> a = Accrepo.findByemail(email);
        if(a.isEmpty()){
            return Map.of("status", "error",
                      "message", "email not registered");
        }
        if(!security.passwordEncoder().matches(password, a.get().getPassword())){
            return Map.of("status", "error",
                      "message", "wrong password");
        }
        return Map.of("status", "ok",
                "message", "login successful");
    }

 
    // Delete a product by its id 
    public void deleteAccountById(Long id) { 
        if (!Accrepo.existsById(id)) {
            throw new IllegalStateException("Account with ID " + id + " does not exist.");
        }
        Accrepo.deleteById(id);
    }

    public void addstorecredit(long id, double amount){
        Account existing = Accrepo.findById(id)
        .orElseThrow(() -> new IllegalStateException("Account with ID " + id + " does not exist."));
        existing.setCredit(existing.getCredit() + 10000);
        Accrepo.save(existing);
    }
} 