package com.example.nutriai;

import android.os.Bundle;
import android.text.TextUtils;
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

import com.example.nutriai.api.ChatRequest;
import com.example.nutriai.api.ChatResponse;
import com.example.nutriai.api.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import io.noties.markwon.Markwon;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Setup Markwon
        Markwon markwon = Markwon.create(requireContext());

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, markwon);
        rcvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvChat.setAdapter(chatAdapter);

        // Handle Send Button
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                sendMessage(question, true, null, null);
                etInput.setText("");
            }
        });
    }

    private void sendMessage(String content, boolean isUser, String imageUrl, String sources) {
        messageList.add(new Message(content, isUser, imageUrl, sources));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);

        if (isUser) {
            callRealApi(content);
        }
    }

    private void callRealApi(String query) {
        ChatRequest request = new ChatRequest(query);
        RetrofitClient.getApiService().chatWithLucfin(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getAnswer();
                    String image = response.body().getImage();
                    List<String> sourceDocs = response.body().getSourceDocuments();
                    
                    String sources = null;
                    if (sourceDocs != null && !sourceDocs.isEmpty()) {
                        sources = TextUtils.join(", ", sourceDocs);
                    }

                    sendMessage(answer, false, image, sources);
                } else {
                    sendMessage("Error: " + response.code(), false, null, null);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                sendMessage("Error: " + t.getMessage(), false, null, null);
            }
        });
    }
}