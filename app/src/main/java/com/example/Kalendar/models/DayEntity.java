package com.example.Kalendar.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity(tableName = "days", foreignKeys = @ForeignKey(entity = CalendarEntity.class,
        parentColumns = "id",
        childColumns = "calendarId",
        onDelete = CASCADE))
public class DayEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public int calendarId;

    public long timestamp; // UNIX-время
    public String awardType; // "cup", "medal", "gold_border", null
}
