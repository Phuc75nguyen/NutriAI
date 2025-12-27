package com.example.nutriai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface FoodDao {
    @Insert
    void insert(FoodHistory food);

    @Query("SELECT * FROM food_history ORDER BY timestamp DESC")
    List<FoodHistory> getAllHistory();

    @Query("SELECT * FROM food_history ORDER BY timestamp DESC LIMIT 1")
    FoodHistory getLatestFood();

    @Delete
    void delete(FoodHistory food);
}
