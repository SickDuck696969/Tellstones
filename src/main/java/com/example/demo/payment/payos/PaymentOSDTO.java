package com.example.demo.payment.payos;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class PaymentOSDTO {
    private long orderCode;
    private int amount;
    private String description;

    private String returnUrl;
    private String cancelUrl;

    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;

    private List<Item> items;

    private Long expiredAt;

    public PaymentOSDTO() {
    }

    public PaymentOSDTO(long orderCode, int amount, String description, String returnUrl, String cancelUrl, String buyerName, String buyerEmail, String buyerPhone, List<Item> items, Long expiredAt) {
        this.orderCode = orderCode;
        this.amount = amount;
        this.description = description;
        this.returnUrl = returnUrl;
        this.cancelUrl = cancelUrl;
        this.buyerName = buyerName;
        this.buyerEmail = buyerEmail;
        this.buyerPhone = buyerPhone;
        this.items = items;
        this.expiredAt = expiredAt;
    }
}
