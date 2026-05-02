package com.example.gachabox.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.gachabox.data.entity.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE userId = 1 LIMIT 1")
    UserEntity getUser();

    @Update
    void updateUser(UserEntity user);

    @Query("DELETE FROM users")
    void deleteAllUsers();
}
