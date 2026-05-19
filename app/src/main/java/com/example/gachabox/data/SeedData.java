package com.example.gachabox.data;

import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.model.Banner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initial inventory data inserted into the Room DB on first launch.
 *
 * <p>Item ids use a {@code <bannerId>_<NNN>} scheme so they stay globally
 * unique even though each banner has its own "Common 1", "Common 2", ...
 * The {@code bannerId} column is denormalised onto each row so banner
 * filtering is a simple WHERE clause instead of a string-prefix scan.</p>
 *
 * <p>Each item starts locked (unlocked = false, quantity = 0); the
 * gallery shows the silhouette / locked overlay until the user pulls
 * the matching card.</p>
 *
 * <h3>Phase 4 final content</h3>
 * Anime and food banners now use real card names / artwork. The dogs
 * Secret was renamed from "Secret 1" to "Godzilla" with new art. Item
 * ids did NOT change — this means existing user inventory rows can be
 * content-synced in place via {@link GachaRepository#initializeData()}
 * without wiping unlock progress.
 */
public final class SeedData {

    private SeedData() {
        // utility class — no instances
    }

    /** All seed items across all banners. */
    public static List<InventoryEntity> defaultInventory() {
        List<InventoryEntity> all = new ArrayList<>();
        all.addAll(dogsBanner());
        all.addAll(animeBanner());
        all.addAll(foodBanner());
        return all;
    }

    // -------- Per-banner seeds --------

    public static List<InventoryEntity> dogsBanner() {
        String b = Banner.DOGS.id;
        // Note: dogs_010 Secret renamed from "Secret 1" to "Godzilla",
        // with new dedicated drawable. Other 9 items unchanged.
        return new ArrayList<>(Arrays.asList(
                new InventoryEntity("dogs_001", "Common 1",   "Common",   false, 0, "common_1",       "locked_overlay", b),
                new InventoryEntity("dogs_002", "Common 2",   "Common",   false, 0, "common_2",       "locked_overlay", b),
                new InventoryEntity("dogs_003", "Common 3",   "Common",   false, 0, "common_3",       "locked_overlay", b),
                new InventoryEntity("dogs_004", "Common 4",   "Common",   false, 0, "common_4",       "locked_overlay", b),
                new InventoryEntity("dogs_005", "Uncommon 1", "Uncommon", false, 0, "uncommon_1",     "locked_overlay", b),
                new InventoryEntity("dogs_006", "Uncommon 2", "Uncommon", false, 0, "uncommon_2",     "locked_overlay", b),
                new InventoryEntity("dogs_007", "Uncommon 3", "Uncommon", false, 0, "uncommon_3",     "locked_overlay", b),
                new InventoryEntity("dogs_008", "Rare 1",     "Rare",     false, 0, "rare_1",         "locked_overlay", b),
                new InventoryEntity("dogs_009", "Rare 2",     "Rare",     false, 0, "rare_2",         "locked_overlay", b),
                new InventoryEntity("dogs_010", "Godzilla",   "Secret",   false, 0, "dogs_secret_1",  "locked_overlay", b)
        ));
    }

    public static List<InventoryEntity> animeBanner() {
        String b = Banner.ANIME.id;
        return new ArrayList<>(Arrays.asList(
                new InventoryEntity("anime_001", "Saitama",    "Common",   false, 0, "anime_common_1",   "locked_overlay", b),
                new InventoryEntity("anime_002", "Naruto",     "Common",   false, 0, "anime_common_2",   "locked_overlay", b),
                new InventoryEntity("anime_003", "Random Mob", "Common",   false, 0, "anime_common_3",   "locked_overlay", b),
                new InventoryEntity("anime_004", "Eren",       "Common",   false, 0, "anime_common_4",   "locked_overlay", b),
                new InventoryEntity("anime_005", "Kaneki",     "Uncommon", false, 0, "anime_uncommon_1", "locked_overlay", b),
                new InventoryEntity("anime_006", "Tanjiro",    "Uncommon", false, 0, "anime_uncommon_2", "locked_overlay", b),
                new InventoryEntity("anime_007", "Kurapika",   "Uncommon", false, 0, "anime_uncommon_3", "locked_overlay", b),
                new InventoryEntity("anime_008", "Sukuna",     "Rare",     false, 0, "anime_rare_1",     "locked_overlay", b),
                new InventoryEntity("anime_009", "Gojo",       "Rare",     false, 0, "anime_rare_2",     "locked_overlay", b),
                new InventoryEntity("anime_010", "Tsuna",      "Secret",   false, 0, "anime_secret_1",   "locked_overlay", b)
        ));
    }

    public static List<InventoryEntity> foodBanner() {
        String b = Banner.FOOD.id;
        return new ArrayList<>(Arrays.asList(
                new InventoryEntity("food_001", "Wonton",            "Common",   false, 0, "food_common_1",   "locked_overlay", b),
                new InventoryEntity("food_002", "Chicken Noodles",   "Common",   false, 0, "food_common_2",   "locked_overlay", b),
                new InventoryEntity("food_003", "Rice Cake Noodles", "Common",   false, 0, "food_common_3",   "locked_overlay", b),
                new InventoryEntity("food_004", "Ramen",             "Common",   false, 0, "food_common_4",   "locked_overlay", b),
                new InventoryEntity("food_005", "Pasta",             "Uncommon", false, 0, "food_uncommon_1", "locked_overlay", b),
                new InventoryEntity("food_006", "SE Asian Dish",     "Uncommon", false, 0, "food_uncommon_2", "locked_overlay", b),
                new InventoryEntity("food_007", "Sausage",           "Uncommon", false, 0, "food_uncommon_3", "locked_overlay", b),
                new InventoryEntity("food_008", "Mapo Tofu",         "Rare",     false, 0, "food_rare_1",     "locked_overlay", b),
                new InventoryEntity("food_009", "Steak",             "Rare",     false, 0, "food_rare_2",     "locked_overlay", b),
                new InventoryEntity("food_010", "Lanzhou Noodles",   "Secret",   false, 0, "food_secret_1",   "locked_overlay", b)
        ));
    }
}
