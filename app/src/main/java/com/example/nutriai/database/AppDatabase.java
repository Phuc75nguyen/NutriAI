package com.example.nutriai.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Conversation.class, ChatMessage.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ChatDao chatDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "nutriai_chat_db")
                    .allowMainThreadQueries() // Lưu ý: Thực tế nên chạy background thread, nhưng để demo đơn giản ta cho phép chạy main
                    .build();
        }
        return instance;
    }
}