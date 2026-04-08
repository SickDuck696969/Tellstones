package com.example.demo.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;

@Controller
@RequestMapping("/rules")
public class RuleController {

    @GetMapping
    public String showRules() {
        return "tellstone/rules";
    }

    @GetMapping("/Rule.pdf")
    public ResponseEntity<InputStreamResource> getRulePdf() throws IOException {
        ClassPathResource pdfFile = new ClassPathResource("Rule.pdf");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfFile.getInputStream()));
    }
}
