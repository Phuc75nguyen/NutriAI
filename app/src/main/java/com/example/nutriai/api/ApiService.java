package com.example.nutriai.api;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
    @POST("ask")
    Call<ChatResponse> chatWithLucfin(@Body ChatRequest request);

    @Multipart
    @POST("analyze-image") // Make sure this matches the Colab endpoint
    Call<ChatMessageResponse> analyzeImage(@Part MultipartBody.Part image);
}
