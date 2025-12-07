package com.example.nutriai.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "conversations")
public class Conversation {
    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public String title; // Ví dụ: "Give me random names..."
    public String lastMessage; // Để hiển thị preview
    public long timestamp; // Để sắp xếp mới nhất lên đầu
    public String imageUrl; // Để hiển thị ảnh
    public String sources; // Để hiển thị nguồn
}
