package com.example.Kalendar.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "days", foreignKeys = @ForeignKey(entity = CalendarEntity.class,
        parentColumns = "id",
        childColumns = "calendarId",
        onDelete = CASCADE),
        indices = @Index("calendarId"))
public class DayEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private long timestamp;

    private int calendarId;
    private String awardType; // "cup", "medal", "gold_border", null

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(int calendarId) {
        this.calendarId = calendarId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAwardType() {
        return awardType;
    }

    public void setAwardType(String awardType) {
        this.awardType = awardType;
    }
}
