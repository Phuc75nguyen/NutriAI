package com.example.nutriai.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {
    @Insert
    void insert(FoodHistory food);

    @Query("SELECT * FROM food_history ORDER BY timestamp DESC")
    List<FoodHistory> getAllHistory();

    // --- THÊM HÀM NÀY ĐỂ LẤY MÓN ĂN VỪA SCAN ---
    @Query("SELECT * FROM food_history ORDER BY timestamp DESC LIMIT 1")
    FoodHistory getLatestFood();
}