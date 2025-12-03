package com.example.nutriai;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment; // Import quan trọng

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Ánh xạ
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 2. Load màn hình Home mặc định
        if (savedInstanceState == null) {
            loadFragment(new DashboardFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // 3. Xử lý sự kiện click
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                // --- SỬA LỖI Ở ĐÂY: Khai báo là Fragment, KHÔNG ĐƯỢC KHAI BÁO LÀ LucfinFragment ---
                Fragment selectedFragment = null;
                // ----------------------------------------------------------------------------------

                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardFragment();

                } else if (itemId == R.id.nav_chat) { // ID khớp với menu XML của bạn
                    selectedFragment = new LucfinFragment();

                } else if (itemId == R.id.nav_scan) { // ID khớp với menu XML của bạn
                    // Mở màn hình Scan (Activity riêng)
                    Intent intent = new Intent(MainActivity.this, ScaningFoodActivity.class);
                    startActivity(intent);
                    return false;
                }

                // Thay thế Fragment
                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}