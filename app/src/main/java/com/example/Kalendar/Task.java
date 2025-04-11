package com.example.Kalendar;

public class Task {
    public String time;
    public String title;
    public String description;
    public boolean isDone;

    public Task(String time, String title, String description, boolean isDone) {
        this.time = time;
        this.title = title;
        this.description = description;
        this.isDone = isDone;
    }
}
