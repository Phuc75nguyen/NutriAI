package com.example.nutriai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.User;
import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername;
    private MaterialButton btnLogin;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        db = AppDatabase.getInstance(this);

        // Bind to new views
        etUsername = findViewById(R.id.et_email); // ID from new layout
        btnLogin = findViewById(R.id.btn_login);     // ID from new layout

        btnLogin.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        User userToLogin = null;

        if ("Phuc".equalsIgnoreCase(username)) {
            userToLogin = new User(1, "Phuc", "Tan Phuc");
        } else if ("Linh".equalsIgnoreCase(username)) {
            userToLogin = new User(2, "Linh", "Thanh Linh");
        } else {
            Toast.makeText(this, "User not found. Please enter 'Phuc' or 'Linh'", Toast.LENGTH_SHORT).show();
            return;
        }

        // Insert/Update user in DB
        db.userDao().insert(userToLogin);

        // Save to SharedPreferences
        SharedPreferences prefs = getSharedPreferences("NutriPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("CURRENT_USER_ID", userToLogin.uid);
        editor.apply();

        // Navigate to main screen
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Prevent returning to login
    }
}