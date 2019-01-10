package com.example.lesou;


public class DetailMenuItem {
    private String name;
    private int imageId;

    public DetailMenuItem(String name, int imageId) {
        this.name = name;
        this.imageId = imageId;
    }

    public String getName() {
        return name;
    }

    public int getImageId() {
        return imageId;
    }
}
