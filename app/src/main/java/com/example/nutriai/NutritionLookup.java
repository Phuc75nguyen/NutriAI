package com.example.nutriai;

import com.example.nutriai.database.FoodHistory;

public class NutritionLookup {

    // Lấy thông tin từ class_name của API (vd: "suon_cot_let")
    public static FoodHistory getNutritionInfo(String rawLabel) {
        if (rawLabel == null) rawLabel = "";
        String label = rawLabel.toLowerCase();

        // 1. SƯỜN CỐT LẾT (Miếng to)
        if (label.contains("suon_cot_let")) {
            return createFoodHistory("Sườn Cốt lết", "1 miếng", 550, 25, 5, 30);
        }
        // 2. SƯỜN NON (Khúc nhỏ)
        else if (label.contains("suon_non")) {
            return createFoodHistory("Sườn non", "1 phần", 600, 30, 8, 35);
        }
        // 3. SƯỜN (Chung chung - Fix lỗi hiện "Suon" không dấu)
        else if (label.contains("suon")) {
            return createFoodHistory("Sườn nướng", "1 miếng", 500, 20, 5, 25);
        }
        // 4. CHẢ (Cá/Trứng/...)
        else if (label.contains("cha")) {
            return createFoodHistory("Chả cá", "1 phần", 450, 20, 10, 25);
        }
        // 5. ĐẬU HŨ (Tofu)
        else if (label.contains("tofu") || label.contains("dau")) {
            return createFoodHistory("Đậu hũ", "1 phần", 150, 12, 5, 10);
        }
        // Mặc định
        else {
            // Viết hoa chữ cái đầu cho đẹp: "unknown" -> "Unknown"
            String display = rawLabel.isEmpty() ? "Unknown" : rawLabel.substring(0, 1).toUpperCase() + rawLabel.substring(1);
            return createFoodHistory(display, "1 phần", 0, 0, 0, 0);
        }
    }

    // Hàm này được gọi từ FoodResultActivity để lấy thông tin chuẩn
    public static FoodHistory getNutritionInfoByVietnameseName(String vietnameseName) {
        if (vietnameseName == null) return getNutritionInfo("");

        if (vietnameseName.equals("Sườn Cốt lết")) {
            return getNutritionInfo("suon_cot_let");
        }
        if (vietnameseName.equals("Sườn non")) {
            return getNutritionInfo("suon_non");
        }
        if (vietnameseName.equals("Sườn nướng")) { // Thêm dòng này cho chắc
            return getNutritionInfo("suon");
        }
        // SỬA LỖI Ở ĐÂY: "Chả Cá" -> "Chả cá" (cho khớp với bên trên)
        if (vietnameseName.equals("Chả cá") || vietnameseName.equals("Chả")) {
            return getNutritionInfo("cha");
        }
        if (vietnameseName.equals("Đậu hũ")) {
            return getNutritionInfo("tofu");
        }

        return getNutritionInfo(vietnameseName); // Fallback
    }

    // Hàm hỗ trợ để tạo đối tượng FoodHistory nhanh gọn
    private static FoodHistory createFoodHistory(String name, String weight, double cal, double pro, double carb, double fat) {
        FoodHistory food = new FoodHistory();
        food.setFoodName(name);
        food.setFoodWeight(weight);
        food.setCalories(cal);
        food.setProtein(pro);
        food.setCarbs(carb);
        food.setFat(fat);
        food.setSummary("");
        food.setImagePath("");
        food.setTimestamp(0);
        return food;
    }
}