package com.example.gachabox.logic;

import com.example.gachabox.model.Banner;
import com.example.gachabox.model.GachaItem;

import java.util.HashMap;
import java.util.Map;
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
 *
 * <h3>Banner support (Phase 4 — final content)</h3>
 * Each banner has its own pool of items. The {@link #pull(String)}
 * overload picks from a specific banner's pool. Item ids align with
 * the entries seeded into the inventory table by
 * {@link com.example.gachabox.data.SeedData}, so the Repository can
 * look up and unlock the correct row.
 */
public class GachaEngine {

    /**
     * Per-banner rarity buckets. Each entry: {id, displayName, drawableResName}.
     */
    private static final class BannerPool {
        final String[][] commons;
        final String[][] uncommons;
        final String[][] rares;
        final String[][] secrets;

        BannerPool(String[][] commons, String[][] uncommons,
                   String[][] rares, String[][] secrets) {
            this.commons = commons;
            this.uncommons = uncommons;
            this.rares = rares;
            this.secrets = secrets;
        }
    }

    private static final Map<String, BannerPool> POOLS = new HashMap<>();

    static {
        // ----- DOGS banner -----
        // 9 cards unchanged from teammate's art; Secret renamed to Godzilla
        // and points at the new dogs_secret_1 drawable.
        POOLS.put(Banner.DOGS.id, new BannerPool(
                new String[][] {
                        {"dogs_001", "Common 1",   "common_1"},
                        {"dogs_002", "Common 2",   "common_2"},
                        {"dogs_003", "Common 3",   "common_3"},
                        {"dogs_004", "Common 4",   "common_4"}
                },
                new String[][] {
                        {"dogs_005", "Uncommon 1", "uncommon_1"},
                        {"dogs_006", "Uncommon 2", "uncommon_2"},
                        {"dogs_007", "Uncommon 3", "uncommon_3"}
                },
                new String[][] {
                        {"dogs_008", "Rare 1",     "rare_1"},
                        {"dogs_009", "Rare 2",     "rare_2"}
                },
                new String[][] {
                        {"dogs_010", "Godzilla",   "dogs_secret_1"}
                }
        ));

        // ----- ANIME banner -----
        POOLS.put(Banner.ANIME.id, new BannerPool(
                new String[][] {
                        {"anime_001", "Saitama",    "anime_common_1"},
                        {"anime_002", "Naruto",     "anime_common_2"},
                        {"anime_003", "Random Mob", "anime_common_3"},
                        {"anime_004", "Eren",       "anime_common_4"}
                },
                new String[][] {
                        {"anime_005", "Kaneki",     "anime_uncommon_1"},
                        {"anime_006", "Tanjiro",    "anime_uncommon_2"},
                        {"anime_007", "Kurapika",   "anime_uncommon_3"}
                },
                new String[][] {
                        {"anime_008", "Sukuna",     "anime_rare_1"},
                        {"anime_009", "Gojo",       "anime_rare_2"}
                },
                new String[][] {
                        {"anime_010", "Tsuna",      "anime_secret_1"}
                }
        ));

        // ----- FOOD banner -----
        POOLS.put(Banner.FOOD.id, new BannerPool(
                new String[][] {
                        {"food_001", "Wonton",            "food_common_1"},
                        {"food_002", "Chicken Noodles",   "food_common_2"},
                        {"food_003", "Rice Cake Noodles", "food_common_3"},
                        {"food_004", "Ramen",             "food_common_4"}
                },
                new String[][] {
                        {"food_005", "Pasta",             "food_uncommon_1"},
                        {"food_006", "SE Asian Dish",     "food_uncommon_2"},
                        {"food_007", "Sausage",           "food_uncommon_3"}
                },
                new String[][] {
                        {"food_008", "Mapo Tofu",         "food_rare_1"},
                        {"food_009", "Steak",             "food_rare_2"}
                },
                new String[][] {
                        {"food_010", "Lanzhou Noodles",   "food_secret_1"}
                }
        ));
    }

    private final Random random;

    public GachaEngine() {
        this(new Random());
    }

    /** Visible-for-testing: lets unit tests inject a seeded RNG. */
    public GachaEngine(Random random) {
        this.random = random;
    }

    /**
     * Convenience overload that pulls from the default banner.
     * Equivalent to {@code pull(Banner.DOGS.id)}.
     */
    public GachaItem pull() {
        return pull(Banner.DOGS.id);
    }

    /**
     * Pull a card from the specified banner.
     *
     * @param bannerId one of {@code Banner.DOGS.id} / {@code Banner.ANIME.id} /
     *                 {@code Banner.FOOD.id}
     * @throws IllegalArgumentException if the banner has no registered pool
     */
    public GachaItem pull(String bannerId) {
        BannerPool pool = POOLS.get(bannerId);
        if (pool == null) {
            throw new IllegalArgumentException("No pool registered for banner: " + bannerId);
        }

        int roll = random.nextInt(100);  // 0..99 inclusive

        if (roll < 10) {
            return pickFrom(pool.secrets, "Secret", bannerId);
        } else if (roll < 30) {
            return pickFrom(pool.rares, "Rare", bannerId);
        } else if (roll < 60) {
            return pickFrom(pool.uncommons, "Uncommon", bannerId);
        } else {
            return pickFrom(pool.commons, "Common", bannerId);
        }
    }

    private GachaItem pickFrom(String[][] pool, String rarity, String bannerId) {
        String[] picked = pool[random.nextInt(pool.length)];
        return new GachaItem(picked[0], picked[1], rarity, picked[2], bannerId);
    }
}
