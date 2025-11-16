package com.example.nutriai;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.bumptech.glide.Glide; // <-- Import thư viện Glide

public class LucfinFragment extends Fragment {
    private ImageView ivRobotGif; // Khai báo biến ImageView

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout của bạn
        return inflater.inflate(R.layout.activity_start_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ (tìm) ImageView từ layout bằng ID của nó
        ivRobotGif = view.findViewById(R.id.iv_robot_gif);

        // 2. Dùng GLIDE để tải ảnh GIF
        // 'this' ở đây là Fragment.
        // Glide sẽ tự động nhận ra đây là GIF và cho nó chạy (loop)
        Glide.with(this)
                .load(R.drawable.robot_animation) // Tải từ thư mục drawable
                .into(ivRobotGif); // Hiển thị vào ImageView
    }
}
