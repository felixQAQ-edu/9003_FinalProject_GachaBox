package com.example.gachabox.data.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One row per collectable card in the catalog.
 *
 * <p>The primary key is {@code itemId}, which is globally unique across
 * all banners — we use a prefix scheme (e.g. "dogs_001", "anime_001",
 * "food_001"). The {@code bannerId} column is stored separately to make
 * banner-filtered queries cheap.</p>
 *
 * <p>{@code unlocked} starts false; turns true when the user pulls the
 * card for the first time. {@code quantity} tracks duplicates (how many
 * copies the user owns).</p>
 */
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
    public String bannerId;

    public InventoryEntity(@NonNull String itemId,
                           String itemName,
                           String rarity,
                           boolean unlocked,
                           int quantity,
                           String imageResName,
                           String silhouetteResName,
                           String bannerId) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.unlocked = unlocked;
        this.quantity = quantity;
        this.imageResName = imageResName;
        this.silhouetteResName = silhouetteResName;
        this.bannerId = bannerId;
    }
}
