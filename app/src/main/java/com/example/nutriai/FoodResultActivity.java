package com.example.nutriai;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.nutriai.api.ApiService;
import com.example.nutriai.api.CVRetrofitClient;
import com.example.nutriai.api.ChatRetrofitClient; // [MỚI] Import client chat
import com.example.nutriai.api.FoodAnalysisResponse;
import com.example.nutriai.api.ScanRequest; // [MỚI] Import model request
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.FoodHistory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FoodResultActivity extends AppCompatActivity {

    private ImageView ivResultImage;
    private TextView tvFoodName, tvFoodWeight, tvSummaryContent;
    private TextView tvCalorieValue, tvProteinValue, tvCarbValue, tvFatValue;
    private ProgressBar progressBar;
    private Button btnSaveDiary;

    private AppDatabase db;
    private String originalImagePath; // Đường dẫn ảnh gốc
    private FoodHistory currentFoodHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaning_food_result);

        db = AppDatabase.getInstance(getApplicationContext());

        ivResultImage = findViewById(R.id.iv_result_image);
        tvFoodName = findViewById(R.id.tv_food_name);
        tvFoodWeight = findViewById(R.id.tv_food_weight);
        tvCalorieValue = findViewById(R.id.tvCalorieValue);
        tvProteinValue = findViewById(R.id.tvProteinValue);
        tvCarbValue = findViewById(R.id.tvCarbValue);
        tvFatValue = findViewById(R.id.tvFatValue);
        tvSummaryContent = findViewById(R.id.tv_summary_content);
        btnSaveDiary = findViewById(R.id.btn_save_diary);
        progressBar = findViewById(R.id.progress_bar);

        originalImagePath = getIntent().getStringExtra("image_path");

        if (originalImagePath != null && !originalImagePath.isEmpty()) {
            File processedFile = fixImageRotation(originalImagePath);
            if (processedFile != null) {
                analyzeImage(processedFile);
            } else {
                analyzeImage(new File(originalImagePath));
            }
        } else {
            Toast.makeText(this, "Lỗi đường dẫn ảnh", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // Hàm xoay ảnh (Giữ nguyên vì đang hoạt động tốt)
    private File fixImageRotation(String path) {
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int angle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: angle = 90; break;
                case ExifInterface.ORIENTATION_ROTATE_180: angle = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: angle = 270; break;
                case ExifInterface.ORIENTATION_NORMAL: return new File(path);
                default: return new File(path);
            }
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            File fixedFile = new File(getCacheDir(), "fixed_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(fixedFile);
            rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush(); out.close();
            return fixedFile;
        } catch (IOException e) { return new File(path); }
    }

    private void analyzeImage(File file) {
        progressBar.setVisibility(View.VISIBLE);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        CVRetrofitClient.getApiService().analyzeImage(body).enqueue(new Callback<FoodAnalysisResponse>() {
            @Override
            public void onResponse(Call<FoodAnalysisResponse> call, Response<FoodAnalysisResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    FoodAnalysisResponse res = response.body();

                    // --- 1. XỬ LÝ ẢNH CÓ BOUNDING BOX ---
                    if (res.getImage_base64() != null && !res.getImage_base64().isEmpty()) {
                        try {
                            byte[] decoded = Base64.decode(res.getImage_base64(), Base64.DEFAULT);
                            Bitmap resultBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            ivResultImage.setImageBitmap(resultBitmap); // Hiển thị ảnh có khung
                        } catch (Exception e) {}
                    }

                    // --- 2. XỬ LÝ CỘNG DỒN DINH DƯỠNG ---
                    processMultipleFoods(res.getDetections());

                } else {
                    Toast.makeText(FoodResultActivity.this, "Lỗi Server: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<FoodAnalysisResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodResultActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processMultipleFoods(List<FoodAnalysisResponse.Detection> detections) {
        if (detections == null || detections.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy món ăn nào", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalCal = 0, totalPro = 0, totalCarb = 0, totalFat = 0;

        // 1. Đếm số lượng box của từng món
        Map<String, Integer> foodCounts = new HashMap<>();
        for (FoodAnalysisResponse.Detection detection : detections) {
            FoodHistory item = NutritionLookup.getNutritionInfo(detection.getClass_name());
            String name = item.getFoodName();
            foodCounts.put(name, foodCounts.getOrDefault(name, 0) + 1);
        }

        List<String> displayNames = new ArrayList<>();
        StringBuilder summaryBuilder = new StringBuilder("Bữa ăn gồm: ");

        // 2. Tính toán dinh dưỡng & Hiển thị
        for (Map.Entry<String, Integer> entry : foodCounts.entrySet()) {
            String foodName = entry.getKey();
            int count = entry.getValue();

            FoodHistory standardItem = NutritionLookup.getNutritionInfoByVietnameseName(foodName);
            double multiplier = getNutritionMultiplier(foodName, count);

            totalCal += standardItem.getCalories() * multiplier;
            totalPro += standardItem.getProtein() * multiplier;
            totalCarb += standardItem.getCarbs() * multiplier;
            totalFat += standardItem.getFat() * multiplier;

            String displayStr;
            if (multiplier > 1.0) {
                displayStr = (int)multiplier + "x " + foodName;
            } else {
                displayStr = foodName;
            }

            displayNames.add(displayStr);
            summaryBuilder.append(displayStr).append(", ");
        }

        String finalName = String.join(" và ", displayNames);
        if (finalName.length() > 40) finalName = "Combo " + displayNames.size() + " món";

        if (summaryBuilder.length() > 2) summaryBuilder.setLength(summaryBuilder.length() - 2);
        String autoSummary = summaryBuilder.toString() + ". Tổng năng lượng: " + (int)totalCal + " kcal.";

        currentFoodHistory = new FoodHistory(
                finalName,
                "1 phần",
                autoSummary,
                "",
                System.currentTimeMillis(),
                totalCal, totalPro, totalCarb, totalFat
        );

        updateUi(currentFoodHistory);

        // =================================================================
        // [QUAN TRỌNG] BƯỚC 3: GỌI API ĐỒNG BỘ MÓN ĂN LÊN CHATBOT SERVER
        // =================================================================

        // Lấy danh sách tên món ăn đã detect (để gửi lên server)
        List<String> detectedFoods = new ArrayList<>(foodCounts.keySet());

        // Tạo Request (dùng ScanRequest đã tạo ở Bước 1)
        ScanRequest request = new ScanRequest("default_user", detectedFoods);

        // Gọi API bất đồng bộ (Background Thread)
        ChatRetrofitClient.getApiService().syncScanData(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Lucfin", "✅ Đã đồng bộ món ăn lên Server (Session: default_user)!");
                } else {
                    Log.e("Lucfin", "⚠️ Server từ chối: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Lucfin", "❌ Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private double getNutritionMultiplier(String foodName, int boxCount) {
        if (foodName.equals("Sườn Cốt lết")) {
            return (double) boxCount;
        }
        if (foodName.equals("Sườn non")) {
            return 1.0;
        }
        return 1.0;
    }

    private void updateUi(FoodHistory food) {
        tvFoodName.setText(food.getFoodName());
        tvFoodWeight.setText(food.getFoodWeight());
        tvCalorieValue.setText(String.format(Locale.US, "%.0f kcal", food.getCalories()));
        tvProteinValue.setText(String.format(Locale.US, "%.1fg", food.getProtein()));
        tvCarbValue.setText(String.format(Locale.US, "%.1fg", food.getCarbs()));
        tvFatValue.setText(String.format(Locale.US, "%.1fg", food.getFat()));

        tvSummaryContent.setText(food.getSummary());

        btnSaveDiary.setOnClickListener(v -> saveToDiary());
    }

    private void saveToDiary() {
        if (currentFoodHistory != null) {
            String savedPath = saveResultImageToFile();
            if (savedPath != null) {
                currentFoodHistory.setImagePath(savedPath);
            } else {
                currentFoodHistory.setImagePath(originalImagePath);
            }

            currentFoodHistory.setTimestamp(System.currentTimeMillis());

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                db.foodDao().insert(currentFoodHistory);
                runOnUiThread(() -> {
                    Toast.makeText(FoodResultActivity.this, "Đã lưu nhật ký & ảnh bounding box!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private String saveResultImageToFile() {
        try {
            if (ivResultImage.getDrawable() == null) return null;

            Bitmap bitmap = ((BitmapDrawable) ivResultImage.getDrawable()).getBitmap();

            File resultFile = new File(getFilesDir(), "bbox_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream out = new FileOutputStream(resultFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            return resultFile.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}