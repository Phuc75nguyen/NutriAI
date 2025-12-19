package com.example.nutriai;

import com.example.nutriai.database.FoodHistory;

public class NutritionLookup {
    public static FoodHistory getNutritionInfo(String rawLabel) {
        if (rawLabel == null) rawLabel = "";
        String label = rawLabel.toLowerCase();

        // Server trả về: "Suon", "Cha Ca", "Tofu"
        // Thứ tự Constructor: Name, Weight, Summary, ImagePath, Timestamp, Cal, Pro, Carb, Fat

        if (label.contains("suon")) {
            return new FoodHistory("Sườn nướng", "1 miếng", "", "", 0, 600, 30, 80, 20);
        } else if (label.contains("cha")) {
            return new FoodHistory("Chả cá", "1 phần", "", "", 0, 450, 25, 60, 10);
        } else if (label.contains("tofu") || label.contains("dau")) {
            return new FoodHistory("Đậu hũ", "1 phần", "", "", 0, 150, 12, 10, 8);
        } else {
            return new FoodHistory(rawLabel, "Chưa rõ", "", "", 0, 0, 0, 0, 0);
        }
    }
}