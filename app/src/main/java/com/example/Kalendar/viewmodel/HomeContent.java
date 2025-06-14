package com.example.Kalendar.viewmodel;

import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;

import java.util.List;

public class HomeContent {
    public final List<TaskEntity> tasks;
    public final List<EventEntity> events;

    public HomeContent(List<TaskEntity> t, List<EventEntity> e) {
        this.tasks = t;
        this.events = e;
    }
}
