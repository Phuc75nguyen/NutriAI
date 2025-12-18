package com.example.nutriai;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.nutriai.api.ApiService;
import com.example.nutriai.api.CVRetrofitClient;
import com.example.nutriai.api.FoodAnalysisResponse;
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.FoodHistory;
import com.example.nutriai.utils.FoodMapper;

import java.io.File;
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
    private String imagePath;
    private FoodAnalysisResponse currentFoodResponse;

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

        imagePath = getIntent().getStringExtra("image_path");

        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(this).load(imageFile).into(ivResultImage);
                analyzeImage(imagePath);
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void analyzeImage(String imagePath) {
        progressBar.setVisibility(View.VISIBLE);

        File file = new File(imagePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        ApiService apiService = CVRetrofitClient.getApiService();
        Call<FoodAnalysisResponse> call = apiService.analyzeImage(body);

        call.enqueue(new Callback<FoodAnalysisResponse>() {
            @Override
            public void onResponse(Call<FoodAnalysisResponse> call, Response<FoodAnalysisResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    currentFoodResponse = response.body();
                    updateUi(currentFoodResponse);
                } else {
                    Toast.makeText(FoodResultActivity.this, "Analysis failed: " + response.message(), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<FoodAnalysisResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FoodResultActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void updateUi(FoodAnalysisResponse response) {
        String displayName = FoodMapper.getDisplayName(response.getFoodName());
        tvFoodName.setText(displayName);
        tvFoodWeight.setText(response.getFoodWeight());

        tvCalorieValue.setText(String.format(Locale.getDefault(), "%.0f kcal", response.getCalories()));
        tvProteinValue.setText(String.format(Locale.getDefault(), "%.1fg", response.getProtein()));
        tvCarbValue.setText(String.format(Locale.getDefault(), "%.1fg", response.getCarbs()));
        tvFatValue.setText(String.format(Locale.getDefault(), "%.1fg", response.getFat()));
        tvSummaryContent.setText(response.getSummary());

        btnSaveDiary.setOnClickListener(v -> saveToDiary());
    }

    private void saveToDiary() {
        if (currentFoodResponse != null) {
            String displayName = FoodMapper.getDisplayName(currentFoodResponse.getFoodName());

            FoodHistory foodHistory = new FoodHistory(
                    displayName,
                    currentFoodResponse.getFoodWeight(),
                    currentFoodResponse.getSummary(),
                    imagePath,
                    System.currentTimeMillis(),
                    currentFoodResponse.getCalories(),
                    currentFoodResponse.getProtein(),
                    currentFoodResponse.getCarbs(),
                    currentFoodResponse.getFat()
            );

            Executor executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                db.foodDao().insert(foodHistory);
                runOnUiThread(() -> {
                    Toast.makeText(FoodResultActivity.this, "Saved to diary", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        }
    }
}
