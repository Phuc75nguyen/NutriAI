package com.example.nutriai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Handle the splash screen transition.
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        // Keep the splash screen visible for this Activity
        splashScreen.setKeepOnScreenCondition(() -> true);

        SharedPreferences prefs = getSharedPreferences("NutriPrefs", MODE_PRIVATE);
        int userId = prefs.getInt("CURRENT_USER_ID", -1);

        if (userId != -1) {
            // User is logged in, go to main activity
            startActivity(new Intent(this, MainActivity.class));
        } else {
            // User is not logged in, go to login activity
            startActivity(new Intent(this, LoginActivity.class));
        }
        finish(); // Finish splash activity
    }
}
