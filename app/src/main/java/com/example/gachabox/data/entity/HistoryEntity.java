package com.example.gachabox.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * One row per gacha pull. Stored in the "history" table.
 *
 * Card metadata (name / rarity / image / banner) is denormalised — copied
 * onto the history row at write time rather than referenced via itemId —
 * so the history display doesn't need a JOIN against the inventory table
 * on every read, and old history entries survive any future rename or
 * rebalance of the card catalogue.
 */
@Entity(tableName = "history")
public class HistoryEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public String itemId;
    public String itemName;
    public String rarity;
    public String imageResName;
    public String bannerId;
    public long timestamp;   // System.currentTimeMillis()

    public HistoryEntity(String itemId,
                         String itemName,
                         String rarity,
                         String imageResName,
                         String bannerId,
                         long timestamp) {
        this.itemId = itemId;
        this.itemName = itemName;
        this.rarity = rarity;
        this.imageResName = imageResName;
        this.bannerId = bannerId;
        this.timestamp = timestamp;
    }
}
