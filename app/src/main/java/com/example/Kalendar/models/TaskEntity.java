package com.example.Kalendar.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        foreignKeys = @ForeignKey(entity = DayEntity.class,
                parentColumns = "id",
                childColumns = "dayId",
                onDelete = CASCADE),
        indices = @Index("dayId"))
public class TaskEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public boolean done;

    public String category; // "Учёба", "Работа", "Быт", ...

    public String comment;         // описание задачи
    public String reviewComment;
    public String completionDate;
    public String completionTime;
    public Integer rating;


    public int dayId;
    public int calendarId;
    public String doneReason;

}

