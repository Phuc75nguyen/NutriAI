package com.example.nutriai.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "messages",
        foreignKeys = @ForeignKey(entity = Conversation.class,
                                  parentColumns = "id",
                                  childColumns = "conversationId",
                                  onDelete = CASCADE))
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public long id;
    
    public long conversationId; // Khóa ngoại liên kết với Conversation
    public String content;
    public boolean isUser; // true: người dùng, false: bot
    public long timestamp;
    public String imageUrl;
    public String sources;
}
