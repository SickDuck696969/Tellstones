package com.example.demo.model;
 
import jakarta.persistence.*; 
 
@Entity 
@Table(name = "orders") 
public class Order { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
 
    @ManyToOne 
    @JoinColumn(name = "account_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (account_id) REFERENCES account(id) ON DELETE CASCADE")) 
    private Account account;

    public Order() {
    }

    public Order(Long id, Account account) {
        this.id = id;
        this.account = account;
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
} 
