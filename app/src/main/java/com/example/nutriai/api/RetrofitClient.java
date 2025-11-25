package com.example.nutriai.api;

import java.util.concurrent.TimeUnit; // Import mới
import okhttp3.OkHttpClient; // Import mới
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    private static final String BASE_URL = "http://10.0.2.2:8000/";

    public static ApiService getApiService() {
        if (retrofit == null) {
            // 1. Tạo bộ cấu hình thời gian chờ (60 giây)
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(60, TimeUnit.SECONDS) // Thời gian chờ kết nối
                    .readTimeout(60, TimeUnit.SECONDS)    // Thời gian chờ server trả lời (Quan trọng nhất cho AI)
                    .writeTimeout(60, TimeUnit.SECONDS)   // Thời gian chờ gửi dữ liệu
                    .build();

            // 2. Gắn vào Retrofit
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // <-- Thêm dòng này để áp dụng cấu hình trên
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
}