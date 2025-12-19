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

        if (getArguments() != null) {
            long conversationId = getArguments().getLong("CONVERSATION_ID", -1);
            if (conversationId != -1) {
                currentConversationId = conversationId;
                loadMessagesFromDb(currentConversationId);
            }
        }

        // --- XỬ LÝ NÚT GỬI (LOGIC MỚI: ẨN CONTEXT) ---
        btnSend.setOnClickListener(v -> {
            String question = etInput.getText().toString().trim();
            if (question.isEmpty()) return;

            String lowerMsg = question.toLowerCase();
            boolean isAskingHistory = lowerMsg.contains("vừa ăn") ||
                    lowerMsg.contains("hôm nay ăn") ||
                    lowerMsg.contains("nãy ăn") ||
                    lowerMsg.contains("ăn gì");

            if (isAskingHistory) {
                new Thread(() -> {
                    FoodHistory latestFood = db.foodDao().getLatestFood();
                    String apiPayload = question; // Mặc định gửi câu hỏi gốc

                    if (latestFood != null) {
                        // Tạo context để gửi Server (User KHÔNG nhìn thấy cái này)
                        String contextInfo = "\n\n(System Context: Người dùng vừa ăn món: " + latestFood.getFoodName()
                                + ". Tổng calo: " + (int)latestFood.getCalories() + " kcal"
                                + ". Protein: " + latestFood.getProtein() + "g"
                                + ". Hãy trả lời câu hỏi dựa trên thông tin này.)";

                        apiPayload = question + contextInfo;
                    }

                    String finalApiPayload = apiPayload;
                    requireActivity().runOnUiThread(() -> {
                        // Gọi hàm đặc biệt: Hiển thị 1 đằng, Gửi 1 nẻo
                        handleUserMessage(question, finalApiPayload);
                        etInput.setText("");
                    });
                }).start();
            } else {
                // Chat bình thường: Hiển thị và Gửi giống nhau
                handleUserMessage(question, question);
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

    // --- HÀM XỬ LÝ TIN NHẮN USER (QUAN TRỌNG: Tách biệt nội dung hiển thị & gửi đi) ---
    private void handleUserMessage(String displayContent, String apiContent) {
        // 1. Lưu vào DB & Hiển thị (Dùng displayContent - NGẮN GỌN, SẠCH SẼ)
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
        userMsg.content = displayContent; // Lưu câu hỏi ngắn vào DB
        userMsg.isUser = true;
        userMsg.timestamp = System.currentTimeMillis();
        db.chatDao().insertMessage(userMsg);

        // Cập nhật UI (User chỉ thấy câu hỏi ngắn)
        sendMessage(displayContent, true, null, null);

        // 2. Gửi API (Dùng apiContent - CÓ KÈM CONTEXT)
        callRealApi(apiContent);
    }

    // Hàm cập nhật giao diện (Đã xóa bỏ việc gọi API ở đây để tránh lặp)
    private void sendMessage(String content, boolean isUser, String imageUrl, String sources) {
        messageList.add(new Message(content, isUser, imageUrl, sources));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        rcvChat.scrollToPosition(messageList.size() - 1);
    }

    private void showTyping() {
        messageList.add(new Message(true));
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
        showTyping();

        ChatRequest request = new ChatRequest(query);
        ChatRetrofitClient.getApiService().chatWithLucfin(request).enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                setInputEnabled(true);
                hideTyping();

                if (response.isSuccessful() && response.body() != null) {
                    String answer = response.body().getAnswer();
                    String image = response.body().getImage();
                    List<String> sourceDocs = response.body().getSourceDocuments();

                    String sources = null;
                    if (sourceDocs != null && !sourceDocs.isEmpty()) {
                        sources = TextUtils.join(", ", sourceDocs);
                    }

                    if (currentConversationId != -1) {
                        ChatMessage botMsg = new ChatMessage();
                        botMsg.conversationId = currentConversationId;
                        botMsg.content = answer;
                        botMsg.isUser = false;
                        botMsg.timestamp = System.currentTimeMillis();
                        botMsg.imageUrl = image;
                        botMsg.sources = sources;
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
                hideTyping();
                sendMessage("Error: " + t.getMessage(), false, null, null);
            }
        });
    }

    private void setInputEnabled(boolean enabled) {
        if (etInput != null) etInput.setEnabled(enabled);
        if (btnSend != null) {
            btnSend.setEnabled(enabled);
            btnSend.setAlpha(enabled ? 1.0f : 0.5f);
        }
    }
}