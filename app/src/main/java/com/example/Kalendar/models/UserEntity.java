package com.example.Kalendar.models;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    @ColumnInfo(name = "username")
    private String username;

    @NonNull
    @ColumnInfo(name = "password_hash")
    private String passwordHash;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "description")
    private String description;

    @ColumnInfo(name = "photo_uri")
    private String photoUri;
    @ColumnInfo(name = "photo_blob", typeAffinity = ColumnInfo.BLOB)
    private byte[]  photoBlob;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(@NonNull String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public byte[] getPhotoBlob() {
        return photoBlob;
    }

    public void setPhotoBlob(byte[] photoBlob) {
        this.photoBlob = photoBlob;
    }
}
