package com.example.gachabox.gallery;

/**
 * Data model for a single gallery card item.
 *
 * This class is used by the gallery screen to represent
 * one collectible card, including:
 * - its image resource
 * - its display name
 * - its rarity
 * - whether it has been unlocked
 *
 * TODO Interface 8:
 * In the current version, GalleryItem objects are created
 * from static mock data.
 * Later, these fields should be populated from the real
 * collection / gallery data source.
 */
public class GalleryItem {

    // Drawable resource ID for the card image
    private final int imageResId;

    // Display name of the card
    private final String name;

    // Card rarity, for example: Common / Uncommon / Rare / Secret
    private final String rarity;

    // Whether the card has been unlocked by the user
    private final boolean unlocked;

    /**
     * Creates a new gallery item.
     *
     * @param imageResId drawable resource ID of the card image
     * @param name display name of the card
     * @param rarity rarity level of the card
     * @param unlocked whether the card is unlocked
     */
    public GalleryItem(int imageResId, String name, String rarity, boolean unlocked) {
        this.imageResId = imageResId;
        this.name = name;
        this.rarity = rarity;
        this.unlocked = unlocked;
    }

    /**
     * Returns the drawable resource ID of the card image.
     */
    public int getImageResId() {
        return imageResId;
    }

    /**
     * Returns the display name of the card.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the rarity of the card.
     */
    public String getRarity() {
        return rarity;
    }

    /**
     * Returns whether the card is unlocked.
     */
    public boolean isUnlocked() {
        return unlocked;
    }
}