package com.example.demo.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Setter 
@Getter 
@RequiredArgsConstructor 
@AllArgsConstructor 
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
}