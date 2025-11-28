package com.example.nutriai;

public class Message {
    private String content;
    private boolean isUser;
    private String imageUrl;
    private String sources;

    public Message(String content, boolean isUser) {
        this.content = content;
        this.isUser = isUser;
    }

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