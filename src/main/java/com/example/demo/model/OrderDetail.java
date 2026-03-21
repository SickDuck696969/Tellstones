package com.example.demo.model;
 
import jakarta.persistence.*; 
import lombok.AllArgsConstructor; 
import lombok.Getter; 
import lombok.RequiredArgsConstructor; 
import lombok.Setter;
@Setter 
@Getter 
@RequiredArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "order_detail") 
public class OrderDetail { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
 
    private int quantity; 
 
    @ManyToOne 
    @JoinColumn(name = "product_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE"))  
    private Product product; 
    private double shipping;
    @ManyToOne 
    @JoinColumn(name = "order_id", foreignKey = @ForeignKey(
                foreignKeyDefinition =
                  "FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE")) 
    private Order order; 
} 