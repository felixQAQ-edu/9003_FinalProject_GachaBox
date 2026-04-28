package com.example.gachabox.model;

public class GachaItem {
    private String name;
    private String rarity;

    public GachaItem(String name, String rarity) {
        this.name = name;
        this.rarity = rarity;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getRarity() {
        return rarity;
    }

    @Override
    public String toString() {
        return "[" + rarity + "] " + name;
    }
}