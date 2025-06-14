package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.UserDao;
import com.example.Kalendar.models.UserEntity;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UserRepository {

    private final UserDao userDao;
    private final Executor ioExecutor;

    @Inject
    public UserRepository(UserDao userDao, Executor ioExecutor) {
        this.userDao = userDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<UserEntity> getUserByUsername(String username) {
        return userDao.getUserByUsernameLiveData(username);
    }

    public void insert(UserEntity user) {
        ioExecutor.execute(() -> userDao.insert(user));
    }

    public void update(UserEntity user) {
        ioExecutor.execute(() -> userDao.update(user));
    }

    public void delete(UserEntity user) {
        ioExecutor.execute(() -> userDao.delete(user));
    }
}
