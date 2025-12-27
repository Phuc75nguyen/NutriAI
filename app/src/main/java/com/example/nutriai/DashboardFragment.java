package com.example.nutriai;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.FoodHistory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment implements FoodHistoryAdapter.OnHistoryAction {

    private RecyclerView rvFoodHistory;
    private FoodHistoryAdapter adapter;
    private List<FoodHistory> historyList;
    private AppDatabase db;
    private ImageButton btnAvatar;
    private ImageButton btnSettings;

    public DashboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());

        // Bind Views
        rvFoodHistory = view.findViewById(R.id.rv_food_history);
        btnAvatar = view.findViewById(R.id.btn_avatar);
        btnSettings = view.findViewById(R.id.btn_settings);

        // Setup RecyclerView
        historyList = new ArrayList<>();
        adapter = new FoodHistoryAdapter(historyList, this);
        rvFoodHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoodHistory.setAdapter(adapter);

        // Setup Click Listeners
        btnAvatar.setOnClickListener(v -> handleLogout());
        btnSettings.setOnClickListener(v -> showThemeDialog());
        
        // Initial load
        loadHistoryData();
    }

    private void showThemeDialog() {
        final String[] themes = {"Light Mode", "Dark Mode", "System Default"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Theme");
        builder.setItems(themes, (dialog, which) -> {
            int mode;
            switch (which) {
                case 0:
                    mode = AppCompatDelegate.MODE_NIGHT_NO;
                    break;
                case 1:
                    mode = AppCompatDelegate.MODE_NIGHT_YES;
                    break;
                default:
                    mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                    break;
            }
            
            SharedPreferences prefs = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
            prefs.edit().putInt("night_mode", mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
            dialog.dismiss();
        });
        builder.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryData();
    }

    private void loadHistoryData() {
        if (db == null) {
            db = AppDatabase.getInstance(requireContext());
        }
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<FoodHistory> loadedHistory = db.foodDao().getAllHistory();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    historyList = loadedHistory;
                    adapter.setData(historyList);
                });
            }
        });
    }

    private void handleLogout() {
        SharedPreferences prefs = requireContext().getSharedPreferences("NutriPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove("CURRENT_USER_ID").apply();

        Intent intent = new Intent(getActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    // --- Implementation of OnHistoryAction interface ---

    @Override
    public void onViewImage(String path) {
        if (path != null && !path.isEmpty()) {
            Intent intent = new Intent(getContext(), FullScreenImageActivity.class);
            intent.putExtra("image_path", path);
            startActivity(intent);
        } else {
            Toast.makeText(getContext(), "No image available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDeleteItem(FoodHistory item, int position) {
        new AlertDialog.Builder(requireContext())
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete this history item?")
            .setPositiveButton("Delete", (dialog, which) -> {
                Executors.newSingleThreadExecutor().execute(() -> {
                    db.foodDao().delete(item);
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            historyList.remove(position);
                            adapter.notifyItemRemoved(position);
                            // You might want to update positions for the rest of the items
                            adapter.notifyItemRangeChanged(position, historyList.size());
                            Toast.makeText(getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
                        });
                    }
                });
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}