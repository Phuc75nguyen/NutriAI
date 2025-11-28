package com.example.nutriai;

import android.content.Context; // Import mới
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

<<<<<<< HEAD
import io.noties.markwon.Markwon; // Import thư viện Markwon
=======
import com.bumptech.glide.Glide;

>>>>>>> 26670c1 (update UI Lucfin markdown image)
import java.util.List;

import io.noties.markwon.Markwon;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

<<<<<<< HEAD
    private List<Message> mListMessage;
    private Markwon markwon; // Khai báo đối tượng Markwon

    // --- CẬP NHẬT 1: Constructor nhận thêm Context ---
    public ChatAdapter(Context context, List<Message> mListMessage) {
        this.mListMessage = mListMessage;
        // Khởi tạo Markwon 1 lần duy nhất tại đây để tối ưu hiệu năng
        this.markwon = Markwon.create(context);
=======
    private final List<Message> messageList;
    private final Markwon markwon;

    public ChatAdapter(List<Message> messageList, Markwon markwon) {
        this.messageList = messageList;
        this.markwon = markwon;
>>>>>>> 26670c1 (update UI Lucfin markdown image)
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
<<<<<<< HEAD
            holder.layoutUser.setVisibility(View.GONE);

            // --- CẬP NHẬT 2: Dùng Markwon để hiển thị ---
            // Nó sẽ tự động biến **text** thành in đậm, - thành gạch đầu dòng...
            markwon.setMarkdown(holder.tvBotMsg, message.getContent());
=======
            
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
>>>>>>> 26670c1 (update UI Lucfin markdown image)
        }
    }

    @Override
    public int getItemCount() {
        return mListMessage != null ? mListMessage.size() : 0;
    }

<<<<<<< HEAD
    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutUser, layoutBot;
        TextView tvUserMsg, tvBotMsg;
=======
    static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutBot, layoutUser;
        TextView tvBotMessage, tvUserMessage, tvSources;
        ImageView ivBotImage;
>>>>>>> 26670c1 (update UI Lucfin markdown image)

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUser = itemView.findViewById(R.id.layout_user);
<<<<<<< HEAD
            layoutBot = itemView.findViewById(R.id.layout_bot);
            tvUserMsg = itemView.findViewById(R.id.tv_user_message);
            tvBotMsg = itemView.findViewById(R.id.tv_bot_message);
=======
            tvBotMessage = itemView.findViewById(R.id.tv_bot_message);
            tvUserMessage = itemView.findViewById(R.id.tv_user_message);
            ivBotImage = itemView.findViewById(R.id.iv_bot_image);
            tvSources = itemView.findViewById(R.id.tv_sources);
>>>>>>> 26670c1 (update UI Lucfin markdown image)
        }
    }
}