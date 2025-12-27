package com.example.nutriai;

import android.content.Intent;
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
import com.example.nutriai.api.ChatRetrofitClient;
import com.example.nutriai.api.FoodAnalysisResponse;
import com.example.nutriai.api.ScanRequest;
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
    private String originalImagePath;
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
            analyzeImage(processedFile != null ? processedFile : new File(originalImagePath));
        } else {
            Toast.makeText(this, "Image path error", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private File fixImageRotation(String path) {
        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
            int angle = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90: angle = 90; break;
                case ExifInterface.ORIENTATION_ROTATE_180: angle = 180; break;
                case ExifInterface.ORIENTATION_ROTATE_270: angle = 270; break;
                default: return new File(path);
            }
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            File fixedFile = new File(getCacheDir(), "fixed_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(fixedFile)) {
                rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }
            return fixedFile;
        } catch (IOException e) {
            return new File(path);
        }
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

                    if (res.getImage_base64() != null && !res.getImage_base64().isEmpty()) {
                        try {
                            byte[] decoded = Base64.decode(res.getImage_base64(), Base64.DEFAULT);
                            Bitmap resultBitmap = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                            ivResultImage.setImageBitmap(resultBitmap);

                            // --- MAKE IMAGE CLICKABLE FOR FULL-SCREEN VIEW ---
                            ivResultImage.setOnClickListener(v -> {
                                String tempPath = saveResultImageToFile();
                                if (tempPath != null) {
                                    Intent intent = new Intent(FoodResultActivity.this, FullScreenImageActivity.class);
                                    intent.putExtra("image_path", tempPath);
                                    startActivity(intent);
                                } else {
                                    Toast.makeText(FoodResultActivity.this, "Cannot open image", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (Exception e) {
                            Log.e("FoodResultActivity", "Error decoding or setting image", e);
                        }
                    }

                    processMultipleFoods(res.getDetections());

                } else {
                    Toast.makeText(FoodResultActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FoodAnalysisResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodResultActivity.this, "Connection Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void processMultipleFoods(List<FoodAnalysisResponse.Detection> detections) {
        if (detections == null || detections.isEmpty()) {
            Toast.makeText(this, "No food detected", Toast.LENGTH_SHORT).show();
            return;
        }

        double totalCal = 0, totalPro = 0, totalCarb = 0, totalFat = 0;
        Map<String, Integer> foodCounts = new HashMap<>();
        for (FoodAnalysisResponse.Detection detection : detections) {
            FoodHistory item = NutritionLookup.getNutritionInfo(detection.getClass_name());
            String name = item.getFoodName();
            foodCounts.put(name, foodCounts.getOrDefault(name, 0) + 1);
        }

        List<String> displayNames = new ArrayList<>();
        StringBuilder summaryBuilder = new StringBuilder("Bữa ăn gồm: ");

        for (Map.Entry<String, Integer> entry : foodCounts.entrySet()) {
            String foodName = entry.getKey();
            int count = entry.getValue();
            FoodHistory standardItem = NutritionLookup.getNutritionInfoByVietnameseName(foodName);
            double multiplier = getNutritionMultiplier(foodName, count);

            totalCal += standardItem.getCalories() * multiplier;
            totalPro += standardItem.getProtein() * multiplier;
            totalCarb += standardItem.getCarbs() * multiplier;
            totalFat += standardItem.getFat() * multiplier;

            String displayStr = (multiplier > 1.0) ? (int)multiplier + "x " + foodName : foodName;
            displayNames.add(displayStr);
            summaryBuilder.append(displayStr).append(", ");
        }

        String finalName = String.join(" + ", displayNames);
        if (finalName.length() > 40) finalName = "Combo " + displayNames.size() + " món";

        if (summaryBuilder.length() > 2) summaryBuilder.setLength(summaryBuilder.length() - 2);
        String autoSummary = summaryBuilder.toString() + ". Tổng năng lượng: " + (int)totalCal + " kcal.";

        currentFoodHistory = new FoodHistory(
                finalName, "1 phần", autoSummary, "", System.currentTimeMillis(),
                totalCal, totalPro, totalCarb, totalFat
        );

        updateUi(currentFoodHistory);

        List<String> detectedFoods = new ArrayList<>(foodCounts.keySet());
        ScanRequest request = new ScanRequest("default_user", detectedFoods);
        ChatRetrofitClient.getApiService().syncScanData(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d("Lucfin", "✅ Scan data synced!");
                } else {
                    Log.e("Lucfin", "⚠️ Sync failed: " + response.code());
                }
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e("Lucfin", "❌ Sync network error: " + t.getMessage());
            }
        });
    }

    private double getNutritionMultiplier(String foodName, int boxCount) {
        if (foodName.equals("Sườn Cốt lết")) return (double) boxCount;
        if (foodName.equals("Sườn non")) return 1.0;
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
            currentFoodHistory.setImagePath(savedPath != null ? savedPath : originalImagePath);
            currentFoodHistory.setTimestamp(System.currentTimeMillis());

            Executors.newSingleThreadExecutor().execute(() -> {
                db.foodDao().insert(currentFoodHistory);
                runOnUiThread(() -> {
                    Toast.makeText(FoodResultActivity.this, "Saved to diary!", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }

    private String saveResultImageToFile() {
        try {
            if (ivResultImage.getDrawable() == null) return null;
            Bitmap bitmap = ((BitmapDrawable) ivResultImage.getDrawable()).getBitmap();
            File resultFile = new File(getCacheDir(), "bbox_" + System.currentTimeMillis() + ".jpg");
            try (FileOutputStream out = new FileOutputStream(resultFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
            return resultFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("SaveResultImage", "Error saving image to file", e);
            return null;
        }
    }
}
