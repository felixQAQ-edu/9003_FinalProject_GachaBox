package com.example.gachabox.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "inventory")
public class InventoryEntity {

    @PrimaryKey
    @NonNull
    public String itemId;

    public String itemName;
    public String rarity;
    public boolean unlocked;
    public int quantity;
    public String imageResName;
    public String silhouetteResName;

    public InventoryEntity(@NonNull String itemId,
                           String itemName,
                           String rarity,
                           boolean unlocked,
                           int quantity,
                           String imageResName,
                           String silhouetteResName) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.unlocked = unlocked;
        this.quantity = quantity;
        this.imageResName = imageResName;
        this.silhouetteResName = silhouetteResName;
    }
}
