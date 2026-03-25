package com.example.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;

@Controller
public class AssetController {

    @GetMapping("/logo.png")
    public ResponseEntity<InputStreamResource> getLogo() throws IOException {
        ClassPathResource logoFile = new ClassPathResource("logo.png");
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(new InputStreamResource(logoFile.getInputStream()));
    }
}
