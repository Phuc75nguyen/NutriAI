package com.example.nutriai.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// 1. TĂNG VERSION LÊN 5 (Hoặc số nào lớn hơn số hiện tại)
// 2. Thêm exportSchema = false để đỡ bị warning
@Database(entities = {Conversation.class, ChatMessage.class, FoodHistory.class, User.class}, version = 5, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatDao chatDao();
    public abstract FoodDao foodDao();
    public abstract UserDao userDao();

    private static volatile AppDatabase instance; // Thêm volatile cho an toàn đa luồng

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "nutriai_chat_db")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // Dòng này sẽ xóa DB cũ đi xây lại cái mới khi tăng version
                    .build();
        }
        return instance;
    }
}