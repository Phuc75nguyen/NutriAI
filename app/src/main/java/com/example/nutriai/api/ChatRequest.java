package com.example.nutriai.api;

public class ChatRequest {
    private String question;

    public ChatRequest(String query) {
        this.question = query;
    }

    public String getQuery() {
        return question;
    }

    public void setQuery(String query) {
        this.question = query;
    }
}
