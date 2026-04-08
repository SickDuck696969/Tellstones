package com.example.demo.model;
 
import jakarta.persistence.*;
 
@Entity 
@Table(name = "cart") 
public class Cart { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 

    @ManyToOne 
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE")) 
    private Account account; 

    @ManyToOne 
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE")) 
    private Product product; 
 
    @ManyToOne 
    @JoinColumn(name = "category_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE CASCADE"))
    private Category category; 

    public Cart() {
    }

    public Cart(Long id, Account account, Product product, Category category) {
        this.id = id;
        this.account = account;
        this.product = product;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
