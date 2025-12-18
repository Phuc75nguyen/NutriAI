package com.example.nutriai.api;

import com.google.gson.annotations.SerializedName;

public class FoodAnalysisResponse {

    @SerializedName("food_name")
    private String foodName;

    @SerializedName("food_weight")
    private String foodWeight;

    @SerializedName("calories")
    private double calories;

    @SerializedName("protein")
    private double protein;

    @SerializedName("carbs")
    private double carbs;

    @SerializedName("fat")
    private double fat;

    @SerializedName("summary")
    private String summary;

    // Getters
    public String getFoodName() { return foodName; }
    public String getFoodWeight() { return foodWeight; }
    public double getCalories() { return calories; }
    public double getProtein() { return protein; }
    public double getCarbs() { return carbs; }
    public double getFat() { return fat; }
    public String getSummary() { return summary; }

    // Setters
    public void setFoodName(String foodName) { this.foodName = foodName; }
    public void setFoodWeight(String foodWeight) { this.foodWeight = foodWeight; }
    public void setCalories(double calories) { this.calories = calories; }
    public void setProtein(double protein) { this.protein = protein; }
    public void setCarbs(double carbs) { this.carbs = carbs; }
    public void setFat(double fat) { this.fat = fat; }
    public void setSummary(String summary) { this.summary = summary; }
}
