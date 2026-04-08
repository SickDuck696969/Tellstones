package com.example.demo.model;
 
import jakarta.persistence.*;
 
@Entity 
@Table(name = "product") 
public class Product { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
 
    private String name; 
    private double price; 
    private String description; 
    private String image_string;
 
    @ManyToOne 
    @JoinColumn(name = "category_id") 
    private Category category; 

    public Product() {
    }

    public Product(Long id, String name, double price, String description, String image_string, Category category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.description = description;
        this.image_string = image_string;
        this.category = category;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage_string() {
        return image_string;
    }

    public void setImage_string(String image_string) {
        this.image_string = image_string;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }
}
