package com.example.Kalendar;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Task implements Parcelable {
    private String time;
    private String title;
    private String description;
    public boolean isDone;

    protected Task(Parcel in) {
        time = in.readString();
        title = in.readString();
        description = in.readString();
        isDone = in.readByte() != 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(time);
        parcel.writeString(title);
        parcel.writeString(description);
        parcel.writeByte((byte) (isDone ? 1 : 0));
    }
}
