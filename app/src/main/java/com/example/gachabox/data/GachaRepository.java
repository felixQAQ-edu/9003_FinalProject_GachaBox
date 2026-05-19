package com.example.gachabox.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.gachabox.data.dao.HistoryDao;
import com.example.gachabox.data.dao.InventoryDao;
import com.example.gachabox.data.dao.UserDao;
import com.example.gachabox.data.entity.HistoryEntity;
import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.data.entity.UserEntity;
import com.example.gachabox.model.Banner;
import com.example.gachabox.model.GachaItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for User + Inventory + History data.
 *
 * <h3>Lifecycle</h3>
 * Singleton — never instantiate with {@code new}. Always obtain via
 * {@link #getInstance(Context)}. The instance is held for the lifetime
 * of the process and shares one background executor across all callers.
 *
 * <h3>Threading contract</h3>
 * <ul>
 *   <li>All DAO operations run on a private single-thread executor.
 *       Single-thread is intentional: it serializes writes, so we
 *       don't need extra locking, and Room is happy.</li>
 *   <li>All callback methods fire on the Android <b>main thread</b>.
 *       Callers can update the UI directly inside callbacks without
 *       wrapping in {@code runOnUiThread}.</li>
 * </ul>
 */
public class GachaRepository {

    // ---------- Singleton ----------

    private static volatile GachaRepository INSTANCE;

    public static GachaRepository getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (GachaRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new GachaRepository(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    // ---------- Instance state ----------

    private final UserDao userDao;
    private final InventoryDao inventoryDao;
    private final HistoryDao historyDao;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private GachaRepository(Context appContext) {
        AppDatabase database = AppDatabase.getInstance(appContext);
        userDao = database.userDao();
        inventoryDao = database.inventoryDao();
        historyDao = database.historyDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ---------- Public API ----------

    /**
     * Seed and content-sync the database on launch.
     *
     * <h4>Per-banner seeding</h4>
     * Each banner's seed rows are inserted only if no rows exist for it
     * yet. Adding a new banner in a later phase doesn't wipe or duplicate
     * existing banners.
     *
     * <h4>Content sync (Phase 4)</h4>
     * After seeding, walks every seed row and rewrites the matching DB
     * row's {@code itemName} / {@code imageResName} / {@code silhouetteResName}
     * to whatever {@link SeedData} currently says — while preserving
     * {@code unlocked} and {@code quantity}. That means when card names
     * or artwork change between releases (e.g. "Hero 1" → "Saitama" with
     * new art), the user's unlock progress survives, but they see the
     * new content immediately on next launch.
     *
     * <p>Fire-and-forget — no callback because the UI doesn't wait on it.</p>
     */
    public void initializeData() {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user == null) {
                userDao.insertUser(new UserEntity(1, 10));
            }

            seedBannerIfEmpty(Banner.DOGS.id,  SeedData.dogsBanner());
            seedBannerIfEmpty(Banner.ANIME.id, SeedData.animeBanner());
            seedBannerIfEmpty(Banner.FOOD.id,  SeedData.foodBanner());

            // After seeding, push the latest names / artwork from SeedData
            // onto pre-existing rows whose IDs already exist in the DB.
            syncBannerContent(SeedData.dogsBanner());
            syncBannerContent(SeedData.animeBanner());
            syncBannerContent(SeedData.foodBanner());
        });
    }

    /** Insert seed rows for a banner only if none exist yet. */
    private void seedBannerIfEmpty(String bannerId, List<InventoryEntity> seeds) {
        List<InventoryEntity> existing = inventoryDao.getItemsByBanner(bannerId);
        if (existing == null || existing.isEmpty()) {
            inventoryDao.insertAll(seeds);
        }
    }

    /**
     * For each seed row, find the matching DB row by itemId and rewrite
     * the content fields (name + image + silhouette) if they differ.
     * Preserves unlock state and quantity — only display content changes.
     */
    private void syncBannerContent(List<InventoryEntity> seeds) {
        for (InventoryEntity seed : seeds) {
            InventoryEntity existing = inventoryDao.getItemById(seed.itemId);
            if (existing == null) continue;  // defensive — shouldn't happen post-seed

            boolean changed = false;
            if (!textEquals(existing.itemName, seed.itemName)) {
                existing.itemName = seed.itemName;
                changed = true;
            }
            if (!textEquals(existing.imageResName, seed.imageResName)) {
                existing.imageResName = seed.imageResName;
                changed = true;
            }
            if (!textEquals(existing.silhouetteResName, seed.silhouetteResName)) {
                existing.silhouetteResName = seed.silhouetteResName;
                changed = true;
            }

            if (changed) {
                inventoryDao.updateItem(existing);
            }
        }
    }

    private static boolean textEquals(String a, String b) {
        return a == null ? b == null : a.equals(b);
    }

    public void getUser(RepositoryUserCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            mainHandler.post(() -> callback.onResult(user));
        });
    }

    public void getAllInventory(RepositoryInventoryCallback callback) {
        executorService.execute(() -> {
            List<InventoryEntity> items = inventoryDao.getAllItems();
            mainHandler.post(() -> callback.onResult(items));
        });
    }

    /**
     * Inventory rows for a single banner. Phase 2's banner-tab UI uses
     * this to render per-banner galleries without mixing items across
     * banners. The order matches getAllInventory (by itemId ASC).
     */
    public void getInventoryByBanner(String bannerId, RepositoryInventoryCallback callback) {
        executorService.execute(() -> {
            List<InventoryEntity> items = inventoryDao.getItemsByBanner(bannerId);
            mainHandler.post(() -> callback.onResult(items));
        });
    }

    public void addTokens(int amount, RepositoryActionCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user != null) {
                user.tokenBalance += amount;
                userDao.updateUser(user);
                mainHandler.post(() -> callback.onComplete(true, "Tokens added."));
            } else {
                mainHandler.post(() -> callback.onComplete(false, "User not found."));
            }
        });
    }

    public void spendOneToken(RepositoryActionCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user != null && user.tokenBalance > 0) {
                user.tokenBalance -= 1;
                userDao.updateUser(user);
                mainHandler.post(() -> callback.onComplete(true, "Token spent."));
            } else {
                mainHandler.post(() -> callback.onComplete(false, "Not enough tokens."));
            }
        });
    }

    public void unlockItem(String itemId, RepositoryActionCallback callback) {
        executorService.execute(() -> {
            InventoryEntity item = inventoryDao.getItemById(itemId);

            if (item == null) {
                mainHandler.post(() -> callback.onComplete(false, "Item not found."));
                return;
            }

            // Distinguish first-time unlock from a duplicate pull so the
            // UI can show different feedback (e.g. "New!" toast vs
            // "Duplicate (×N)" toast).
            boolean wasNew = !item.unlocked;
            if (wasNew) {
                item.unlocked = true;
                item.quantity = 1;
            } else {
                item.quantity += 1;
            }

            inventoryDao.updateItem(item);

            // Capture values now so the lambda doesn't depend on the
            // mutable entity fields at dispatch time.
            String name = item.itemName;
            int quantity = item.quantity;
            String message = wasNew
                    ? "New! " + name + " unlocked."
                    : "Duplicate: " + name + " (×" + quantity + ")";

            mainHandler.post(() -> callback.onComplete(true, message));
        });
    }

    /**
     * Append a row to the history table. Caller passes the pull result;
     * we stamp it with the current wall-clock time and pick up the
     * bannerId from the GachaItem.
     */
    public void recordHistory(GachaItem pulledItem, RepositoryActionCallback callback) {
        executorService.execute(() -> {
            HistoryEntity entry = new HistoryEntity(
                    pulledItem.getId(),
                    pulledItem.getName(),
                    pulledItem.getRarity(),
                    pulledItem.getImageResName(),
                    pulledItem.getBannerId(),
                    System.currentTimeMillis()
            );
            historyDao.insertHistory(entry);
            mainHandler.post(() -> callback.onComplete(true, "History recorded."));
        });
    }

    /**
     * Fetch the 50 most recent pulls, newest first. Returns an empty
     * list (never null) if the table is empty.
     */
    public void getHistory(RepositoryHistoryCallback callback) {
        executorService.execute(() -> {
            List<HistoryEntity> history = historyDao.getRecentHistory();
            mainHandler.post(() -> callback.onResult(history));
        });
    }

    public void resetAllData(RepositoryActionCallback callback) {
        executorService.execute(() -> {
            userDao.deleteAllUsers();
            inventoryDao.deleteAllItems();
            historyDao.deleteAll();

            userDao.insertUser(new UserEntity(1, 10));
            inventoryDao.insertAll(SeedData.defaultInventory());

            mainHandler.post(() -> callback.onComplete(true, "Data reset complete."));
        });
    }

    // ---------- Callback interfaces ----------

    public interface RepositoryUserCallback {
        void onResult(UserEntity user);
    }

    public interface RepositoryInventoryCallback {
        void onResult(List<InventoryEntity> inventory);
    }

    public interface RepositoryHistoryCallback {
        void onResult(List<HistoryEntity> history);
    }

    public interface RepositoryActionCallback {
        void onComplete(boolean success, String message);
    }
}
