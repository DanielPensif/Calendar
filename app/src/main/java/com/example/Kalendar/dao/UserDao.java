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

    // Найти пользователя по email/username
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    LiveData<UserEntity> getUserByUsernameLiveData(String username);

    // Получить пользователя по id (LiveData)
    @Query("SELECT * FROM users WHERE id = :userId LIMIT 1")
    LiveData<UserEntity> getUserByIdLiveData(int userId);

    // Все пользователи (например, админская выборка) – не обязательно, но может пригодиться
    @Query("SELECT * FROM users")
    LiveData<List<UserEntity>> getAllUsersLiveData();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    @Update
    void update(UserEntity user);

    @Delete
    void delete(UserEntity user);
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    UserEntity getUserByUsername(String username);
    @Query("SELECT * FROM users WHERE username = :username")
    UserEntity getByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(int id);
}
