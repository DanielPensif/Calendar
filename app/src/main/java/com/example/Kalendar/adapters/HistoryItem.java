package com.example.Kalendar.adapters;

public class HistoryItem {
    public String dateFormatted;
    public String calendarName;
    public long timestamp;
    public String award; // "cup", "medal", "gold_border" или null

    public HistoryItem(String dateFormatted, String calendarName, long timestamp) {
        this.dateFormatted = dateFormatted;
        this.calendarName = calendarName;
        this.timestamp = timestamp;
        this.award = null; // по умолчанию
    }
}
