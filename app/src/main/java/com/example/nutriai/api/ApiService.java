package com.example.nutriai.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {

    // API Chat
    @POST("ask")
    Call<ChatResponse> chatWithLucfin(@Body ChatRequest request);

    // API Computer Vision
    @Multipart
    @POST("predict")
    Call<FoodAnalysisResponse> analyzeImage(@Part MultipartBody.Part file);

    // API Đồng bộ (Server Python yêu cầu endpoint là "scan")
    @POST("scan")
    Call<Void> syncScanData(@Body ScanRequest request);
}