package com.example.Kalendar.models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "calendars",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "id",
                childColumns = "userId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("userId")}
)
public class CalendarEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public long createdAt;

    public String colorHex;

    public int userId;

    public CalendarEntity(String title, long createdAt, String colorHex, int userId) {
        this.title = title;
        this.createdAt = createdAt;
        this.colorHex = colorHex;
        this.userId = userId;
    }
}
