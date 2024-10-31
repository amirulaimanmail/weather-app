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

    private Context context;
    private ArrayList<String> locations;

    public LocationListAdapter(Context context, ArrayList<String> locations) {
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
        holder.locationName.setText(locations.get(position));
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void addLocation(String location) {
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

