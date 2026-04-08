package com.example.demo.payment.payos;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.example.demo.payment.payos.PaymentOSDTO;

import java.util.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.apache.commons.codec.binary.Hex;

@Service
public class PaymentOSService {
    private String clientId = "9656a779-b4ea-456e-b897-bf832d45dc18";
    private String apiKey = "d1b4990e-7b78-4464-8263-9581b84bc620";
    private String checksumKey = "a9337f658f92afbb68302ddfd1ffda431274862061a7b93eaff53ca527a8fa4a";

    private String baseUrl = "https://api-merchant.payos.vn";

    private final RestTemplate restTemplate = new RestTemplate();

    private void validate(PaymentOSDTO request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        if (request.getOrderCode() <= 0) {
            throw new IllegalArgumentException("Invalid orderCode");
        }
    }

    private String generateSignature(PaymentOSDTO request) throws Exception {

        String data = "amount=" + request.getAmount()
                + "&cancelUrl=" + request.getCancelUrl()
                + "&description=" + request.getDescription()
                + "&orderCode=" + request.getOrderCode()
                + "&returnUrl=" + request.getReturnUrl();

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
                checksumKey.getBytes(),
                "HmacSHA256"
        );

        mac.init(secretKey);

        byte[] hash = mac.doFinal(data.getBytes());

        return Hex.encodeHexString(hash);
    }

    private Map<String, Object> buildRequestBody(PaymentOSDTO request, String signature) {

        Map<String, Object> body = new HashMap<>();

        body.put("orderCode", request.getOrderCode());
        body.put("amount", request.getAmount());
        body.put("description", request.getDescription());
        body.put("returnUrl", request.getReturnUrl());
        body.put("cancelUrl", request.getCancelUrl());

        body.put("signature", signature);

        return body;
    }

    public String createPaymentLink(PaymentOSDTO request) throws Exception {

        validate(request);

        String signature = generateSignature(request);

        Map<String, Object> body = buildRequestBody(request, signature);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("x-client-id", clientId);
        headers.set("x-api-key", apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                baseUrl + "/v2/payment-requests",
                HttpMethod.POST,
                entity,
                Map.class
        );

        Map data = (Map) response.getBody().get("data");

        return (String) data.get("checkoutUrl");
    }
}
