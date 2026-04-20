package com.example.gachabox.model;

public class GachaItem {
    private String name;
    private String rarity;

    // 构造函数
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

    // 为了方便打印测试，重写 toString
    @Override
    public String toString() {
        return "[" + rarity + "] " + name;
    }
}