package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.LocationViewHolder> {

    private final Context context;
    private final ArrayList<WeatherLocationModel> locations;
    private final OnButtonClickListener onButtonClickListener;
    private final ArrayList<LocationViewHolder> viewHolders = new ArrayList<>();
    public Boolean hideOnce;

    public LocationListAdapter(Context context, ArrayList<WeatherLocationModel> locations, OnButtonClickListener onButtonClickListener) {
        this.context = context;
        this.locations = locations;
        this.onButtonClickListener = onButtonClickListener;
        hideOnce = false;
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_location_recycler, parent, false);
        return new LocationViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        WeatherLocationModel location = locations.get(position);
        holder.locationName.setText(location.getLocationName());
        holder.locationDeleteButton.setVisibility(View.GONE);

        holder.locationItemLayout.setOnClickListener(v -> onButtonClickListener.onSelectLocationClick(location.getLocationId()));

        //show delete button when long press
        holder.locationItemLayout.setOnLongClickListener(v -> {
            holder.locationDeleteButton.setVisibility(View.VISIBLE);
            hideOnce = true;
            return true;
        });

        holder.locationDeleteButton.setOnClickListener(v -> onButtonClickListener.onDeleteLocationClick(location.getDatabaseId(), position));

        if (!viewHolders.contains(holder)) {
            viewHolders.add(holder);
        }
    }

    @Override
    public void onViewRecycled(@NonNull LocationViewHolder holder) {
        viewHolders.remove(holder);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public void hideAllDeleteButtons() {
        if(hideOnce)
        {
            for (LocationViewHolder holder : viewHolders) {
                holder.locationDeleteButton.setVisibility(View.GONE);
            }
        }
        hideOnce = false;
    }

    public static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView locationName;
        ConstraintLayout locationItemLayout;
        Button locationDeleteButton;

        public LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            locationName = itemView.findViewById(R.id.location_name);
            locationItemLayout = itemView.findViewById(R.id.location_item_layout);
            locationDeleteButton = itemView.findViewById(R.id.location_btn_delete);
        }
    }

    public interface OnButtonClickListener {
        void onSelectLocationClick(String locationId);
        void onDeleteLocationClick(String databaseId, int position);
    }
}

