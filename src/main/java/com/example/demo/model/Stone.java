package com.example.demo.model;
 
public class Stone { 
    private Long id; 
    private String icon;
    private Boolean faceup;

    public Stone() {
    }

    public Stone (int id, String icon, Boolean faceup) {
        this.id = (long) id;
        this.icon = icon;
        this.faceup = faceup;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Boolean getFaceup() {
        return faceup;
    }

    public void setFaceup(Boolean faceup) {
        this.faceup = faceup;
    }
}
