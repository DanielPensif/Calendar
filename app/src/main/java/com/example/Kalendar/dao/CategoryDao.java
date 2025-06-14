package com.example.Kalendar.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import com.example.Kalendar.models.CategoryEntity;

import java.util.List;

@Dao
public interface CategoryDao {

    // Получить все категории конкретного пользователя
    @Query("SELECT * FROM categories WHERE userId = :userId")
    LiveData<List<CategoryEntity>> getCategoriesForUserLiveData(int userId);

    // Получить одну категорию по её id
    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    LiveData<CategoryEntity> getCategoryByIdLiveData(int categoryId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CategoryEntity category);

    @Update
    void update(CategoryEntity category);

    @Delete
    void delete(CategoryEntity category);

    // Нужен «синхронный» вариант, если в репозитории захочется получить сразу объект:
    @Query("SELECT * FROM categories WHERE id = :categoryId LIMIT 1")
    CategoryEntity getCategoryById(int categoryId);
    @Query("SELECT * FROM categories WHERE userId = :userId ORDER BY id ASC")
    List<CategoryEntity> getAllForUser(int userId);

    @Query("SELECT * FROM categories WHERE name = :name AND userId = :userId LIMIT 1")
    CategoryEntity getByNameAndUserId(String name, int userId);
    @Query("SELECT id FROM categories WHERE name = 'Без категории' AND userId = :userId LIMIT 1")
    int getDefaultCategoryId(int userId);
    @Query("UPDATE tasks SET category = (SELECT name FROM categories WHERE id = :defaultId) WHERE category = :oldName")
    void reassignCategoryInTasks(String oldName, int defaultId);
    @Query(" UPDATE events SET category = (SELECT name FROM categories WHERE id = :defaultId) WHERE category = :oldName")
    void reassignCategoryInEvents(String oldName, int defaultId);

    @Transaction
    default void reassignToDefault(String oldName, int userId) {
        int defId = getDefaultCategoryId(userId);
        reassignCategoryInTasks(oldName, defId);
        reassignCategoryInEvents(oldName, defId);
    }
}
