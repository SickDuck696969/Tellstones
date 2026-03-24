package com.example.demo.model;
 
import jakarta.persistence.*; 
import jakarta.validation.constraints.NotBlank; 
import lombok.*; 
 
@Setter 
@Getter 
@RequiredArgsConstructor 
@AllArgsConstructor 
@Entity 
@Table(name = "category") 
public class Category { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 
 
    @NotBlank(message = "Tên là bắt buộc") 
    private String name; 
} 