package com.example.Kalendar.models;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "categories")
public class CategoryEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String name;

    @NonNull
    public String color; // Hex, e.g. "#FF5722"

    public int userId;

    public CategoryEntity(@NonNull String name, @NonNull String color, int userId) {
        this.name = name;
        this.color = color;
        this.userId = userId;
    }
}

