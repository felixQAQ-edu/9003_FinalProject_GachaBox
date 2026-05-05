package com.example.gachabox.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.gachabox.data.dao.InventoryDao;
import com.example.gachabox.data.dao.UserDao;
import com.example.gachabox.data.entity.InventoryEntity;
import com.example.gachabox.data.entity.UserEntity;

/**
 * Room database for GachaBox.
 * Binds the User and Inventory entities and exposes their DAOs.
 *
 * Use AppDatabase.getInstance(context) to obtain the singleton.
 */
@Database(
        entities = {UserEntity.class, InventoryEntity.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    public abstract UserDao userDao();
    public abstract InventoryDao inventoryDao();

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
                            // Dev convenience: if the schema changes without a
                            // proper Migration, wipe and recreate instead of
                            // crashing. Fine for a school project; remove for
                            // any real release.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
