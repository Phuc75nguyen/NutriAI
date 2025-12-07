package com.example.nutriai.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatDao {
    // Lấy danh sách lịch sử chat, sắp xếp mới nhất
    @Query("SELECT * FROM conversations ORDER BY timestamp DESC")
    List<Conversation> getAllConversations();

    // Lấy tin nhắn của 1 cuộc hội thoại cụ thể
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesByConversation(long conversationId);

    // Tạo cuộc hội thoại mới -> trả về ID
    @Insert
    long insertConversation(Conversation conversation);

    // Lưu tin nhắn
    @Insert
    void insertMessage(ChatMessage message);

    // Cập nhật tin nhắn cuối cùng cho hội thoại
    @Query("UPDATE conversations SET lastMessage = :lastMsg, timestamp = :time WHERE id = :id")
    void updateLastMessage(long id, String lastMsg, long time);

    // Xóa một cuộc hội thoại
    @Delete
    void deleteConversation(Conversation conversation);
}