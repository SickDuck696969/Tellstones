package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.example.demo.service.AccountService;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

@SpringBootTest
public class AccountServicetest {

    @Autowired
    private AccountService accountService;

    @Test
    void successfullylogin() {

        Map<String, String> result = accountService.checkpassword(
                "lazybone300@gmail.com",
                "Banhmi.com.vn"
        );

        assertEquals("ok", result.get("status"));
    }
}