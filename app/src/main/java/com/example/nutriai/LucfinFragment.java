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

import com.example.nutriai.api.ChatRequest;
import com.example.nutriai.api.ChatResponse;
import com.example.nutriai.api.ChatRetrofitClient;
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.ChatMessage;
import com.example.nutriai.database.Conversation;

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

        // Initialize DB
        db = AppDatabase.getInstance(requireContext());

        // Initialize Views
        rcvChat = view.findViewById(R.id.rcv_chat);
        etInput = view.findViewById(R.id.et_input);
        btnSend = view.findViewById(R.id.btn_send);
        ImageView btnHistory = view.findViewById(R.id.iv_chat_icon);

        // Setup History Button
        btnHistory.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatHistoryActivity.class);
            startActivity(intent);
        });

        // Setup Markwon
        Markwon markwon = Markwon.create(requireContext());

        // Setup RecyclerView
        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList, markwon);
        rcvChat.setLayoutManager(new LinearLayoutManager(getContext()));
        rcvChat.setAdapter(chatAdapter);

        // --- FIX: Check for arguments from Bundle, not Activity Intent ---
        if (getArguments() != null) {
            long conversationId = getArguments().getLong("CONVERSATION_ID", -1);
            if (conversationId != -1) {
                currentConversationId = conversationId;
                loadMessagesFromDb(currentConversationId);
            }
        }

        // Handle Send Button
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (!question.isEmpty()) {
                handleUserMessage(question);
                etInput.setText("");
            }
        });
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
    }

    private void handleUserMessage(String content) {
        // 1. Create conversation if new
        if (currentConversationId == -1) {
            Conversation newConv = new Conversation();
            newConv.title = content; // Use first message as title
            newConv.timestamp = System.currentTimeMillis();
            newConv.lastMessage = content;
            currentConversationId = db.chatDao().insertConversation(newConv);
        } else {
            // Update last message
            db.chatDao().updateLastMessage(currentConversationId, content, System.currentTimeMillis());
        }

        // 2. Save User Message
        ChatMessage userMsg = new ChatMessage();
        userMsg.conversationId = currentConversationId;
        userMsg.content = content;
        userMsg.isUser = true;
        userMsg.timestamp = System.currentTimeMillis();
        db.chatDao().insertMessage(userMsg);

        // 3. Update UI and Call API
        sendMessage(content, true, null, null);
    }

    private void sendMessage(String content, boolean isUser, String imageUrl, String sources) {
        messageList.add(new Message(content, isUser, imageUrl, sources));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);

        if (isUser) {
            callRealApi(content);
        }
    }

    private void showTyping() {
        messageList.add(new Message(true)); // Add typing message
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);
    }

    private void hideTyping() {
        if (!messageList.isEmpty()) {
            int lastIndex = messageList.size() - 1;
            if (messageList.get(lastIndex).isTyping()) {
                messageList.remove(lastIndex);
                chatAdapter.notifyItemRemoved(lastIndex);
            }
        }
    }

    private void callRealApi(String query) {
        setInputEnabled(false);
        showTyping(); // Show typing indicator
        
        ChatRequest request = new ChatRequest(query);
        ChatRetrofitClient.getApiService().chatWithLucfin(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                setInputEnabled(true);
                hideTyping(); // Hide typing indicator
                
                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getAnswer();
                    String image = response.body().getImage();
                    List<String> sourceDocs = response.body().getSourceDocuments();
                    
                    String sources = null;
                    if (sourceDocs != null && !sourceDocs.isEmpty()) {
                        sources = TextUtils.join(", ", sourceDocs);
                    }

                    // Save Bot Message to DB
                    if (currentConversationId != -1) {
                        ChatMessage botMsg = new ChatMessage();
                        botMsg.conversationId = currentConversationId;
                        botMsg.content = answer;
                        botMsg.isUser = false;
                        botMsg.timestamp = System.currentTimeMillis();
                        botMsg.imageUrl = image; // Save image
                        botMsg.sources = sources; // Save sources
                        db.chatDao().insertMessage(botMsg);
                        
                        db.chatDao().updateLastMessage(currentConversationId, answer, System.currentTimeMillis());
                    }

                    sendMessage(answer, false, image, sources);
                } else {
                    sendMessage("Error: " + response.code(), false, null, null);
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                setInputEnabled(true);
                hideTyping(); // Hide typing indicator
                
                sendMessage("Error: " + t.getMessage(), false, null, null);
            }
        });
    }

    private void setInputEnabled(boolean enabled) {
        if (etInput != null) {
            etInput.setEnabled(enabled);
        }
        if (btnSend != null) {
            btnSend.setEnabled(enabled);
            btnSend.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
}