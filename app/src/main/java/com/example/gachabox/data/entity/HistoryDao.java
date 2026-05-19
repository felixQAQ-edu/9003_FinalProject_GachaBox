package com.example.gachabox.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.gachabox.data.entity.HistoryEntity;

import java.util.List;

@Dao
public interface HistoryDao {

    @Insert
    void insertHistory(HistoryEntity entity);

    /**
     * Most recent pulls first. Capped at 50 to keep the dialog snappy
     * even if a heavy tester has thousands of pulls. A real product
     * would paginate or scroll-load instead.
     */
    @Query("SELECT * FROM history ORDER BY timestamp DESC LIMIT 50")
    List<HistoryEntity> getRecentHistory();

    @Query("SELECT COUNT(*) FROM history")
    int getTotalCount();

    @Query("DELETE FROM history")
    void deleteAll();
}
