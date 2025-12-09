package com.example.nutriai;

public class Message {
    private String content;
    private boolean isUser;
    private String imageUrl; // Cho tin nhắn bot có ảnh
    private String sources;  // Cho tin nhắn bot có nguồn
    private boolean isTyping; // Trạng thái đang nhập

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

    // Constructor đầy đủ (dùng khi load từ DB hoặc API trả về)
    public Message(String content, boolean isUser, String imageUrl, String sources) {
        this.content = content;
        this.isUser = isUser;
        this.imageUrl = imageUrl;
        this.sources = sources;
    }

    // Constructor cho trạng thái typing
    public Message(boolean isTyping) {
        this.isTyping = isTyping;
        this.isUser = false; // Typing indicator luôn là của bot
    }

    public String getContent() {
        return content;
    }

    public boolean isUser() {
        return isUser;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getSources() {
        return sources;
    }

    public boolean isTyping() {
        return isTyping;
    }
}