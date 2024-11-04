package com.example.weatherapp;

import androidx.annotation.NonNull;

public class WeatherLocationModel {
    private String databaseId;
    private String locationId;
    private String locationName;

    public WeatherLocationModel(String locationId, String locationName) {
        this.databaseId = null;
        this.locationId = locationId;
        this.locationName = locationName;
    }

    public WeatherLocationModel(String databaseId, String locationId, String locationName) {
        this.databaseId = databaseId;
        this.locationId = locationId;
        this.locationName = locationName;
    }

    public String getDatabaseId() {
        return databaseId;
    }

    public void setDatabaseId(String databaseId) {
        this.databaseId = databaseId;
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


