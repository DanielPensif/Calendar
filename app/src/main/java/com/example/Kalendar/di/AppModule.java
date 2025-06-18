package com.example.Kalendar.di;

import android.content.Context;

import androidx.room.Room;

import com.example.Kalendar.db.AppDatabase;
import com.example.Kalendar.dao.CalendarDao;
import com.example.Kalendar.dao.CategoryDao;
import com.example.Kalendar.dao.DayDao;
import com.example.Kalendar.dao.EventDao;
import com.example.Kalendar.dao.TaskDao;
import com.example.Kalendar.dao.UserDao;
import com.example.Kalendar.repository.CalendarRepository;
import com.example.Kalendar.repository.CategoryRepository;
import com.example.Kalendar.repository.DayRepository;
import com.example.Kalendar.repository.EventRepository;
import com.example.Kalendar.repository.TaskRepository;
import com.example.Kalendar.repository.UserRepository;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;

@Module
@InstallIn(SingletonComponent.class)
public class AppModule {

    // 1. Создаём AppDatabase как синглтон
    @Provides
    @Singleton
    public AppDatabase provideDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, AppDatabase.class, "calendar_db")
                .fallbackToDestructiveMigration()
                .build();
    }

    // 2. Из AppDatabase берём все DAO
    @Provides
    @Singleton
    public EventDao provideEventDao(AppDatabase db) {
        return db.eventDao();
    }

    @Provides
    @Singleton
    public TaskDao provideTaskDao(AppDatabase db) {
        return db.taskDao();
    }

    @Provides
    @Singleton
    public CalendarDao provideCalendarDao(AppDatabase db) {
        return db.calendarDao();
    }

    @Provides
    @Singleton
    public DayDao provideDayDao(AppDatabase db) {
        return db.dayDao();
    }

    @Provides
    @Singleton
    public CategoryDao provideCategoryDao(AppDatabase db) {
        return db.categoryDao();
    }

    @Provides
    @Singleton
    public UserDao provideUserDao(AppDatabase db) {
        return db.userDao();
    }

    // 3. Предоставляем Executor для асинхронных операций
    @Provides
    @Singleton
    public Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    // 4. Создаём репозитории, инжектя DAO + Executor
    @Provides
    @Singleton
    public EventRepository provideEventRepository(EventDao eventDao, DayDao dayDao, Executor executor) {
        return new EventRepository(eventDao, dayDao, executor);
    }

    @Provides
    @Singleton
    public TaskRepository provideTaskRepository(TaskDao taskDao, DayDao dayDao, Executor executor) {
        return new TaskRepository(taskDao, dayDao, executor);
    }

    @Provides
    @Singleton
    public CalendarRepository provideCalendarRepository(CalendarDao calendarDao, Executor executor) {
        return new CalendarRepository(calendarDao, executor);
    }

    @Provides
    @Singleton
    public DayRepository provideDayRepository(DayDao dayDao, TaskDao taskDao, EventDao eventDao, Executor executor) {
        return new DayRepository(dayDao, taskDao, eventDao, executor);
    }

    @Provides
    @Singleton
    public CategoryRepository provideCategoryRepository(CategoryDao categoryDao, Executor executor) {
        return new CategoryRepository(categoryDao, executor);
    }

    @Provides
    @Singleton
    public UserRepository provideUserRepository(UserDao userDao, Executor executor) {
        return new UserRepository(userDao, executor);
    }
}
