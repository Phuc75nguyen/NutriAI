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

        // --- FIX: Check Intent extras BEFORE loading default fragment ---
        Intent intent = getIntent();
        boolean isFromHistory = intent.getBooleanExtra("IS_HISTORY", false);

        if (isFromHistory) {
            // Open Chat fragment from history
            long conversationId = intent.getLongExtra("CONVERSATION_ID", -1);
            if (conversationId != -1) {
                LucfinFragment chatFragment = new LucfinFragment();
                Bundle args = new Bundle();
                args.putLong("CONVERSATION_ID", conversationId);
                chatFragment.setArguments(args);
                
                loadFragment(chatFragment);
                bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            }
        } else if (savedInstanceState == null) {
            // 2. Load màn hình Home mặc định
            loadFragment(new DashboardFragment());
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        }

        // 3. Xử lý sự kiện click
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_chat) {
                    selectedFragment = new LucfinFragment();
                } else if (itemId == R.id.nav_scan) {
                    Intent scanIntent = new Intent(MainActivity.this, ScaningFoodActivity.class);
                    startActivity(scanIntent);
                    return false; // Don't select the item, it's an activity
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent); // Update intent in case MainActivity is singleTop

        // Handle navigation if coming from history while activity is open
        boolean isFromHistory = intent.getBooleanExtra("IS_HISTORY", false);
        if (isFromHistory) {
            long conversationId = intent.getLongExtra("CONVERSATION_ID", -1);
            if (conversationId != -1) {
                LucfinFragment chatFragment = new LucfinFragment();
                Bundle args = new Bundle();
                args.putLong("CONVERSATION_ID", conversationId);
                chatFragment.setArguments(args);
                
                loadFragment(chatFragment);
                bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            }
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}