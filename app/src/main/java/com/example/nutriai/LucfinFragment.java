package com.example.nutriai;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class LucfinFragment extends Fragment {

    private RecyclerView rcvChat;
    private ChatAdapter chatAdapter;
    private List<Message> messageList;
    private EditText etInput;
    private ImageView btnSend;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_start_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Views
        rcvChat = view.findViewById(R.id.rcv_chat);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);
        rcvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvChat.setAdapter(chatAdapter);

        // Handle Send Button
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                sendMessage(question, true);
                etInput.setText("");

                // Simulate Bot Response
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    sendMessage("This is a fake response from NutriAI bot.", false);
                }, 1000);
            }
        });
    }

    private void sendMessage(String content, boolean isUser) {
        messageList.add(new Message(content, isUser));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);
    }
}