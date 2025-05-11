package com.example.Kalendar.adapters;

public class HistoryItem {
    public String dateFormatted;
    public String calendarName;
    public long timestamp;
    public int calendarId;
    public String award; // "cup", "medal", "gold_border" или null

    public HistoryItem(String dateFormatted, String calendarName, long timestamp, int calendarId, String awardType) {
        this.dateFormatted = dateFormatted;
        this.calendarName = calendarName;
        this.timestamp = timestamp;
        this.calendarId = calendarId;
        this.award = null; // по умолчанию
    }
}
