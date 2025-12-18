package com.example.nutriai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {
    @PrimaryKey
    public int uid;

    public String username;
    public String fullName;

    public User(int uid, String username, String fullName) {
        this.uid = uid;
        this.username = username;
        this.fullName = fullName;
    }
}