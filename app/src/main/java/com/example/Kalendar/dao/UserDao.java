package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.UserEntity;

import java.util.List;

@Dao
public interface UserDao {

    // ——— LiveData для UI ———

    /** LiveData: один пользователь по username (email) */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<UserEntity> getUserByUsernameLiveData(String username);

    /** LiveData: один пользователь по id */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserByIdLiveData(int userId);

    /** LiveData: все пользователи */
    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> getAllUsersLiveData();

    // ——— Синхронные методы для UseCase/репозиториев ———

    /** Синхронно вернуть одного пользователя по id (или null) */
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    UserEntity getById(int userId);

    /** Синхронно вернуть одного пользователя по username (или null) */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getByUsername(String username);

    /**
     * Вставить или заменить пользователя.
     * Возвращает сгенерированный ROWID (id) — нужно для insertSync в репозитории.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(UserEntity user);

    /**
     * Обновить пользователя.
     * Возвращает количество изменённых строк (может быть 0, если запись не найдена).
     */
    @Update
    int update(UserEntity user);

    /**
     * Удалить пользователя.
     * Возвращает количество удалённых строк.
     */
    @Delete
    int delete(UserEntity user);
}
