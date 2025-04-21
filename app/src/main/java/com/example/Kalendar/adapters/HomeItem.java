package com.example.Kalendar.adapters;

import com.example.Kalendar.models.EventEntity;
import com.example.Kalendar.models.TaskEntity;

public abstract class HomeItem {

    public static class Header extends HomeItem {
        public final String title;

        public Header(String title) {
            this.title = title;
        }
    }

    public static class EventItem extends HomeItem {
        public final EventEntity event;

        public EventItem(EventEntity event) {
            this.event = event;
        }
    }

    public static class TaskItem extends HomeItem {
        public final TaskEntity task;

        public TaskItem(TaskEntity task) {
            this.task = task;
        }
    }
}
