package com.example.gachabox.data;

import com.example.gachabox.data.entity.InventoryEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Initial inventory data inserted into the Room DB on first launch.
 *
 * IDs ("001"..."010") match the order the cards appear in the gallery.
 * Each item starts locked (unlocked = false, quantity = 0); the gallery
 * shows the silhouette / locked overlay until the user pulls the
 * matching card.
 *
 * NOTE: GachaEngine currently produces only 3 distinct names
 * ("Golden Retriever" / "Husky" / "Godzilla") with its own IDs.
 * Aligning engine output with these seed entries is part of wave 2.
 */
public final class SeedData {

    private SeedData() {
        // utility class — no instances
    }

    public static List<InventoryEntity> defaultInventory() {
        return new ArrayList<>(Arrays.asList(
                new InventoryEntity("001", "Common 1",   "Common",   false, 0, "common_1",   "locked_overlay"),
                new InventoryEntity("002", "Common 2",   "Common",   false, 0, "common_2",   "locked_overlay"),
                new InventoryEntity("003", "Common 3",   "Common",   false, 0, "common_3",   "locked_overlay"),
                new InventoryEntity("004", "Common 4",   "Common",   false, 0, "common_4",   "locked_overlay"),
                new InventoryEntity("005", "Uncommon 1", "Uncommon", false, 0, "uncommon_1", "locked_overlay"),
                new InventoryEntity("006", "Uncommon 2", "Uncommon", false, 0, "uncommon_2", "locked_overlay"),
                new InventoryEntity("007", "Uncommon 3", "Uncommon", false, 0, "uncommon_3", "locked_overlay"),
                new InventoryEntity("008", "Rare 1",     "Rare",     false, 0, "rare_1",     "locked_overlay"),
                new InventoryEntity("009", "Rare 2",     "Rare",     false, 0, "rare_2",     "locked_overlay"),
                new InventoryEntity("010", "Secret 1",   "Secret",   false, 0, "secret_1",   "locked_overlay")
        ));
    }
}
