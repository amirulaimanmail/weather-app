package com.example.weatherapp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainWeatherActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private LocationListAdapter locationListAdapter;
    private ArrayList<WeatherLocationModel> locations;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actv_mainweather);

        drawerLayout = findViewById(R.id.drawer_layout);
        Button openDrawerButton = findViewById(R.id.open_drawer_button);
        recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton addButton = findViewById(R.id.add_button);

        locations = new ArrayList<>();
        locationListAdapter = new LocationListAdapter(this, locations);
        recyclerView.setAdapter(locationListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> showAddLocationDialog());

        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
    }

    //Dialog to add location
    private void showAddLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Location");

        final Spinner spinner = new Spinner(this);
        new FetchLocationsForDialog(spinner).execute("https://api.met.gov.my/v2.1/locations?locationcategoryid=STATE");

        builder.setView(spinner);

        builder.setPositiveButton("Add", (dialog, which) -> {
            WeatherLocationModel selectedLocation = (WeatherLocationModel) spinner.getSelectedItem();
            locationListAdapter.addLocation(selectedLocation);
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //Fetches a list of states for dialog
    private class FetchLocationsForDialog extends AsyncTask<String, Void, ArrayList<WeatherLocationModel>> {
        private final Spinner spinner;

        public FetchLocationsForDialog(Spinner spinner) {
            this.spinner = spinner;
        }

        @Override
        protected ArrayList<WeatherLocationModel> doInBackground(String... urls) {
            ArrayList<WeatherLocationModel> locationsList = new ArrayList<>();
            try {
                Thread.sleep(3000); //simulate loading delay

                URL url = new URL(urls[0]);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "METToken 8d863944cd6fbb68560f6492507d0ceabe192033");

                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }

                in.close();
                conn.disconnect();

                JSONObject jsonObject = new JSONObject(content.toString());
                JSONArray resultsArray = jsonObject.getJSONArray("results");

                for (int i = 0; i < resultsArray.length(); i++) {
                    JSONObject result = resultsArray.getJSONObject(i);
                    String locationId = result.getString("id");
                    String locationName = result.getString("name");
                    WeatherLocationModel location = new WeatherLocationModel(locationId, locationName);
                    locationsList.add(location);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return locationsList;
        }

        @Override
        protected void onPostExecute(ArrayList<WeatherLocationModel> locationsList) {
            ArrayAdapter<WeatherLocationModel> adapter = new ArrayAdapter<>(MainWeatherActivity.this,
                    android.R.layout.simple_spinner_item, locationsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
}