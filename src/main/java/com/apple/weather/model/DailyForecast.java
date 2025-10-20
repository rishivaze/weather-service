package com.apple.weather.model;

public class DailyForecast {
    private final String date;
    private double tempHigh;
    private double tempLow;
    private final String description;

    public DailyForecast(String date, double tempHigh, double tempLow, String description) {
        this.date = date;
        this.tempHigh = tempHigh;
        this.tempLow = tempLow;
        this.description = description;
    }

    public String getDate() {
        return date;
    }
    public double getTempHigh() {
        return tempHigh;
    }
    public double getTempLow() {
        return tempLow;
    }
    public String getDescription() {
        return description;
    }

    public void setTempHigh(double tempHigh) {
        this.tempHigh = tempHigh;
    }
    public void setTempLow(double tempLow) {
        this.tempLow = tempLow;
    }
}

