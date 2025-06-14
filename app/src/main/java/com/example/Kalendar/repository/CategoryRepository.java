package com.example.Kalendar.repository;

import androidx.lifecycle.LiveData;

import com.example.Kalendar.dao.CategoryDao;
import com.example.Kalendar.models.CategoryEntity;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class CategoryRepository {

    private final CategoryDao categoryDao;
    private final Executor ioExecutor;

    @Inject
    public CategoryRepository(CategoryDao categoryDao, Executor ioExecutor) {
        this.categoryDao = categoryDao;
        this.ioExecutor = ioExecutor;
    }

    public LiveData<List<CategoryEntity>> getAllCategories(int userId) {
        return categoryDao.getCategoriesForUserLiveData(userId);
    }

    public void insert(CategoryEntity category) {
        ioExecutor.execute(() -> categoryDao.insert(category));
    }

    public void update(CategoryEntity category) {
        ioExecutor.execute(() -> categoryDao.update(category));
    }

    public void delete(CategoryEntity category) {
        ioExecutor.execute(() -> categoryDao.delete(category));
    }
}
