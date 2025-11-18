package com.example.nutriai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment; // QUAN TRỌNG: Phải dùng androidx

import com.bumptech.glide.Glide;

public class LucfinFragment extends Fragment {

    private ImageView ivRobotGif;
    private EditText etInput;
    private ImageView btnSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate layout chat
        return inflater.inflate(R.layout.activity_start_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Ánh xạ View
        ivRobotGif = view.findViewById(R.id.iv_robot_gif);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);

        // 2. Kích hoạt ảnh động Robot bằng Glide
        Glide.with(this)
                .asGif()
                .load(R.drawable.robot_animation)
                .into(ivRobotGif);

        // 3. Xử lý nút Gửi
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                // Hiện tại chưa có AI, nên mình Toast lên để test giao diện trước
                Toast.makeText(getContext(), "Bạn hỏi: " + question, Toast.LENGTH_SHORT).show();

                // Xóa ô nhập sau khi gửi
                etInput.setText("");
            }
        });
    }
}