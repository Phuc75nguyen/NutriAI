package com.example.nutriai.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class FoodAnalysisResponse {

    @SerializedName("status")
    private String status;

    @SerializedName("detections")
    private List<Detection> detections;

    @SerializedName("image_base64")
    private String image_base64;

    public String getStatus() {
        return status;
    }

    public List<Detection> getDetections() {
        return detections;
    }

    public String getImage_base64() {
        return image_base64;
    }

    public static class Detection {
        @SerializedName("class_id")
        private int class_id;

        @SerializedName("class_name")
        private String class_name;

        @SerializedName("confidence")
        private double confidence;

        @SerializedName("bbox")
        private List<Double> bbox;

        public int getClass_id() { return class_id; }
        public String getClass_name() { return class_name; }
        public double getConfidence() { return confidence; }
        public List<Double> getBbox() { return bbox; }
    }
}
