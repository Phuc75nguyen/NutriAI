package com.example.nutriai;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutriai.database.AppDatabase;
import com.example.nutriai.database.Conversation;
import java.util.List;

public class ChatHistoryActivity extends AppCompatActivity {

    private RecyclerView rcvHistory;
    private HistoryAdapter adapter;
    private TextView tvEmpty;
    private AppDatabase db;
    private List<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        rcvHistory = findViewById(R.id.rcv_history);
        tvEmpty = findViewById(R.id.tv_empty);
        findViewById(R.id.btn_back_history).setOnClickListener(v -> finish());
        
        db = AppDatabase.getInstance(this);

        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        rcvHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadHistory() {
        conversations = db.chatDao().getAllConversations();

        if (conversations.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rcvHistory.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rcvHistory.setVisibility(View.VISIBLE);
        }

        if (adapter == null) {
            adapter = new HistoryAdapter(conversations, this::openConversation, this::showDeleteConfirmationDialog);
            rcvHistory.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged(); // This is not the most efficient, but simple for now.
        }
    }

    private void openConversation(Conversation conversation) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("IS_HISTORY", true);
        intent.putExtra("CONVERSATION_ID", conversation.id);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    private void showDeleteConfirmationDialog(Conversation conversation) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Conversation")
                .setMessage("Are you sure you want to delete this conversation?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteConversation(conversation);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteConversation(Conversation conversation) {
        db.chatDao().deleteConversation(conversation);
        loadHistory(); // Reload the list from the database
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadHistory(); // Reload when returning to the activity
    }
}
