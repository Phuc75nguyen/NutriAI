package com.example.nutriai;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private long pendingConversationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 3. Xử lý sự kiện click
        // Setup listener BEFORE setting selected item to ensure logic flows correctly if needed,
        // though typically setSelectedItemId triggers the listener.
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    selectedFragment = new DashboardFragment();
                } else if (itemId == R.id.nav_chat) {
                    LucfinFragment chatFragment = new LucfinFragment();
                    // Check logic for pending ID
                    if (pendingConversationId != -1) {
                        Bundle args = new Bundle();
                        args.putLong("CONVERSATION_ID", pendingConversationId);
                        chatFragment.setArguments(args);
                        pendingConversationId = -1; // Reset immediately
                    }
                    selectedFragment = chatFragment;
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
            // Check if we are ALREADY on the chat tab
            if (bottomNavigationView.getSelectedItemId() == R.id.nav_chat) {
                // Case A: Already on Chat tab.
                // setSelectedItemId won't trigger listener if item is already selected.
                // Force reload manually.
                LucfinFragment chatFragment = new LucfinFragment();
                Bundle args = new Bundle();
                args.putLong("CONVERSATION_ID", pendingConversationId);
                chatFragment.setArguments(args);
                
                loadFragment(chatFragment);
                pendingConversationId = -1; // Reset immediately
            } else {
                // Case B: Switching from another tab.
                // This will trigger onNavigationItemSelected, which handles the logic.
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