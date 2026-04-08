package com.example.demo.model;
 
import jakarta.persistence.*; 
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

    public OrderDetail() {
    }

    public OrderDetail(Long id, int quantity, Product product, double shipping, Order order) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
        this.shipping = shipping;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public double getShipping() {
        return shipping;
    }

    public void setShipping(double shipping) {
        this.shipping = shipping;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }
} 
