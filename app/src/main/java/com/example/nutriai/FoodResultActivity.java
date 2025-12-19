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
import com.example.nutriai.api.FoodAnalysisResponse;
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.FoodHistory;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

                    // --- 2. XỬ LÝ CỘNG DỒN DINH DƯỠNG (FIX LỖI CHỈ HIỆN 1 MÓN) ---
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
        List<String> foodNames = new ArrayList<>();
        StringBuilder summaryBuilder = new StringBuilder("Bữa ăn gồm: ");

        // Duyệt qua tất cả các món phát hiện được
        for (FoodAnalysisResponse.Detection detection : detections) {
            FoodHistory item = NutritionLookup.getNutritionInfo(detection.getClass_name());

            totalCal += item.getCalories();
            totalPro += item.getProtein();
            totalCarb += item.getCarbs();
            totalFat += item.getFat();
            foodNames.add(item.getFoodName());
            summaryBuilder.append(item.getFoodName()).append(", ");
        }

        // Tạo đối tượng tổng hợp
        String finalName = String.join(" + ", foodNames); // Ví dụ: Sườn nướng + Đậu hũ
        if (finalName.length() > 30) finalName = "Bữa ăn tổng hợp (" + foodNames.size() + " món)";

        // Xóa dấu phẩy cuối
        if (summaryBuilder.length() > 2) summaryBuilder.setLength(summaryBuilder.length() - 2);

        // --- 3. TẠO SUMMARY TỰ ĐỘNG ĐỂ LUCFIN CÓ CÁI MÀ ĐỌC ---
        String autoSummary = summaryBuilder.toString() + ". Tổng năng lượng: " + (int)totalCal + " kcal. Hãy hỏi tôi nếu cần lời khuyên chi tiết!";

        currentFoodHistory = new FoodHistory(
                finalName,
                foodNames.size() + " món",
                autoSummary, // Lưu summary này vào DB
                "", // ImagePath sẽ cập nhật lúc save
                System.currentTimeMillis(),
                totalCal, totalPro, totalCarb, totalFat
        );

        updateUi(currentFoodHistory);
    }

    private void updateUi(FoodHistory food) {
        tvFoodName.setText(food.getFoodName());
        tvFoodWeight.setText(food.getFoodWeight());
        tvCalorieValue.setText(String.format(Locale.US, "%.0f kcal", food.getCalories()));
        tvProteinValue.setText(String.format(Locale.US, "%.1fg", food.getProtein()));
        tvCarbValue.setText(String.format(Locale.US, "%.1fg", food.getCarbs()));
        tvFatValue.setText(String.format(Locale.US, "%.1fg", food.getFat()));

        // Hiển thị Summary ngay lập tức
        tvSummaryContent.setText(food.getSummary());

        btnSaveDiary.setOnClickListener(v -> saveToDiary());
    }

    private void saveToDiary() {
        if (currentFoodHistory != null) {
            // --- 4. FIX LỖI ẢNH DASHBOARD MẤT KHUNG ---
            // Thay vì lưu ảnh gốc, ta lưu ảnh đang hiển thị trên màn hình (đã có khung)
            String savedPath = saveResultImageToFile();
            if (savedPath != null) {
                currentFoodHistory.setImagePath(savedPath);
            } else {
                currentFoodHistory.setImagePath(originalImagePath); // Fallback nếu lỗi
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

    // Hàm lưu ảnh từ ImageView ra file riêng
    private String saveResultImageToFile() {
        try {
            if (ivResultImage.getDrawable() == null) return null;

            // Lấy Bitmap từ ImageView (Ảnh có vẽ khung từ Server)
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