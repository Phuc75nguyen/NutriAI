package com.example.nutriai.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Conversation.class, ChatMessage.class, FoodHistory.class}, version = 3)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatDao chatDao();
    public abstract FoodDao foodDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "nutriai_chat_db")
                    .allowMainThreadQueries() // Cho phép chạy trên main thread (để đơn giản hóa)
                    .fallbackToDestructiveMigration() // Tự động xóa và tạo lại DB nếu schema thay đổi
                    .build();
        }
        return instance;
    }
}