package com.example.Kalendar.adapters;

public class HistoryItem {
    public String dateFormatted;
    public String calendarName;
    public long timestamp;

    public HistoryItem(String dateFormatted, String calendarName, long timestamp) {
        this.dateFormatted = dateFormatted;
        this.calendarName = calendarName;
        this.timestamp = timestamp;
    }
}
