package com.example.nutriai.database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "food_history")
public class FoodHistory {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String foodName;
    private String foodWeight;
    private String summary;
    private String imagePath;
    private long timestamp;
    private double calories;
    private double protein;
    private double carbs;
    private double fat;

    // Empty constructor for Room
    public FoodHistory() { }

    // Ignored constructor for manual object creation
    @Ignore
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

    // --- Getters and Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFoodName() { return foodName; }
    public void setFoodName(String foodName) { this.foodName = foodName; }

    public String getFoodWeight() { return foodWeight; }
    public void setFoodWeight(String foodWeight) { this.foodWeight = foodWeight; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public double getCalories() { return calories; }
    public void setCalories(double calories) { this.calories = calories; }

    public double getProtein() { return protein; }
    public void setProtein(double protein) { this.protein = protein; }

    public double getCarbs() { return carbs; }
    public void setCarbs(double carbs) { this.carbs = carbs; }

    public double getFat() { return fat; }
    public void setFat(double fat) { this.fat = fat; }
}