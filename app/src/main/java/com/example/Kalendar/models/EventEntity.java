package com.example.Kalendar.models;

import static androidx.room.ForeignKey.CASCADE;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "events",
        foreignKeys = @ForeignKey(entity = DayEntity.class,
                parentColumns = "id",
                childColumns = "dayId",
                onDelete = CASCADE),
        indices = @Index("dayId"))
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String timeStart; // формат: HH:mm
    public String timeEnd;
    public boolean allDay;

    public String repeatRule; // NONE, DAILY, WEEKLY
    public String category;   // "Встреча", "Звонок", "Личное", ...

    public String description;
    public String location;

    public boolean done;

    public String excludedDates; // например: "2025-04-21,2025-04-28"

    public int dayId;
    public int calendarId;
    public boolean notifyOnStart;
    public boolean earlyReminderEnabled;
    public int earlyReminderHour;
    public int earlyReminderMinute;
    public int userId;

    @Ignore
    public String date; // используется для виртуальных экземпляров повторов

}


