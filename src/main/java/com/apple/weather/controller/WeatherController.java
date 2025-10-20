package com.apple.weather.controller;

import com.apple.weather.model.WeatherResponse;
import com.apple.weather.service.WeatherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WeatherController {
    @Autowired
    private WeatherService weatherService;

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping("/forecast")
    public String getForecast(@RequestParam String zip, Model model) {
        try {
            WeatherResponse response = weatherService.getWeather(zip);
            model.addAttribute("weather", response);
            model.addAttribute("fromCache", response.isFromCache());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }
        return "index";
    }
}
