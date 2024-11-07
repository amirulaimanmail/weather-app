package com.example.weatherapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
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
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements LocationListAdapter.OnButtonClickListener {

    private DrawerLayout drawerLayout;
    private RecyclerView recyclerView;
    private LocationListAdapter locationListAdapter;
    private ProgressBar locationProgressBar1;
    private ProgressBar locationProgressBar2;
    private ConstraintLayout weatherProgressBar;

    //DECLARE WEATHER DATA UI ELEMENTS
    private ConstraintLayout backgroundLayout;

    private TextView weathertemp_tv;
    private TextView weatherdate_tv;
    private TextView weatherstatus_tv;
    private TextView weatherlocation_tv;
    private ImageView weathericon_iv;

    private ImageView weatherIconMorning_iv, weatherIconAfternoon_iv, weatherIconNight_iv;

    private TextView weatherUpDay1_tv, weatherUpDay2_tv, weatherUpDay3_tv;

    private TextView weatherTempDay1_tv, weatherTempDay2_tv, weatherTempDay3_tv;

    private ImageView weatherIconDay1_iv, weatherIconDay2_iv, weatherIconDay3_iv;


    //
    private ArrayList<WeatherLocationModel> locations;

    private Spinner spinner1;
    private Spinner spinner2;

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
         backgroundLayout = findViewById(R.id.background_main);

        weathertemp_tv = findViewById(R.id.weathertemp_tv);
        weatherdate_tv = findViewById(R.id.date_tv);
        weatherstatus_tv = findViewById(R.id.weatherstatus_tv);
        weatherlocation_tv = findViewById(R.id.weatherlocation_tv);
        weatherProgressBar = findViewById(R.id.weather_progressbar);
        weathericon_iv = findViewById(R.id.weathericon_iv);

        weatherIconMorning_iv = findViewById(R.id.mIcon_iv);
        weatherIconAfternoon_iv = findViewById(R.id.aIcon_iv);
        weatherIconNight_iv = findViewById(R.id.nIcon_iv);

        weatherUpDay1_tv = findViewById(R.id.upcomingDay1_tv);
        weatherUpDay2_tv = findViewById(R.id.upcomingDay2_tv);
        weatherUpDay3_tv = findViewById(R.id.upcomingDay3_tv);

        weatherTempDay1_tv = findViewById(R.id.upcomingTemp1_tv);
        weatherTempDay2_tv = findViewById(R.id.upcomingTemp2_tv);
        weatherTempDay3_tv = findViewById(R.id.upcomingTemp3_tv);

        weatherIconDay1_iv = findViewById(R.id.upcomingIcon1_iv);
        weatherIconDay2_iv = findViewById(R.id.upcomingIcon2_iv);
        weatherIconDay3_iv = findViewById(R.id.upcomingIcon3_iv);

        //manage database
        myDB = new WeatherDatabaseHelper(MainActivity.this);
        locations = myDB.readAllData();

        locationListAdapter = new LocationListAdapter(this, locations, this);
        recyclerView.setAdapter(locationListAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        addButton.setOnClickListener(v -> showAddLocationDialog());
        openDrawerButton.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        updateDisplayData();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            drawerLayout.postDelayed(() -> locationListAdapter.hideAllDeleteButtons(), 10);
        }
        return super.dispatchTouchEvent(ev);
    }

    private FetchLocationsForDialog currentSpinner2Task;

    //Dialog to add location
    private void showAddLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Location");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_location, null);
        spinner1 = dialogView.findViewById(R.id.spinner1);
        spinner2 = dialogView.findViewById(R.id.spinner2);
        locationProgressBar1 = dialogView.findViewById(R.id.progressBar1);
        locationProgressBar2 = dialogView.findViewById(R.id.progressBar2);

        spinner1.setEnabled(false);
        spinner2.setEnabled(false);

        builder.setView(dialogView);
        builder.setPositiveButton("Add", null);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_bg_1);
        dialog.show();

        //SET DIALOG WIDTH SIZE
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 80% of screen width
        dialog.getWindow().setAttributes(layoutParams);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        new FetchLocationsForDialog(spinner1, 1, dialog).execute("https://api.met.gov.my/v2.1/locations?locationcategoryid=STATE");

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                WeatherLocationModel selectedLocation = (WeatherLocationModel) spinner1.getSelectedItem();
                String locationRootId = selectedLocation.getLocationId();
                String url = "https://api.met.gov.my/v2.1/locations?locationrootid=" + locationRootId + "&locationcategoryid=DISTRICT";

                // Cancel the previous task if it's still running
                 if (currentSpinner2Task != null && currentSpinner2Task.getStatus() != AsyncTask.Status.FINISHED) {
                     currentSpinner2Task.cancel(true);
                 }

                // Clear spinner2 before loading new data
                ArrayAdapter<WeatherLocationModel> emptyAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, new ArrayList<>());
                spinner2.setAdapter(emptyAdapter);

                currentSpinner2Task = new FetchLocationsForDialog(spinner2, 2, dialog);
                currentSpinner2Task.execute(url);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (((SpinnerAdapter) spinner2.getAdapter()).isItemDisabled(position)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            WeatherLocationModel selectedLocation = (WeatherLocationModel) spinner2.getSelectedItem();

            //ADD TO DATABASE
            myDB.addData(selectedLocation.getLocationId(),selectedLocation.getLocationName());

            //ADD TO CURRENT LIST
            locations.add(new WeatherLocationModel(selectedLocation.getLocationId(),selectedLocation.getLocationName()));
            locationListAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

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
        private final int loader;
        private final AlertDialog dialog;

        public FetchLocationsForDialog(Spinner spinner, int loader, AlertDialog dialog) {
            this.spinner = spinner;
            this.loader = loader;
            this.dialog = dialog;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

            switch(loader){
                case 1:
                    locationProgressBar1.setVisibility(View.VISIBLE);
                    spinner1.setEnabled(false);
                    break;
                case 2:
                    locationProgressBar2.setVisibility(View.VISIBLE);
                    spinner2.setEnabled(false);
                    break;
            }
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

            switch(loader){
                case 1:
                    Log.d("TAG", "WEE");
                    locationProgressBar1.setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    Log.d("TAG", "WOO");
                    if(locationsList.isEmpty()){
                        spinner2.setEnabled(false);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                    else {
                        spinner2.setEnabled(true);
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    }
                    Log.d("TAG", "1");
                    locationProgressBar2.setVisibility(View.INVISIBLE);
                    Log.d("TAG", "2");
                    spinner1.setEnabled(true);
                    break;
            }

            //retrieve list of existing locationIds
            ArrayList<String> existingIds = new ArrayList<>();
            for (WeatherLocationModel location : locations) {
                existingIds.add(location.getLocationId());
            }

            //initialize adapter
            SpinnerAdapter adapter = new SpinnerAdapter(MainActivity.this, locationsList, existingIds);
            spinner.setAdapter(adapter);
        }
    }

    //Fetches weather forecast and update to front screen
    private class FetchWeatherForecast extends AsyncTask<String, Void, ArrayList<DisplayWeatherDataModel>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            weatherProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ArrayList<DisplayWeatherDataModel> doInBackground(String... urls) {
            ArrayList<DisplayWeatherDataModel> weatherData = new ArrayList<>();

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

                DisplayWeatherDataModel weatherDayData1 = new DisplayWeatherDataModel();
                DisplayWeatherDataModel weatherDayData2 = new DisplayWeatherDataModel();
                DisplayWeatherDataModel weatherDayData3 = new DisplayWeatherDataModel();
                DisplayWeatherDataModel weatherDayData4 = new DisplayWeatherDataModel();

                for(int i = 0; i < 6; i ++){

                    for (int j = 0; j < 4; j++) {
                        JSONObject result = resultsArray.getJSONObject(i * 4 + j); // Adjust index for nested loop

                        switch(result.getString("datatype")){
                            case "FGM":
                                CustomDateManager converter = new CustomDateManager();
                                switch(j){
                                    case 0:
                                        weatherDayData1.setLocationName(result.getString("locationname"));
                                        weatherDayData2.setLocationName(result.getString("locationname"));
                                        weatherDayData3.setLocationName(result.getString("locationname"));
                                        weatherDayData4.setLocationName(result.getString("locationname"));

                                        weatherDayData1.setWeatherMorning(result.getString("value"));

                                        weatherDayData1.setDate(result.getString("date"));
                                        String day1 = converter.convertDateToDay(result.getString("date"));
                                        weatherDayData1.setDay(day1);
                                        break;
                                    case 1:
                                        weatherDayData2.setWeatherMorning(result.getString("value"));

                                        weatherDayData2.setDate(result.getString("date"));
                                        String day2 = converter.convertDateToDay(result.getString("date"));
                                        weatherDayData2.setDay(day2);
                                        break;
                                    case 2:
                                        weatherDayData3.setWeatherMorning(result.getString("value"));

                                        weatherDayData3.setDate(result.getString("date"));
                                        String day3 = converter.convertDateToDay(result.getString("date"));
                                        weatherDayData3.setDay(day3);
                                        break;
                                    case 3:
                                        weatherDayData4.setWeatherMorning(result.getString("value"));

                                        weatherDayData4.setDate(result.getString("date"));
                                        String day4 = converter.convertDateToDay(result.getString("date"));
                                        weatherDayData4.setDay(day4);
                                        break;
                                }
                                break;

                            case "FGA":
                                switch(j) {
                                    case 0:
                                        weatherDayData1.setWeatherAfternoon(result.getString("value"));
                                        break;
                                    case 1:
                                        weatherDayData2.setWeatherAfternoon(result.getString("value"));
                                        break;
                                    case 2:
                                        weatherDayData3.setWeatherAfternoon(result.getString("value"));
                                        break;
                                    case 3:
                                        weatherDayData4.setWeatherAfternoon(result.getString("value"));
                                        break;
                                }

                            case "FGN":
                                switch(j) {
                                    case 0:
                                        weatherDayData1.setWeatherNight(result.getString("value"));
                                        break;
                                    case 1:
                                        weatherDayData2.setWeatherNight(result.getString("value"));
                                        break;
                                    case 2:
                                        weatherDayData3.setWeatherNight(result.getString("value"));
                                        break;
                                    case 3:
                                        weatherDayData4.setWeatherNight(result.getString("value"));
                                        break;
                                }

                            case "FMAXT":
                                switch(j) {
                                    case 0:
                                        weatherDayData1.settMax(result.getString("value"));
                                        break;
                                    case 1:
                                        weatherDayData2.settMax(result.getString("value"));
                                        break;
                                    case 2:
                                        weatherDayData3.settMax(result.getString("value"));
                                        break;
                                    case 3:
                                        weatherDayData4.settMax(result.getString("value"));
                                        break;
                                }
                                break;

                            case "FMINT":
                                switch(j) {
                                    case 0:
                                        weatherDayData1.settMin(result.getString("value"));
                                        break;
                                    case 1:
                                        weatherDayData2.settMin(result.getString("value"));
                                        break;
                                    case 2:
                                        weatherDayData3.settMin(result.getString("value"));
                                        break;
                                    case 3:
                                        weatherDayData4.settMin(result.getString("value"));
                                        break;
                                }
                                break;

                            case "FSIGW":
                                switch(j) {
                                    case 0:
                                        weatherDayData1.setWeatherHighlight(result.getString("value"));
                                        break;
                                    case 1:
                                        weatherDayData2.setWeatherHighlight(result.getString("value"));
                                        break;
                                    case 2:
                                        weatherDayData3.setWeatherHighlight(result.getString("value"));
                                        break;
                                    case 3:
                                        weatherDayData4.setWeatherHighlight(result.getString("value"));
                                        break;
                                }
                                break;
                        }
                    }
                }

                weatherData.add(weatherDayData1);
                weatherData.add(weatherDayData2);
                weatherData.add(weatherDayData3);
                weatherData.add(weatherDayData4);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return weatherData;
        }

        @Override
        protected void onPostExecute(ArrayList<DisplayWeatherDataModel> weatherData) {
            super.onPostExecute(weatherData);
            // This is where you can handle the returned weatherData, for example:
            if (weatherData != null) {

                // Update UI with weatherData here
                weatherlocation_tv.setText(weatherData.get(0).getLocationName());
                weathertemp_tv.setText(weatherData.get(0).gettMax());

                CustomDateManager convertDate = new CustomDateManager();
                String date_tv_string = convertDate.convertToSimpleDate(weatherData.get(0).getDate())  + " | " + weatherData.get(0).getDay();
                weatherdate_tv.setText(date_tv_string);

                //update weather icon
                CustomDateManager getDayStat = new CustomDateManager();
                switch(getDayStat.getTimeOfDay()){
                    case 0:
                        String weatherStatus = weatherData.get(0).getWeatherMorning();
                        if(Objects.equals(weatherStatus, "No rain")){
                            weatherStatus = "Sunny";
                        }

                        weatherstatus_tv.setText(weatherStatus);
                        if (weatherStatus.toLowerCase().contains("storm")) {
                            weathericon_iv.setImageResource(R.drawable.rainy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_cloudy_background);
                        }
                        else {
                            weathericon_iv.setImageResource(R.drawable.cloudy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_clearsky_background);
                        }
                        break;

                    case 1:
                        String weatherStatus1 = weatherData.get(0).getWeatherAfternoon();
                        if(Objects.equals(weatherStatus1, "No rain")){
                            weatherStatus1 = "Sunny";
                        }

                        weatherstatus_tv.setText(weatherStatus1);
                        if (weatherStatus1.toLowerCase().contains("storm")) {
                            weathericon_iv.setImageResource(R.drawable.rainy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_cloudy_background);
                        }
                        else {
                            weathericon_iv.setImageResource(R.drawable.cloudy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_clearsky_background);

                        }
                        break;

                    case 2:
                        String weatherStatus2 = weatherData.get(0).getWeatherNight();
                        if(Objects.equals(weatherStatus2, "No rain")){
                            weatherStatus2 = "Clear";
                        }

                        weatherstatus_tv.setText(weatherStatus2);
                        if (weatherStatus2.toLowerCase().contains("storm")) {
                            weathericon_iv.setImageResource(R.drawable.rainy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_cloudy_background);
                        }
                        else {
                            weathericon_iv.setImageResource(R.drawable.cloudy);
                            backgroundLayout.setBackgroundResource(R.drawable.day_clearsky_background);

                        }
                        break;
                }

                //// update weather time icon
                String weatherStatusMorning = weatherData.get(0).getWeatherMorning();
                if (weatherStatusMorning.toLowerCase().contains("storm")) {
                    weatherIconMorning_iv.setImageResource(R.drawable.rainy);
                }
                else {
                    weatherIconMorning_iv.setImageResource(R.drawable.cloudy);
                }

                String weatherStatusAfternoon = weatherData.get(0).getWeatherAfternoon();
                if (weatherStatusAfternoon.toLowerCase().contains("storm")) {
                    weatherIconAfternoon_iv.setImageResource(R.drawable.rainy);
                }
                else {
                    weatherIconAfternoon_iv.setImageResource(R.drawable.cloudy);
                }

                String weatherStatusNight = weatherData.get(0).getWeatherNight();
                if (weatherStatusNight.toLowerCase().contains("storm")) {
                    weatherIconNight_iv.setImageResource(R.drawable.rainy);
                }
                else {
                    weatherIconNight_iv.setImageResource(R.drawable.cloudynight);
                }

                // update upcoming data
                weatherUpDay1_tv.setText(weatherData.get(1).getDay());
                weatherUpDay2_tv.setText(weatherData.get(2).getDay());
                weatherUpDay3_tv.setText(weatherData.get(3).getDay());

                String tempUnit = "°C";
                weatherTempDay1_tv.setText(String.format("%s %s", weatherData.get(1).gettMax(), tempUnit));
                weatherTempDay2_tv.setText(String.format("%s %s", weatherData.get(2).gettMax(), tempUnit));
                weatherTempDay3_tv.setText(String.format("%s %s", weatherData.get(3).gettMax(), tempUnit));

                ImageView[] weatherIcons = {weatherIconDay1_iv, weatherIconDay2_iv, weatherIconDay3_iv};
                for (int i = 0; i < 3; i++) { String weatherHighlight = weatherData.get(i + 1).getWeatherHighlight().toLowerCase();
                    if (weatherHighlight.contains("storm")) {
                        weatherIcons[i].setImageResource(R.drawable.rainy);
                    } else {
                        weatherIcons[i].setImageResource(R.drawable.cloudy);
                    }
                }

                weatherProgressBar.setVisibility(View.GONE);
            }
        }
    }

    //dialog for deleting location
    private void deleteLocationDialog(String databaseId, int position){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Location");
        builder.setMessage("Are you sure you want to delete this location?");


        builder.setPositiveButton("Yes", (dialog, which) -> {

            //DELETE FROM DATABASE
            myDB.deleteData(databaseId);

            //DELETE FROM CURRENT LIST
            locations.remove(position);

            //locationListAdapter.notifyDataSetChanged();
            locationListAdapter.notifyItemRemoved(position);
            locationListAdapter.notifyItemRangeChanged(position, locationListAdapter.getItemCount());
        });

        builder.setNegativeButton("No", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.getWindow().setBackgroundDrawableResource(R.drawable.alert_dialog_bg_1);
        dialog.show();

        //SET DIALOG WIDTH SIZE
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(dialog.getWindow().getAttributes());
        layoutParams.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85); // 80% of screen width
        dialog.getWindow().setAttributes(layoutParams);
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
        String forecastDate = getDate.getDateAfterDays(3);
        new FetchWeatherForecast().execute("https://api.met.gov.my/v2.1/data?datasetid=FORECAST&datacategoryid=GENERAL&locationid="+ retrieveDisplayLocation() +"&start_date=" + currentDate + "&end_date=" + forecastDate);
    }
}