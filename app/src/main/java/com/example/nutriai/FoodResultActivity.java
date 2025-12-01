package com.example.nutriai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;

import io.noties.markwon.Markwon;

public class FoodResultActivity extends AppCompatActivity {

    private ImageView ivFoodImage;
    private TextView tvDietTitle, tvDietDescription;
    private TextView tvCalorieValue, tvProteinValue, tvCarbValue, tvFatValue;
    private ProgressBar pbCalorie, pbProtein, pbCarb, pbFat;
    private View nutritionGrid;
    private ProgressBar loadingProgress;
    private Button btnRetry;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scaning_food_result);

        // Bind Views based on activity_scaning_food_result.xml structure
        ivFoodImage = findViewById(R.id.iv_food_image);
        tvDietTitle = findViewById(R.id.tvDietTitle);
        tvDietDescription = findViewById(R.id.tvDietDescription);
        loadingProgress = findViewById(R.id.loading_progress);
        btnRetry = findViewById(R.id.btn_retry);
        nutritionGrid = findViewById(R.id.nutritionGrid);

        // Nutrition Cards Views
        tvCalorieValue = findViewById(R.id.tvCalorieValue);
        pbCalorie = findViewById(R.id.pbCalorie);
        
        tvProteinValue = findViewById(R.id.tvProteinValue);
        pbProtein = findViewById(R.id.pbProtein);
        
        tvCarbValue = findViewById(R.id.tvCarbValue);
        pbCarb = findViewById(R.id.pbCarb);
        
        tvFatValue = findViewById(R.id.tvFatValue);
        pbFat = findViewById(R.id.pbFat);
        
        ImageView backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        imagePath = getIntent().getStringExtra("image_path");

        if (imagePath != null) {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                Glide.with(this)
                        .load(imageFile)
                        .into(ivFoodImage);
                simulateAnalysis();
            } else {
                Toast.makeText(this, "Image file not found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No image path provided", Toast.LENGTH_SHORT).show();
        }
        
        btnRetry.setOnClickListener(v -> {
            if (imagePath != null) {
                simulateAnalysis();
            }
        });
    }

    private void simulateAnalysis() {
        // Show loading state
        loadingProgress.setVisibility(View.VISIBLE);
        
        // Hide result views initially
        tvDietTitle.setVisibility(View.GONE);
        tvDietDescription.setVisibility(View.GONE);
        nutritionGrid.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            loadingProgress.setVisibility(View.GONE);
            
            // 1. Set Food Name (Title)
            tvDietTitle.setText("Phở Bò Tái Nạm");
            tvDietTitle.setVisibility(View.VISIBLE);
            
            // 2. Set Description (Markdown)
            String mockDescription = "**Phân tích:** Món ăn này là Phở Bò, một món ăn truyền thống của Việt Nam, giàu protein từ thịt bò và năng lượng từ bánh phở.\n\n" +
                    "**Gợi ý:** Ăn kèm nhiều rau sống để bổ sung chất xơ và vitamin. Hạn chế uống hết nước dùng nếu bạn đang kiểm soát lượng muối nạp vào.";
            final Markwon markwon = Markwon.create(FoodResultActivity.this);
            markwon.setMarkdown(tvDietDescription, mockDescription);
            tvDietDescription.setVisibility(View.VISIBLE);
            
            // 3. Set Macro Values
            // Calories
            tvCalorieValue.setText("450 kcal");
            pbCalorie.setProgress(45); // Mock percentage
            
            // Protein
            tvProteinValue.setText("25g");
            pbProtein.setProgress(60);
            
            // Carbs
            tvCarbValue.setText("60g");
            pbCarb.setProgress(50);
            
            // Fat
            tvFatValue.setText("12g");
            pbFat.setProgress(30);
            
            // Show Nutrition Grid
            nutritionGrid.setVisibility(View.VISIBLE);

        }, 2000); // 2 seconds delay
    }
    
    private void showError(String message) {
        // Use description text view to show error
        tvDietDescription.setText(message);
        tvDietDescription.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
    }
}
