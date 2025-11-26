package com.example.nutriai;

import android.content.Context; // Import mới
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import io.noties.markwon.Markwon; // Import thư viện Markwon
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Message> mListMessage;
    private Markwon markwon; // Khai báo đối tượng Markwon

    // --- CẬP NHẬT 1: Constructor nhận thêm Context ---
    public ChatAdapter(Context context, List<Message> mListMessage) {
        this.mListMessage = mListMessage;
        // Khởi tạo Markwon 1 lần duy nhất tại đây để tối ưu hiệu năng
        this.markwon = Markwon.create(context);
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Message message = mListMessage.get(position);
        if (message == null) return;

        if (message.isUser()) {
            // User: Hiển thị bình thường (vì User ít khi gõ markdown)
            holder.layoutUser.setVisibility(View.VISIBLE);
            holder.layoutBot.setVisibility(View.GONE);
            holder.tvUserMsg.setText(message.getContent());
        } else {
            // Bot: Hiển thị & Render Markdown
            holder.layoutBot.setVisibility(View.VISIBLE);
            holder.layoutUser.setVisibility(View.GONE);

            // --- CẬP NHẬT 2: Dùng Markwon để hiển thị ---
            // Nó sẽ tự động biến **text** thành in đậm, - thành gạch đầu dòng...
            markwon.setMarkdown(holder.tvBotMsg, message.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return mListMessage != null ? mListMessage.size() : 0;
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUser, layoutBot;
        TextView tvUserMsg, tvBotMsg;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUser = itemView.findViewById(R.id.layout_user);
            layoutBot = itemView.findViewById(R.id.layout_bot);
            tvUserMsg = itemView.findViewById(R.id.tv_user_message);
            tvBotMsg = itemView.findViewById(R.id.tv_bot_message);
        }
    }
}