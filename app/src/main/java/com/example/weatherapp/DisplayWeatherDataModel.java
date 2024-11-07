package com.example.weatherapp;

import android.util.Log;

public class DisplayWeatherDataModel {
    private String locationName;
    private String date;
    private String day;
    private String tMin;
    private String tMax;
    private String weatherHighlight;
    private String weatherMorning;
    private String weatherAfternoon;
    private String weatherNight;

    public DisplayWeatherDataModel(String locationName, String date, String day, String tMin, String tMax, String weatherHighlight, String weatherMorning, String weatherAfternoon, String weatherNight) {
        this.locationName = locationName;
        this.date = date;
        this.day = day;
        this.tMin = tMin;
        this.tMax = tMax;
        this.weatherHighlight = weatherHighlight;
        this.weatherMorning = weatherMorning;
        this.weatherAfternoon = weatherAfternoon;
        this.weatherNight = weatherNight;
    }

    public DisplayWeatherDataModel(String nullString){
        this.locationName = nullString;
        this.date = nullString;
        this.day = nullString;
        this.tMin = nullString;
        this.tMax = "0";
        this.weatherHighlight = nullString;
        this.weatherMorning = nullString;
        this.weatherAfternoon = nullString;
        this.weatherNight = nullString;
    }

    public DisplayWeatherDataModel(){
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String gettMin() {
        return tMin;
    }

    public void settMin(String tMin) {
        this.tMin = tMin;
    }

    public String gettMax() {
        return tMax;
    }

    public void settMax(String tMax) {
        this.tMax = tMax;
    }

    public String getWeatherHighlight() {
        return weatherHighlight;
    }

    public void setWeatherHighlight(String weatherHighlight) {
        this.weatherHighlight = weatherHighlight;
    }

    public String getWeatherMorning() {
        return weatherMorning;
    }

    public void setWeatherMorning(String weatherMorning) {
        this.weatherMorning = weatherMorning;
    }

    public String getWeatherAfternoon() {
        return weatherAfternoon;
    }

    public void setWeatherAfternoon(String weatherAfternoon) {
        this.weatherAfternoon = weatherAfternoon;
    }

    public String getWeatherNight() {
        return weatherNight;
    }

    public void setWeatherNight(String weatherNight) {
        this.weatherNight = weatherNight;
    }

    public String getShortDay(){
        String day = getDay();
        if (day != null && day.length() >= 3) {
            return day.substring(0, 3);
        }
        return day;
    }

    public void displayData() {
        String tag = "DisplayWeatherDataModel"; // Log tag for filtering
        Log.d(tag, "Location Name: " + locationName);
        Log.d(tag, "Date: " + date);
        Log.d(tag, "Day: " + day);
        Log.d(tag, "Min Temperature: " + tMin);
        Log.d(tag, "Max Temperature: " + tMax);
        Log.d(tag, "Weather Highlight: " + weatherHighlight);
    }
}
