package com.example.weatherapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {

    private final Context context;
    private final ArrayList<WeatherLocationModel> locations;

    public LocationListAdapter(Context context, ArrayList<WeatherLocationModel> locations) {
        this.context = context;
        this.locations = locations;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        WeatherLocationModel location = locations.get(position);
        holder.locationName.setText(location.getLocationName());
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void addLocation(WeatherLocationModel location) {
        locations.add(location);
        notifyItemInserted(locations.size() - 1);
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.location_name);
        }
    }
}

