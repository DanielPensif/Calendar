package com.example.Kalendar.viewmodel;

import com.example.Kalendar.models.TaskEntity;
import com.example.Kalendar.models.EventEntity;

import java.util.List;

public class DayContent {
    public final List<TaskEntity> tasks;
    public final List<EventEntity> events;

    public DayContent(List<TaskEntity> tasks, List<EventEntity> events) {
        this.tasks = tasks;
        this.events = events;
    }
}