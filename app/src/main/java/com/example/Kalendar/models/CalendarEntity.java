package com.example.Kalendar.models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "calendars")
public class CalendarEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long createdAt;

    public String colorHex;

    public CalendarEntity(String title, long createdAt, String colorHex) {
        this.title = title;
        this.createdAt = createdAt;
        this.colorHex = colorHex;
    }
}
