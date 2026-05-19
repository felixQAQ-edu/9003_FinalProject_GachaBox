package com.example.gachabox.model;

import java.util.Arrays;
import java.util.List;

/**
 * A Banner represents one themed gacha pool (e.g. "Doggos", "Anime", "Foodies").
 * <p>
 * Banners are defined as code constants, not stored in the database. The database
 * only stores the bannerId on each InventoryEntity / HistoryEntity row so we can
 * group items and history by banner at query time.
 * <p>
 * To add a new banner: declare a new public static final Banner here, add it to
 * the ALL list, and register a matching pool in GachaEngine + seed entries in
 * SeedData.
 */
public class Banner {

    public final String id;          // Stable key used in DB columns. Never change.
    public final String displayName; // Shown in UI tabs / dialogs.
    public final int themeColor;     // ARGB hex int for banner tint (used in Phase 2 UI).

    public Banner(String id, String displayName, int themeColor) {
        this.id = id;
        this.displayName = displayName;
        this.themeColor = themeColor;
    }

    // ----- Banner catalog -------------------------------------------------

    public static final Banner DOGS  = new Banner("dogs",  "Doggos",  0xFF7EC8FF); // sky blue
    public static final Banner ANIME = new Banner("anime", "Anime",   0xFFC08CFF); // soft purple
    public static final Banner FOOD  = new Banner("food",  "Foodies", 0xFFFFD86B); // warm yellow

    public static final List<Banner> ALL = Arrays.asList(DOGS, ANIME, FOOD);

    /**
     * Look up a banner by its id. Returns DOGS as a safe fallback if the id is
     * unknown (e.g. legacy data or a typo). Never returns null.
     */
    public static Banner byId(String id) {
        if (id != null) {
            for (Banner b : ALL) {
                if (b.id.equals(id)) return b;
            }
        }
        return DOGS;
    }
}
