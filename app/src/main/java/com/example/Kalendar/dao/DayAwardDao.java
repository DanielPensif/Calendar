package com.example.Kalendar.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Delete;

import com.example.Kalendar.models.DayAwardEntity;

import java.util.List;

@Dao
public interface DayAwardDao {

    @Insert
    long insert(DayAwardEntity award);

    @Update
    void update(DayAwardEntity award);

    @Delete
    void delete(DayAwardEntity award);

    @Query("SELECT * FROM day_awards")
    List<DayAwardEntity> getAll();
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAward(DayAwardEntity award);

    @Query("SELECT * FROM day_awards WHERE dayId = :dayId LIMIT 1")
    DayAwardEntity getAwardByDayId(int dayId);
}
