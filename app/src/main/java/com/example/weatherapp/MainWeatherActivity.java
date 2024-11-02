package com.example.weatherapp;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

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

public class MainWeatherActivity extends AppCompatActivity implements LocationListAdapter.OnButtonClickListener{

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private LocationListAdapter locationListAdapter;
    private ArrayList<WeatherLocationModel> locations;
    private ProgressBar locationProgressBar;
    private Spinner spinner1;
    private Spinner spinner2;

    //database items
    WeatherDatabaseHelper myDB;
    ArrayList<String> weatherId, weatherLocationId, weatherLocationName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actv_mainweather);

        drawerLayout = findViewById(R.id.drawer_layout);
        Button openDrawerButton = findViewById(R.id.open_drawer_button);
        recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton addButton = findViewById(R.id.add_button);

        //manage database
        myDB = new WeatherDatabaseHelper(MainWeatherActivity.this);
        weatherId= new ArrayList<>();
        weatherLocationId = new ArrayList<>();
        weatherLocationName = new ArrayList<>();
        locations = new ArrayList<>();

        resetDataInArrays();

        locationListAdapter = new LocationListAdapter(this, locations, this);
        recyclerView.setAdapter(locationListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> showAddLocationDialog());
        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));
    }

    //Dialog to add location
    private void showAddLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Location");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);
        spinner1 = dialogView.findViewById(R.id.spinner1);
        spinner2 = dialogView.findViewById(R.id.spinner2);
        locationProgressBar = dialogView.findViewById(R.id.progressBar);

        spinner1.setEnabled(false);
        spinner2.setEnabled(false);

        new FetchLocationsForDialog(spinner1, false).execute("https://api.met.gov.my/v2.1/locations?locationcategoryid=STATE");

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                WeatherLocationModel selectedLocation = (WeatherLocationModel) spinner1.getSelectedItem();
                String locationRootId = selectedLocation.getLocationId();
                String url = "https://api.met.gov.my/v2.1/locations?locationrootid=" + locationRootId + "&locationcategoryid=DISTRICT";

                new FetchLocationsForDialog(spinner2, true).execute(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        builder.setView(dialogView);

        builder.setPositiveButton("Add", (dialog, which) -> {

            WeatherLocationModel selectedLocation = (WeatherLocationModel) spinner2.getSelectedItem();
            //locationListAdapter.addLocation(selectedLocation);
            myDB.addLocation(selectedLocation.getLocationId(),selectedLocation.getLocationName());
            resetDataInArrays();
            locationListAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //read data from database
    void resetDataInArrays(){
        weatherId.clear();
        weatherLocationId.clear();
        weatherLocationName.clear();
        locations.clear();

        Cursor cursor = myDB.readAllData();
        if(cursor.getCount() == 0){
            Toast.makeText(this, "No data found.", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(this, "Data found!", Toast.LENGTH_SHORT).show();
            while(cursor.moveToNext()){
                weatherId.add(cursor.getString(0));
                weatherLocationId.add(cursor.getString(1));
                weatherLocationName.add(cursor.getString(2));
                locations.add(new WeatherLocationModel(cursor.getString(1),cursor.getString(2)));
            }
        }
    }

    @Override
    public void onButtonClick(String locationId) {
        Toast.makeText(this, locationId, Toast.LENGTH_SHORT).show();
        drawerLayout.close();
    }

    //Fetches a list of states for dialog
    private class FetchLocationsForDialog extends AsyncTask<String, Void, ArrayList<WeatherLocationModel>> {
        private final Spinner spinner;
        private boolean hideLoaderWhenComplete;

        public FetchLocationsForDialog(Spinner spinner, boolean hideLoaderWhenComplete) {
            this.spinner = spinner;
            this.hideLoaderWhenComplete = hideLoaderWhenComplete;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            locationProgressBar.setVisibility(View.VISIBLE);
            spinner1.setEnabled(false);
            spinner2.setEnabled(false);
        }

        @Override
        protected ArrayList<WeatherLocationModel> doInBackground(String... urls) {
            ArrayList<WeatherLocationModel> locationsList = new ArrayList<>();
            try {
                Thread.sleep(0); //simulate loading delay

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
            super.onPostExecute(locationsList);
            if(hideLoaderWhenComplete){
                locationProgressBar.setVisibility(View.INVISIBLE);
                spinner1.setEnabled(true);
                spinner2.setEnabled(true);
            }

            ArrayAdapter<WeatherLocationModel> adapter = new ArrayAdapter<>(MainWeatherActivity.this,
                    android.R.layout.simple_spinner_item, locationsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }
}