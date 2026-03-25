package com.example.demo.model;
 
import lombok.*;

@Setter 
@Getter 
@NoArgsConstructor
public class Stone { 
    private Long id; 
    private String icon;
    private Boolean faceup;

    public Stone (int id, String icon, Boolean faceup) {
        this.id = (long) id;
        this.icon = icon;
        this.faceup = faceup;
    }
}
