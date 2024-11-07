package com.example.weatherapp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CustomDateManager {

    public String convertDateToDay(String dateString){
        try {
            // Original date format
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            // Parse the date
            Date date = originalFormat.parse(dateString);
            // Desired date format
            SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
            // Format the date to get the day
            return dayFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String convertToSimpleDate(String dateString){
        try {
            // Original format
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.getDefault());
            Date date = originalFormat.parse(dateString);

            // Desired format
            SimpleDateFormat desiredFormat = new SimpleDateFormat("d MMM yyyy", Locale.getDefault());
            return desiredFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    public String getDateAfterDays(int days) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, days); return sdf.format(calendar.getTime());
    }

    public int getTimeOfDay() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 6 && hour < 12) {
            return 0;
        } else if (hour >= 12 && hour < 18) {
            return 1;
        } else {
            return 2;
        }
    }
}
