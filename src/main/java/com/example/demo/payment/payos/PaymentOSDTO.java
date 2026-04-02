package com.example.demo.payment.payos;

import java.util.List;
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

    public long getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(long orderCode) {
        this.orderCode = orderCode;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getReturnUrl() {
        return returnUrl;
    }

    public void setReturnUrl(String returnUrl) {
        this.returnUrl = returnUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerEmail() {
        return buyerEmail;
    }

    public void setBuyerEmail(String buyerEmail) {
        this.buyerEmail = buyerEmail;
    }

    public String getBuyerPhone() {
        return buyerPhone;
    }

    public void setBuyerPhone(String buyerPhone) {
        this.buyerPhone = buyerPhone;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Long getExpiredAt() {
        return expiredAt;
    }

    public void setExpiredAt(Long expiredAt) {
        this.expiredAt = expiredAt;
    }
}
