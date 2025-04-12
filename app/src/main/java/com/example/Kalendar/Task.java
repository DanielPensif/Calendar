package com.example.Kalendar;

public class Task {
    private String time;
    private String title;
    private String description;
    public boolean isDone;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Task(String time, String title, String description, boolean isDone) {
        this.time = time;
        this.title = title;
        this.description = description;
        this.isDone = isDone;
    }
}
