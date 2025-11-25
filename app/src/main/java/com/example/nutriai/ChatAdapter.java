package com.example.nutriai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = messageList.get(position);
        if (message.isUser()) {
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutBot.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getContent());
        } else {
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutBot.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBot, layoutUser;
        TextView tvBotMessage, tvUserMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBot = itemView.findViewById(R.id.layout_bot);
            layoutUser = itemView.findViewById(R.id.layout_user);
            tvBotMessage = itemView.findViewById(R.id.tv_bot_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
        }
    }
}