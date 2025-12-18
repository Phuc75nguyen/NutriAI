package com.example.nutriai.utils;

import java.util.HashMap;
import java.util.Map;

public class FoodMapper {
    private static final Map<String, String> foodNameMap = new HashMap<>();

    static {
        // Populate the map with your known food labels and their display names
        foodNameMap.put("suon_non", "Sườn non nướng");
        foodNameMap.put("tofu_trang", "Tofu trắng");
        foodNameMap.put("banh_mi", "Bánh Mì");
        foodNameMap.put("bun_cha", "Bún Chả");
        foodNameMap.put("com_tam", "Cơm Tấm");
        foodNameMap.put("pho", "Phở");
        // Add all your other food items here
    }

    public static String getDisplayName(String rawLabel) {
        if (rawLabel == null) {
            return "Unknown Food";
        }
        String displayName = foodNameMap.get(rawLabel.toLowerCase());
        return displayName != null ? displayName : capitalizeFirstLetter(rawLabel.replace('_', ' '));
    }

    private static String capitalizeFirstLetter(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}