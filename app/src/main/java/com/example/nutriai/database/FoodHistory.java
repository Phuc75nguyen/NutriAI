package com.example.nutriai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_history")
public class FoodHistory {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public String foodName;
    public String foodWeight;
    public String summary;
    public String imagePath;
    public long timestamp;

    public double calories;
    public double protein;
    public double carbs;
    public double fat;


    public FoodHistory(String foodName, String foodWeight, String summary, String imagePath, long timestamp, double calories, double protein, double carbs, double fat) {
        this.foodName = foodName;
        this.foodWeight = foodWeight;
        this.summary = summary;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
        this.calories = calories;
        this.protein = protein;
        this.carbs = carbs;
        this.fat = fat;
    }
}
