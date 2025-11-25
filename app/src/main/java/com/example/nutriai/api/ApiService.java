package com.example.nutriai.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("ask")
    Call<ChatResponse> chatWithLucfin(@Body ChatRequest request);
}
