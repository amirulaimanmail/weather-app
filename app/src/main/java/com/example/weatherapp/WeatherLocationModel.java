package com.example.weatherapp;

import androidx.annotation.NonNull;

public class WeatherLocationModel {
    private String locationId;
    private String locationName;

    public WeatherLocationModel(String locationId, String locationName) {
        this.locationId = locationId;
        this.locationName = locationName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    @NonNull
    @Override
    public String toString() {
        return locationName;
    }
}


