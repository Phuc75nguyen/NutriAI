package com.example.nutriai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private final List<Message> messageList;
    private final Markwon markwon;

    public ChatAdapter(List<Message> messageList, Markwon markwon) {
        this.messageList = messageList;
        this.markwon = markwon;
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

        if (message.isTyping()) {
            holder.layoutTyping.setVisibility(View.VISIBLE);
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutBot.setVisibility(View.GONE);
        } else if (message.isUser()) {
            holder.layoutTyping.setVisibility(View.GONE);
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutBot.setVisibility(View.GONE);
            holder.tvUserMessage.setText(message.getContent());
        } else {
            holder.layoutTyping.setVisibility(View.GONE);
            holder.layoutUser.setVisibility(View.GONE);
            holder.layoutBot.setVisibility(View.VISIBLE);

            // Render Markdown
            markwon.setMarkdown(holder.tvBotMessage, message.getContent());

            // Image Logic
            if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
                holder.ivBotImage.setVisibility(View.VISIBLE);
                Glide.with(holder.itemView.getContext())
                        .load(message.getImageUrl())
                        .into(holder.ivBotImage);
            } else {
                holder.ivBotImage.setVisibility(View.GONE);
            }

            // Source Logic
            if (message.getSources() != null && !message.getSources().isEmpty()) {
                holder.tvSources.setVisibility(View.VISIBLE);
                holder.tvSources.setText("Sources: " + message.getSources());
            } else {
                holder.tvSources.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBot, layoutUser, layoutTyping;
        TextView tvBotMessage, tvUserMessage, tvSources;
        ImageView ivBotImage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutBot = itemView.findViewById(R.id.layout_bot);
            layoutUser = itemView.findViewById(R.id.layout_user);
            layoutTyping = itemView.findViewById(R.id.layout_typing); // New typing layout
            tvBotMessage = itemView.findViewById(R.id.tv_bot_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            ivBotImage = itemView.findViewById(R.id.iv_bot_image);
            tvSources = itemView.findViewById(R.id.tv_sources);
        }
    }
}