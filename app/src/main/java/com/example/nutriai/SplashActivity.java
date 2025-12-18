package com.example.nutriai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            SharedPreferences prefs = getSharedPreferences("NutriPrefs", MODE_PRIVATE);
            int userId = prefs.getInt("CURRENT_USER_ID", -1);

            if (userId != -1) {
                // User is logged in, go to main activity
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // User is not logged in, go to login activity
                startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            }
            finish(); // Finish splash activity
        }, 1500); // 1.5 second delay
    }
}