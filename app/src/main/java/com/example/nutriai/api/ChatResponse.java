package com.example.nutriai.api;

import java.util.List;

public class ChatResponse {
    private String answer;
    private String image;
    private List<String> sourceDocuments;

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<String> getSourceDocuments() {
        return sourceDocuments;
    }

    public void setSourceDocuments(List<String> sourceDocuments) {
        this.sourceDocuments = sourceDocuments;
    }
}