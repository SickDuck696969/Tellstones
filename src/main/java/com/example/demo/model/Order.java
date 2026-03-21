package com.example.demo.model;
 
import jakarta.persistence.*; 
import lombok.*; 
 
@Setter 
@Getter 
@RequiredArgsConstructor 
@AllArgsConstructor 
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
} 