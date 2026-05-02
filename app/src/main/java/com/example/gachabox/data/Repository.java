//Repository with CRUD
package com.example.gachabox.data;

import android.content.Context;

import com.example.gachabox.data.dao.InventoryDao;
import com.example.gachabox.data.dao.UserDao;
import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.data.entity.UserEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GachaRepository {

    private final UserDao userDao;
    private final InventoryDao inventoryDao;
    private final ExecutorService executorService;

    public GachaRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        userDao = database.userDao();
        inventoryDao = database.inventoryDao();
        executorService = Executors.newSingleThreadExecutor();
    }

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
            callback.onResult(user);
        });
    }

    public void getAllInventory(RepositoryInventoryCallback callback) {
        executorService.execute(() -> {
            List<InventoryEntity> items = inventoryDao.getAllItems();
            callback.onResult(items);
        });
    }

    public void addTokens(int amount, RepositoryActionCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user != null) {
                user.tokenBalance += amount;
                userDao.updateUser(user);
                callback.onComplete(true, "Tokens added.");
            } else {
                callback.onComplete(false, "User not found.");
            }
        });
    }

    public void spendOneToken(RepositoryActionCallback callback) {
        executorService.execute(() -> {
            UserEntity user = userDao.getUser();
            if (user != null && user.tokenBalance > 0) {
                user.tokenBalance -= 1;
                userDao.updateUser(user);
                callback.onComplete(true, "Token spent.");
            } else {
                callback.onComplete(false, "Not enough tokens.");
            }
        });
    }

    public void unlockItem(String itemId, RepositoryActionCallback callback) {
        executorService.execute(() -> {
            InventoryEntity item = inventoryDao.getItemById(itemId);

            if (item == null) {
                callback.onComplete(false, "Item not found.");
                return;
            }

            if (!item.unlocked) {
                item.unlocked = true;
                item.quantity = 1;
            } else {
                item.quantity += 1;
            }

            inventoryDao.updateItem(item);
            callback.onComplete(true, item.itemName + " added to inventory.");
        });
    }

    public void resetAllData(RepositoryActionCallback callback) {
        executorService.execute(() -> {
            userDao.deleteAllUsers();
            inventoryDao.deleteAllItems();

            userDao.insertUser(new UserEntity(1, 10));
            inventoryDao.insertAll(SeedData.defaultInventory());

            callback.onComplete(true, "Data reset complete.");
        });
    }

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
