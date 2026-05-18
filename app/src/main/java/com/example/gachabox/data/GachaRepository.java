package com.example.gachabox.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.gachabox.data.dao.InventoryDao;
import com.example.gachabox.data.dao.UserDao;
import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.data.entity.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Single source of truth for User + Inventory data.
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
    private final ExecutorService executorService;
    private final Handler mainHandler;

    private GachaRepository(Context appContext) {
        AppDatabase database = AppDatabase.getInstance(appContext);
        userDao = database.userDao();
        inventoryDao = database.inventoryDao();
        executorService = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    // ---------- Public API ----------

    /**
     * Seed the database on first launch. Idempotent: if a user row or
     * inventory rows already exist, nothing is overwritten.
     * Fire-and-forget — no callback because the UI doesn't wait on it.
     */
    public void initializeData() {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user == null) {
                userDao.insertUser(new UserEntity(1, 10));
            }

            List<InventoryEntity> inventory = inventoryDao.getAllItems();
            if (inventory == null || inventory.isEmpty()) {
                inventoryDao.insertAll(SeedData.defaultInventory());
            }
        });
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

    public void resetAllData(RepositoryActionCallback callback) {
        executorService.execute(() -> {
            userDao.deleteAllUsers();
            inventoryDao.deleteAllItems();

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

    public interface RepositoryActionCallback {
        void onComplete(boolean success, String message);
    }
}
