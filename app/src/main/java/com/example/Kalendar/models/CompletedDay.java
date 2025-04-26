package com.example.Kalendar.models;

public class CompletedDay {
    private final String date;
    private int awardType = -1;

    public CompletedDay(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public int getAwardType() {
        return awardType;
    }

    public void setAwardType(int awardType) {
        this.awardType = awardType;
    }
}
