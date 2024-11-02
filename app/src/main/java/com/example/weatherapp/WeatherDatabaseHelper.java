package com.example.weatherapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

class WeatherDatabaseHelper extends SQLiteOpenHelper {

    private final Context context;
    private static final String DATABASE_NAME = "WeatherLibrary.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_NAME = "weather_list";
    private static final String COLUMN_ID =  "weather_id";
    private static final String COLUMN_LOCATIONID =  "weather_location_id";
    private static final String COLUMN_LOCATIONNAME =  "weather_location_name";

    public WeatherDatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query =
                "CREATE TABLE " + TABLE_NAME + " ("
                        + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                        + COLUMN_LOCATIONID+ " INTEGER, "
                        + COLUMN_LOCATIONNAME + " TEXT); ";

        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addLocation(String locationId, String locationName){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_LOCATIONID, locationId);
        cv.put(COLUMN_LOCATIONNAME, locationName);
        long result = db.insert(TABLE_NAME, null, cv);
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(context, "Added location successfully", Toast.LENGTH_SHORT).show();
        }
    }

    public Cursor readAllData(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        if(db != null) {
            try {
                // Check if the table exists
                String query = "SELECT * FROM " + TABLE_NAME;
                cursor = db.rawQuery(query, null);
            } catch (Exception e) {
                // Create table if it does not exist
                onCreate(db);
                String query = "SELECT * FROM " + TABLE_NAME;
                cursor = db.rawQuery(query, null);
            }
        }
        return cursor;
    }

    void deleteData(String row_id){
        SQLiteDatabase db = this.getWritableDatabase();

        long result = db.delete(TABLE_NAME, COLUMN_ID+"=?", new String[]{row_id} );
        if(result == -1){
            Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
        } else{
            Toast.makeText(context, "Successfully deleted data", Toast.LENGTH_SHORT).show();
        }
    }

    void deleteAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
