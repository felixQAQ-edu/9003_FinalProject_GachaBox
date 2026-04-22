package com.example.gachabox.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    public int userId;

    public int tokenBalance;

    public UserEntity(int userId, int tokenBalance) {
        this.userId = userId;
        this.tokenBalance = tokenBalance;
    }
}
