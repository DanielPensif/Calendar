package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.Kalendar.models.UserEntity;

@Dao
public interface UserDao {
    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE username = :username")
    UserEntity getByUsername(String username);

    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getById(int id);

    @Update
    void update(UserEntity user);
}
