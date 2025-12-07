package com.example.nutriai;

public class Message {
    private String content;
    private boolean isUser;
    private String imageUrl; // Cho tin nhắn bot có ảnh
    private String sources;  // Cho tin nhắn bot có nguồn

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
}