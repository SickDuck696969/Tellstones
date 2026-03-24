package com.example.demo.model;
 
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Setter 
@Getter 
@NoArgsConstructor
@AllArgsConstructor 
@Entity 
@Table(name = "account") 
public class Account { 
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id; 

    private LocalDate birthday;
    private String name;
    private String email;
    private String password;
    private String image_string;
    private Boolean isAdmin = false;
    private double credit;
}