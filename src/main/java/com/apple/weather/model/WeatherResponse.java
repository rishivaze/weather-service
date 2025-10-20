package com.apple.weather.model;

import java.util.List;

public class WeatherResponse {
    private final String zip;
    private final double temp;
    private final double tempMax;
    private final double tempMin;
    private final List<DailyForecast> forecast;
    private boolean fromCache;

    public WeatherResponse(String zip, double temp, double tempMax, double tempMin, List<DailyForecast> forecast, boolean fromCache) {
        this.zip = zip;
        this.temp = temp;
        this.tempMax = tempMax;
        this.tempMin = tempMin;
        this.forecast = forecast;
        this.fromCache = fromCache;
    }

    public String getZip() {
        return zip;
    }
    public double getTemp() {
        return temp;
    }
    public double getTempMax() {
        return tempMax;
    }
    public double getTempMin() {
        return tempMin;
    }
    public List<DailyForecast> getForecast() {
        return forecast;
    }
    public boolean isFromCache() {
        return fromCache;
    }

    public void setFromCache(boolean fromCache) {
        this.fromCache = fromCache;
    }
}
