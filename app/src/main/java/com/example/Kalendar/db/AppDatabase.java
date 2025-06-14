package com.example.Kalendar.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.Kalendar.dao.CalendarDao;
import com.example.Kalendar.dao.CategoryDao;
import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.dao.UserDao;
import com.example.Kalendar.models.CalendarEntity;
import com.example.Kalendar.models.CategoryEntity;
import com.example.Kalendar.models.DayEntity;
import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.models.UserEntity;

@Database(entities = {DayEntity.class, TaskEntity.class, EventEntity.class, CalendarEntity.class, UserEntity.class, CategoryEntity.class}, version = 3)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract DayDao dayDao();
    public abstract TaskDao taskDao();
    public abstract EventDao eventDao();

    public abstract CalendarDao calendarDao();
    public abstract UserDao userDao();
    public abstract CategoryDao categoryDao();

    public static AppDatabase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "calendar_db")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}

