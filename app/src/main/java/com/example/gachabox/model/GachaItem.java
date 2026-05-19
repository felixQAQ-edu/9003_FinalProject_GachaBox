package com.example.gachabox.model;

/**
 * Result of a single gacha pull.
 *
 * Pure data object — no Android or Room dependencies. The GachaEngine
 * produces this; the Repository converts it into a Room InventoryEntity
 * update when persisting.
 *
 * <p>Fields:
 * <ul>
 *   <li>{@code id} — matches the itemId in SeedData / InventoryEntity,
 *       so the Repository can look up the correct row to unlock.</li>
 *   <li>{@code name} — display name shown to the user.</li>
 *   <li>{@code rarity} — "Common" / "Uncommon" / "Rare" / "Secret".</li>
 *   <li>{@code imageResName} — drawable resource name (e.g. "common_1")
 *       used by MainActivity to display the card front.</li>
 *   <li>{@code bannerId} — which banner this item belongs to (e.g. "dogs",
 *       "anime", "food"). Use {@link Banner} constants to compare.</li>
 * </ul>
 */
public class GachaItem {

    private final String id;
    private final String name;
    private final String rarity;
    private final String imageResName;
    private final String bannerId;

    public GachaItem(String id, String name, String rarity,
                     String imageResName, String bannerId) {
        this.id = id;
        this.name = name;
        this.rarity = rarity;
        this.imageResName = imageResName;
        this.bannerId = bannerId;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getRarity() {
        return rarity;
    }

    public String getImageResName() {
        return imageResName;
    }

    public String getBannerId() {
        return bannerId;
    }

    @Override
    public String toString() {
        return "[" + bannerId + "/" + rarity + "] " + name;
    }
}
