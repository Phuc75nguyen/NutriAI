package com.example.nutriai.utils;

import java.util.HashMap;
import java.util.Map;

public class FoodMapper {
    private static final Map<String, String> foodNameMap = new HashMap<>();

    static {
        // Populate the map with your known food labels and their display names
        foodNameMap.put("suon_non", "Sườn non nướng");
        foodNameMap.put("suon_cot_let", "Cốt lết nướng");
        foodNameMap.put("tofu_trang", "Đậu hủ trắng");
        foodNameMap.put("tofu_chien", "Đậu hủ chiên");
        foodNameMap.put("cha_cat-lát", "Chả cá cắ lát");
        foodNameMap.put("cha_mieng", "Chả cá viên");

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