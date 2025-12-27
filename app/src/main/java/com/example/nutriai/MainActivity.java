package com.example.nutriai;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long pendingConversationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // --- APPLY THEME ON STARTUP ---
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        int savedMode = prefs.getInt("night_mode", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(savedMode);
        // ------------------------------

        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNavigationView.getSelectedItemId() != R.id.nav_home) {
                    bottomNavigationView.setSelectedItemId(R.id.nav_home);
                } else {
                    moveTaskToBack(true);
                }
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_chat) {
                    LucfinFragment chatFragment = new LucfinFragment();
                    if (pendingConversationId != -1) {
                        Bundle args = new Bundle();
                        args.putLong("CONVERSATION_ID", pendingConversationId);
                        chatFragment.setArguments(args);
                        pendingConversationId = -1;
                    }
                    selectedFragment = chatFragment;
                } else if (itemId == R.id.nav_scan) {
                    Intent scanIntent = new Intent(MainActivity.this, ScaningFoodActivity.class);
                    startActivity(scanIntent);
                    return false;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                    return true;
                }
                return false;
            }
        });

        handleIntent(getIntent());

        if (savedInstanceState == null) {
            if (pendingConversationId != -1) {
                bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        
        handleIntent(intent);

        if (pendingConversationId != -1) {
            if (bottomNavigationView.getSelectedItemId() == R.id.nav_chat) {
                LucfinFragment chatFragment = new LucfinFragment();
                Bundle args = new Bundle();
                args.putLong("CONVERSATION_ID", pendingConversationId);
                chatFragment.setArguments(args);
                
                loadFragment(chatFragment);
                pendingConversationId = -1;
            } else {
                bottomNavigationView.setSelectedItemId(R.id.nav_chat);
            }
        }
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.getBooleanExtra("IS_HISTORY", false)) {
            pendingConversationId = intent.getLongExtra("CONVERSATION_ID", -1);
        } else {
            pendingConversationId = -1;
        }
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
