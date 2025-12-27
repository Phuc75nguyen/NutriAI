package com.example.nutriai;

import android.content.Intent;
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

import com.bumptech.glide.Glide;
import com.example.nutriai.api.ChatRequest;
import com.example.nutriai.api.ChatResponse;
import com.example.nutriai.api.ChatRetrofitClient;
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.ChatMessage;
import com.example.nutriai.database.Conversation;
import com.example.nutriai.database.FoodHistory;

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
    private ImageView ivRobotMascot; // The new mascot view

    // Database variables
    private long currentConversationId = -1;
    private AppDatabase db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_start_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = AppDatabase.getInstance(requireContext());

        // Bind views
        rcvChat = view.findViewById(R.id.rcv_chat);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);
        ivRobotMascot = view.findViewById(R.id.iv_robot_mascot);
        ImageView btnHistory = view.findViewById(R.id.iv_chat_icon);

        // Load the robot animation
        Glide.with(this).asGif().load(R.drawable.robot_animation).into(ivRobotMascot);

        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatHistoryActivity.class);
            startActivity(intent);
        });

        Markwon markwon = Markwon.create(requireContext());
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, markwon);
        rcvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvChat.setAdapter(chatAdapter);

        if (getArguments() != null) {
            long conversationId = getArguments().getLong("CONVERSATION_ID", -1);
            if (conversationId != -1) {
                currentConversationId = conversationId;
                loadMessagesFromDb(currentConversationId);
            }
        }

        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (question.isEmpty()) return;
            handleUserMessage(question, question);
            etInput.setText("");
        });
        
        // Set initial visibility
        updateRobotVisibility();
    }

    private void loadMessagesFromDb(long convId) {
        List<ChatMessage> oldMessages = db.chatDao().getMessagesByConversation(convId);
        messageList.clear();
        for (ChatMessage oldMsg : oldMessages) {
            messageList.add(new Message(oldMsg.content, oldMsg.isUser, oldMsg.imageUrl, oldMsg.sources));
        }
        chatAdapter.notifyDataSetChanged();
        if (!messageList.isEmpty()) {
            rcvChat.scrollToPosition(messageList.size() - 1);
        }
        updateRobotVisibility(); // Update visibility after loading
    }

    private void handleUserMessage(String displayContent, String apiContent) {
        if (currentConversationId == -1) {
            Conversation newConv = new Conversation();
            newConv.title = displayContent;
            newConv.timestamp = System.currentTimeMillis();
            newConv.lastMessage = displayContent;
            currentConversationId = db.chatDao().insertConversation(newConv);
        } else {
            db.chatDao().updateLastMessage(currentConversationId, displayContent, System.currentTimeMillis());
        }

        ChatMessage userMsg = new ChatMessage();
        userMsg.conversationId = currentConversationId;
        userMsg.content = displayContent;
        userMsg.isUser = true;
        userMsg.timestamp = System.currentTimeMillis();
        db.chatDao().insertMessage(userMsg);

        sendMessage(displayContent, true, null, null);
        callRealApi(apiContent);
    }

    private void sendMessage(String content, boolean isUser, String imageUrl, String sources) {
        messageList.add(new Message(content, isUser, imageUrl, sources));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);
        updateRobotVisibility(); // Update visibility when a message is sent
    }

    private void showTyping() {
        messageList.add(new Message(true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);
        updateRobotVisibility(); // Also hide when typing indicator appears
    }
    
    // --- The new visibility logic ---
    private void updateRobotVisibility() {
        if (ivRobotMascot != null) {
            ivRobotMascot.setVisibility(messageList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void hideTyping() {
        if (!messageList.isEmpty()) {
            int lastIndex = messageList.size() - 1;
            if (messageList.get(lastIndex).isTyping()) {
                messageList.remove(lastIndex);
                chatAdapter.notifyItemRemoved(lastIndex);
            }
        }
        updateRobotVisibility(); // Ensure it's still hidden
    }

    private void callRealApi(String query) {
        setInputEnabled(false);
        showTyping();

        ChatRequest request = new ChatRequest(query);
        ChatRetrofitClient.getApiService().chatWithLucfin(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                setInputEnabled(true);
                hideTyping();

                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getAnswer();
                    String serverImage = response.body().getImage();
                    List<String> sourceDocs = response.body().getSourceDocuments();
                    
                    String sources = (sourceDocs != null && !sourceDocs.isEmpty()) ? TextUtils.join(", ", sourceDocs) : null;

                    if ("USE_LOCAL_IMAGE".equals(serverImage)) {
                        new Thread(() -> {
                            FoodHistory latest = db.foodDao().getLatestFood();
                            String localImagePath = null;

                            if (latest != null && latest.getImagePath() != null) {
                                long timeDiff = System.currentTimeMillis() - latest.getTimestamp();
                                if (timeDiff < 10 * 60 * 1000) { // 10 minutes
                                    localImagePath = latest.getImagePath();
                                }
                            }

                            String finalImg = localImagePath;
                            requireActivity().runOnUiThread(() -> {
                                saveAndDisplayMessage(answer, finalImg, sources);
                            });
                        }).start();
                    } else {
                        saveAndDisplayMessage(answer, serverImage, sources);
                    }
                } else {
                    saveAndDisplayMessage("Error: " + response.code(), null, null);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                setInputEnabled(true);
                hideTyping();
                saveAndDisplayMessage("Error: " + t.getMessage(), null, null);
            }
        });
    }

    private void saveAndDisplayMessage(String answer, String imageUrl, String sources) {
        if (currentConversationId != -1) {
            new Thread(() -> {
                ChatMessage botMsg = new ChatMessage();
                botMsg.conversationId = currentConversationId;
                botMsg.content = answer;
                botMsg.isUser = false;
                botMsg.timestamp = System.currentTimeMillis();
                botMsg.imageUrl = imageUrl;
                botMsg.sources = sources;
                db.chatDao().insertMessage(botMsg);
                db.chatDao().updateLastMessage(currentConversationId, answer, System.currentTimeMillis());
            }).start();
        }
        sendMessage(answer, false, imageUrl, sources);
    }

    private void setInputEnabled(boolean enabled) {
        if (etInput != null) etInput.setEnabled(enabled);
        if (btnSend != null) {
            btnSend.setEnabled(enabled);
            btnSend.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
}