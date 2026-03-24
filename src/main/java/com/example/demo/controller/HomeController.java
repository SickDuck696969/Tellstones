package com.example.demo.controller;

import com.example.demo.model.Product;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class HomeController {
    @GetMapping
    public String getAll() {
        return "redirect:/products";
    }
}