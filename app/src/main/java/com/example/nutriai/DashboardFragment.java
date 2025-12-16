package com.example.nutriai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.FoodHistory;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

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
        adapter = new FoodHistoryAdapter(historyList);
        rvFoodHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        rvFoodHistory.setAdapter(adapter);

        // Setup Click Listeners
        btnAvatar.setOnClickListener(v -> Toast.makeText(getContext(), "Avatar Clicked", Toast.LENGTH_SHORT).show());
        btnSettings.setOnClickListener(v -> Toast.makeText(getContext(), "Settings Clicked", Toast.LENGTH_SHORT).show());
        
        // Initial load
        loadHistoryData();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadHistoryData();
    }

    private void loadHistoryData() {
        // Make sure db is initialized
        if (db == null) {
            db = AppDatabase.getInstance(requireContext());
        }
        
        // Load data from DB
        historyList = db.foodDao().getAllHistory();
        
        // Update adapter
        if (adapter != null) {
            adapter.setData(historyList);
        }
    }
}