package com.apple.weather.service;

import com.apple.weather.model.DailyForecast;
import com.apple.weather.model.WeatherResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class WeatherService {
    @Value("${weather.api.current.url}")
    private String currentWeatherApiUrl;
    
    @Value("${weather.api.forecast.url}")
    private String forecastApiUrl;
    
    @Value("${weather.api.key}")
    private String apiKey;

    @Autowired
    private CacheManager cacheManager;

    public WeatherResponse getWeather(String zip) throws Exception {
        
        // Check cache
        WeatherResponse cached = checkCache(zip);
        if (cached != null) {
            return cached;
        }

        HttpClient client = HttpClient.newHttpClient();
        
        // Get current weather
        JSONObject currentJson = fetchCurrentWeatherJson(client, zip);
        JSONObject main = currentJson.getJSONObject("main");
        double temp = main.getDouble("temp");
        double tempMax = main.getDouble("temp_max");
        double tempMin = main.getDouble("temp_min");
        
        // Get forecast
        List<DailyForecast> forecast = fetchForecastData(client, zip);
        
        // Build and cache response
        WeatherResponse response = new WeatherResponse(
            zip, 
            temp, 
            tempMax, 
            tempMin, 
            forecast, 
            false
        );
        saveToCache(zip, response);
        
        return response;
    }

    private WeatherResponse checkCache(String zip) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            Cache.ValueWrapper cached = cache.get(zip);
            if (cached != null) {
                System.out.println("Fetching from CACHE for ZIP: " + zip);
                WeatherResponse response = (WeatherResponse) cached.get();
                response.setFromCache(true);
                return response;
            }
        }
        return null;
    }

    private void saveToCache(String zip, WeatherResponse response) {
        Cache cache = cacheManager.getCache("weatherCache");
        if (cache != null) {
            cache.put(zip, response);
        }
    }

    private JSONObject fetchCurrentWeatherJson(HttpClient client, String zip) throws Exception {
        String url = currentWeatherApiUrl + "?zip=" + zip + ",us&units=imperial&appid=" + apiKey;
        JSONObject json = makeHttpRequest(client, url);
        
        if (!json.has("main")) {
            throw new RuntimeException("Invalid ZIP code or API error.");
        }
        
        return json;
    }

    private List<DailyForecast> fetchForecastData(HttpClient client, String zip) throws Exception {
        String url = forecastApiUrl + "?zip=" + zip + ",us&units=imperial&appid=" + apiKey;
        JSONObject json = makeHttpRequest(client, url);
        return parseForecast(json);
    }

    private JSONObject makeHttpRequest(HttpClient client, String url) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return new JSONObject(response.body());
    }


    private List<DailyForecast> parseForecast(JSONObject forecastJson) {
        Map<String, DailyForecast> dailyMap = new TreeMap<>();
        
        // Get today's date to exclude it from forecast
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        JSONArray list = forecastJson.getJSONArray("list");
        
        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);
            String dateTime = item.getString("dt_txt");
            String date = dateTime.split(" ")[0];
            
            // Skip today's date so that only future days are in the extended forecast
            if (date.equals(today)) {
                continue;
            }
            
            JSONObject main = item.getJSONObject("main");
            double tempMin = main.getDouble("temp_min");
            double tempMax = main.getDouble("temp_max");
            
            JSONArray weatherArray = item.getJSONArray("weather");
            String description = weatherArray.getJSONObject(0).getString("description");
            
            // The forecast API returns 8 data points per day (3 hour interval)
            // Group by date to track daily high/low of the extended forecast
            if (dailyMap.containsKey(date)) {
                DailyForecast existing = dailyMap.get(date);
                existing.setTempHigh(Math.max(existing.getTempHigh(), tempMax));
                existing.setTempLow(Math.min(existing.getTempLow(), tempMin));
            } else {
                dailyMap.put(date, new DailyForecast(date, tempMax, tempMin, description));
            }
        }
        
        // Return only next 5 days
        return new ArrayList<>(dailyMap.values()).subList(0, Math.min(5, dailyMap.size()));
    }

}
