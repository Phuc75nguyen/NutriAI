package com.example.nutriai.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ScanRequest {
    // Dùng @SerializedName để đảm bảo gửi đúng tên field mà Python yêu cầu
    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("detected_classes")
    private List<String> detectedClasses;

    // Constructor
    public ScanRequest(String sessionId, List<String> detectedClasses) {
        this.sessionId = sessionId;
        this.detectedClasses = detectedClasses;
    }
}