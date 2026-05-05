package com.example.gachabox.logic;

import com.example.gachabox.model.GachaItem;

import java.util.Random;

/**
 * Pure gacha logic. No Android, no Room — fully testable on the JVM.
 *
 * <h3>Probabilities (match dialog_info.xml exactly)</h3>
 * <pre>
 *   Common:   40%
 *   Uncommon: 30%
 *   Rare:     20%
 *   Secret:   10%
 * </pre>
 * Within each rarity bucket, a card is picked uniformly at random.
 * Item ids ("001"–"010") align with SeedData entries so the
 * Repository can find the right inventory row to unlock, and
 * imageResName entries match drawable filenames so MainActivity can
 * display the correct card front.
 *
 * <h3>Note</h3>
 * The previous "pity" mechanic (forced Secret every 30 pulls) was
 * removed in this version because it boosted the effective Secret
 * rate above the 10% shown to users, which violated the displayed
 * rates contract. If pity is desired, it should be advertised in
 * dialog_info too.
 */
public class GachaEngine {

    // Each entry: { id, displayName, drawableResName }
    // ids and names align with SeedData; resNames align with the
    // PNGs in res/drawable.

    private static final String[][] COMMONS = {
            {"001", "Common 1", "common_1"},
            {"002", "Common 2", "common_2"},
            {"003", "Common 3", "common_3"},
            {"004", "Common 4", "common_4"}
    };

    private static final String[][] UNCOMMONS = {
            {"005", "Uncommon 1", "uncommon_1"},
            {"006", "Uncommon 2", "uncommon_2"},
            {"007", "Uncommon 3", "uncommon_3"}
    };

    private static final String[][] RARES = {
            {"008", "Rare 1", "rare_1"},
            {"009", "Rare 2", "rare_2"}
    };

    private static final String[][] SECRETS = {
            {"010", "Secret 1", "secret_1"}
    };

    private final Random random;

    public GachaEngine() {
        this(new Random());
    }

    /** Visible-for-testing: lets unit tests inject a seeded RNG. */
    public GachaEngine(Random random) {
        this.random = random;
    }

    public GachaItem pull() {
        int roll = random.nextInt(100);  // 0..99 inclusive

        if (roll < 10) {
            return pickFrom(SECRETS, "Secret");
        } else if (roll < 30) {
            return pickFrom(RARES, "Rare");
        } else if (roll < 60) {
            return pickFrom(UNCOMMONS, "Uncommon");
        } else {
            return pickFrom(COMMONS, "Common");
        }
    }

    private GachaItem pickFrom(String[][] pool, String rarity) {
        String[] picked = pool[random.nextInt(pool.length)];
        return new GachaItem(picked[0], picked[1], rarity, picked[2]);
    }
}