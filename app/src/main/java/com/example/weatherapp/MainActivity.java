package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
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

public class MainActivity extends AppCompatActivity implements LocationListAdapter.OnButtonClickListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private LocationListAdapter locationListAdapter;
    private ProgressBar locationProgressBar;
    private ConstraintLayout weatherProgressBar;

    //DECLARE WEATHER DATA UI ELEMENTS
    private TextView weathertemp_tv;
    private TextView weatherdate_tv;
    private TextView weatherstatus_tv;
    private TextView weatherlocation_tv;


    private ArrayList<WeatherLocationModel> locations;

    private Spinner spinner1;
    private Spinner spinner2;

    private final Handler handler = new Handler(Looper.getMainLooper());

    WeatherDatabaseHelper myDB;

    @SuppressLint("RtlHardcoded")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.actv_mainweather);

        drawerLayout = findViewById(R.id.drawer_layout);
        Button openDrawerButton = findViewById(R.id.open_drawer_button);
        recyclerView = findViewById(R.id.recycler_view);
        FloatingActionButton addButton = findViewById(R.id.add_button);

        //set ui elements
        weathertemp_tv = findViewById(R.id.weathertemp_tv);
        weatherdate_tv = findViewById(R.id.date_tv);
        weatherstatus_tv = findViewById(R.id.weatherstatus_tv);
        weatherlocation_tv = findViewById(R.id.weatherlocation_tv);
        weatherProgressBar = findViewById(R.id.weather_progressbar);

        //manage database
        myDB = new WeatherDatabaseHelper(MainActivity.this);
        locations = myDB.readAllData();

        locationListAdapter = new LocationListAdapter(this, locations, this);
        recyclerView.setAdapter(locationListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> showAddLocationDialog());
        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(Gravity.LEFT));

        updateDisplayData();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (locationListAdapter.hideOnce) {
            handler.postDelayed(() -> locationListAdapter.hideAllDeleteButtons(), 100);
        }
        return super.dispatchTouchEvent(ev);
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
            //ADD TO DATABASE
            myDB.addData(selectedLocation.getLocationId(),selectedLocation.getLocationName());

            //ADD TO CURRENT LIST
            locations.add(new WeatherLocationModel(selectedLocation.getLocationId(),selectedLocation.getLocationName()));
            locationListAdapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    //Interface called from adapter class for tapping on recycler item
    @Override
    public void onSelectLocationClick(String locationId) {
        storeDisplayLocation(locationId);
        updateDisplayData();
        drawerLayout.close();
    }

    //Interface called from adapter class for tapping delete button
    @Override
    public void onDeleteLocationClick(String locationId, int position) {
        deleteLocationDialog(locationId, position);
    }

    //Fetches a list of states for dialog and updates to spinner
    private class FetchLocationsForDialog extends AsyncTask<String, Void, ArrayList<WeatherLocationModel>> {
        private final Spinner spinner;
        private final boolean hideLoaderWhenComplete;

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

            ArrayAdapter<WeatherLocationModel> adapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_spinner_item, locationsList);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        }
    }

    //Fetches weather forecast and update to front screen
    private class FetchWeatherForecast extends AsyncTask<String, Void, DisplayWeatherDataModel>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weatherProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected DisplayWeatherDataModel doInBackground(String... urls) {
            DisplayWeatherDataModel weatherData = new DisplayWeatherDataModel();
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

                    if(i == 0){
                        weatherData.setLocationName(result.getString("locationname"));
                        weatherData.setDate(result.getString("date"));

                        CustomDateManager converter = new CustomDateManager();
                        weatherData.setDay(converter.convertDateToDay(result.getString("date")));
                    }

                    switch(result.getString("datatype")){
                        case "FMAXT":
                            weatherData.settMax(result.getString("value"));
                        case "FMINT":
                            weatherData.settMin(result.getString("value"));
                        case "FSIGW":
                            weatherData.setWeatherHighlight(result.getString("value"));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return weatherData;
        }

        @Override
        protected void onPostExecute(DisplayWeatherDataModel weatherData) {
            super.onPostExecute(weatherData);
            // This is where you can handle the returned weatherData, for example:
            if (weatherData != null) {
                weatherData.displayData(); // Using the displayData method for logging
                // Update UI with weatherData here
                weatherlocation_tv.setText(weatherData.getLocationName());
                weathertemp_tv.setText(weatherData.gettMax());
                weatherstatus_tv.setText(weatherData.getWeatherHighlight());
                CustomDateManager convertDate = new CustomDateManager();
                String date_tv_string = convertDate.convertToSimpleDate(weatherData.getDate())  + " | " + weatherData.getDay();
                weatherdate_tv.setText(date_tv_string);
                weatherProgressBar.setVisibility(View.INVISIBLE);
            }
        }
    }

    //dialog for deleting location
    private void deleteLocationDialog(String databaseId, int position){
        new AlertDialog.Builder(this)
                .setTitle("Delete Location")
                .setMessage("Are you sure you want to delete this location?")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    //DELETE FROM DATABASE
                    myDB.deleteData(databaseId);

                    //DELETE FROM CURRENT LIST
                    locations.remove(position);
                    //locationListAdapter.notifyDataSetChanged();
                    locationListAdapter.notifyItemRemoved(position);
                    locationListAdapter.notifyItemRangeChanged(position, locationListAdapter.getItemCount());
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    //updates current location and save to prefs
    private void storeDisplayLocation(String displayLocationId){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("locationId",displayLocationId);
        editor.apply();
        //Toast.makeText(this, "DATA UPDATED", Toast.LENGTH_SHORT).show();
    }

    //retrieve current location from prefs
    private String retrieveDisplayLocation(){
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String retrievedId = sharedPreferences.getString("locationId", null);
        if(retrievedId == null){
            return "LOCATION:1";
        }
        //Toast.makeText(this, "DATA RETRIEVED: "+ retrievedId, Toast.LENGTH_SHORT).show();
        return retrievedId;
    }

    //updates front screen
    private void updateDisplayData(){
        CustomDateManager getDate = new CustomDateManager();
        String currentDate = getDate.getCurrentDate();
        new FetchWeatherForecast().execute("https://api.met.gov.my/v2.1/data?datasetid=FORECAST&datacategoryid=GENERAL&locationid="+ retrieveDisplayLocation() +"&start_date=" + currentDate + "&end_date=" + currentDate);
    }
}