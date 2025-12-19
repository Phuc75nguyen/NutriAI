package com.example.nutriai;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.nutriai.database.FoodHistory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FoodHistoryAdapter extends RecyclerView.Adapter<FoodHistoryAdapter.ViewHolder> {

    private List<FoodHistory> list;

    public FoodHistoryAdapter(List<FoodHistory> list) {
        this.list = list;
    }

    public void setData(List<FoodHistory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_food_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FoodHistory item = list.get(position);

        holder.tvFoodName.setText(item.getFoodName());
        holder.tvFoodWeight.setText(item.getFoodWeight());
        holder.tvSummaryContent.setText(item.getSummary());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault());
        holder.tvTimestamp.setText(sdf.format(new Date(item.getTimestamp())));

        if (item.getImagePath() != null) {
            File imgFile = new File(item.getImagePath());
            if (imgFile.exists()) {
                Glide.with(holder.itemView.getContext())
                        .load(imgFile)
                        .into(holder.ivFoodImage);
            } else {
                holder.ivFoodImage.setImageResource(R.drawable.salad_image); // Default placeholder
            }
        } else {
            holder.ivFoodImage.setImageResource(R.drawable.salad_image);
        }

        // Handle Expand/Collapse
        holder.btnExpand.setOnClickListener(v -> {
            if (holder.tvSummaryContent.getVisibility() == View.VISIBLE) {
                holder.tvSummaryContent.setVisibility(View.GONE);
                holder.btnExpand.setRotation(-90);
            } else {
                holder.tvSummaryContent.setVisibility(View.VISIBLE);
                holder.btnExpand.setRotation(90);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTimestamp, tvFoodName, tvFoodWeight, tvSummaryContent;
        ImageView ivFoodImage;
        ImageButton btnExpand;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTimestamp = itemView.findViewById(R.id.tv_item_timestamp);
            tvFoodName = itemView.findViewById(R.id.tv_item_food_name);
            tvFoodWeight = itemView.findViewById(R.id.tv_item_food_weight);
            tvSummaryContent = itemView.findViewById(R.id.tv_item_summary_content);
            ivFoodImage = itemView.findViewById(R.id.iv_item_food_image);
            btnExpand = itemView.findViewById(R.id.btn_expand);
        }
    }
}
