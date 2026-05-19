package com.example.gachabox.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gachabox.data.dao.HistoryDao;
import com.example.gachabox.data.dao.InventoryDao;
import com.example.gachabox.data.dao.UserDao;
import com.example.gachabox.data.entity.HistoryEntity;
import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.data.entity.UserEntity;

/**
 * Room database for GachaBox.
 * Binds the User, Inventory and History entities and exposes their DAOs.
 *
 * Use AppDatabase.getInstance(context) to obtain the singleton.
 *
 * <h3>Versioning</h3>
 * v1: User + Inventory.
 * v2: + History (adds the "history" table).
 * v3: + bannerId columns on Inventory and History (multi-banner support).
 *     No migration provided —
 *     {@link androidx.room.RoomDatabase.Builder#fallbackToDestructiveMigration}
 *     will recreate the database on schema change. Existing test data
 *     (tokens, unlock state, history) is wiped on first launch with v3.
 */
@Database(
        entities = {UserEntity.class, InventoryEntity.class, HistoryEntity.class},
        version = 3,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract InventoryDao inventoryDao();
    public abstract HistoryDao historyDao();

    private static final String DB_NAME = "gachabox.db";
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    DB_NAME
                            )
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
