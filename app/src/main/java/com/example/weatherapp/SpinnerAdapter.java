package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class SpinnerAdapter extends ArrayAdapter<WeatherLocationModel> {

    private final Context context;
    private final ArrayList<WeatherLocationModel> locations;
    private final ArrayList<String> existingLocations;

    public SpinnerAdapter(@NonNull Context context, ArrayList<WeatherLocationModel> locations, ArrayList<String> existingLocations) {
        super(context, 0, locations);
        this.context = context;
        this.locations = locations;
        this.existingLocations = existingLocations;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.custom_spinner_item);
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, R.layout.custom_spinner_item);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent, int resource) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(context).inflate(resource, parent, false);
        }

        WeatherLocationModel location = locations.get(position);
        TextView textView = view.findViewById(R.id.text1);

        textView.setText(location.getLocationName());

        if (existingLocations.contains(location.getLocationId()) | position == 0) {
            textView.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
            textView.setEnabled(false);
        } else {
            textView.setTextColor(context.getResources().getColor(android.R.color.black));
            textView.setEnabled(true);
        }
        return view;
    }

    @Override
    public boolean isEnabled(int position) {
        if(position == 0){
            return false;
        }

        WeatherLocationModel location = locations.get(position);
        return (!existingLocations.contains(location.getLocationId()));
    }

    // Method to check if the item is disabled
     public boolean isItemDisabled(int position) {
         if (position == 0) {
             return true;
         } else {
             WeatherLocationModel location = locations.get(position);
             return existingLocations.contains(location.getLocationId());
         }
    }
}
