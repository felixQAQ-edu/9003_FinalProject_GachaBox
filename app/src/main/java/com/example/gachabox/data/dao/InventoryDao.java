package com.example.gachabox.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gachabox.data.entity.InventoryEntity;

import java.util.List;

@Dao
public interface InventoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertItem(InventoryEntity item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<InventoryEntity> items);

    @Query("SELECT * FROM inventory ORDER BY itemId ASC")
    List<InventoryEntity> getAllItems();

    /**
     * Items from one banner only, ordered by id so commons come first,
     * then uncommons, etc. Phase 2 UI uses this to render per-banner
     * galleries.
     */
    @Query("SELECT * FROM inventory WHERE bannerId = :bannerId ORDER BY itemId ASC")
    List<InventoryEntity> getItemsByBanner(String bannerId);

    @Query("SELECT * FROM inventory WHERE unlocked = 1 ORDER BY itemId ASC")
    List<InventoryEntity> getUnlockedItems();

    @Query("SELECT * FROM inventory WHERE itemId = :itemId LIMIT 1")
    InventoryEntity getItemById(String itemId);

    @Update
    void updateItem(InventoryEntity item);

    @Delete
    void deleteItem(InventoryEntity item);

    @Query("DELETE FROM inventory")
    void deleteAllItems();
}
