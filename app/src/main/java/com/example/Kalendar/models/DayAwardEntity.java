package com.example.Kalendar.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.CASCADE;

@Entity(tableName = "day_awards",
        foreignKeys = @ForeignKey(
                entity = DayEntity.class,
                parentColumns = "id",
                childColumns = "dayId",
                onDelete = CASCADE))
public class DayAwardEntity {

    @PrimaryKey
    public int dayId;

    @NonNull
    public String awardType; // "cup", "medal", "gold_border"
}
