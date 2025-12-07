package com.example.nutriai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.nutriai.database.Conversation;
import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<Conversation> list;
    private final OnItemClickListener listener;
    private final OnDeleteClickListener deleteListener;

    public void updateData(List<Conversation> conversations) {
    }

    public interface OnItemClickListener {
        void onItemClick(Conversation conversation);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(Conversation conversation);
    }

    public HistoryAdapter(List<Conversation> list, OnItemClickListener listener, OnDeleteClickListener deleteListener) {
        this.list = list;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Conversation item = list.get(position);
        holder.tvTitle.setText(item.title);
        holder.tvPreview.setText(item.lastMessage);
        
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item));

        holder.btnMore.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(holder.itemView.getContext(), holder.btnMore);
            popup.inflate(R.menu.conversation_item_menu);
            popup.setOnMenuItemClickListener(menuItem -> {
                if (menuItem.getItemId() == R.id.action_delete) {
                    deleteListener.onDeleteClick(item);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPreview;
        ImageView btnMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_history_title);
            tvPreview = itemView.findViewById(R.id.tv_history_preview);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}