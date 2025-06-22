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

    // Асинхронная LiveData для UI
    public LiveData<UserEntity> getUserByUsername(String username) {
        return userDao.getUserByUsernameLiveData(username);
    }

    public LiveData<UserEntity> getUser(int userId) {
        return userDao.getUserByIdLiveData(userId);
    }

    // Синхронный метод для UseCase
    public UserEntity getByIdSync(int userId) {
        return userDao.getById(userId);
    }

    public UserEntity getByUsernameSync(String username) {
        return userDao.getByUsername(username);
    }

    // Синхронная вставка, возвращает сгенерированный id или брошенное исключение
    public long insertSync(UserEntity user) {
        return userDao.insert(user);
    }

    // Оставляем ваши асинхронные обёртки для UI, если нужно
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
