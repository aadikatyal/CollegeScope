package com.example.collegescope;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

public class PopWindow extends AppCompatActivity
{
    private Spinner spType;
    private Button btFind;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private double currentLat = 0, currentLong = 0;
    private String placeTypeList[] = {"university"};
    private String placeNameList[] = {"College"};

    private String collegeName;
    private double latitude, longitude;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_map);

        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e)
        {

        }

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width * 0.9), (int)(height * 0.8));

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");

        placeNameList = new String[]{name};

        spType = findViewById(R.id.sp_type);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        spType.setAdapter(new ArrayAdapter<>(PopWindow.this, android.R.layout.simple_spinner_dropdown_item, placeNameList));

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(PopWindow.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            String latString = intent.getStringExtra("lat");
            String lonString = intent.getStringExtra("lon");
            currentLat = Double.parseDouble(latString);
            currentLong = Double.parseDouble(lonString);

            getCurrentLocation(currentLat, currentLong);
        }
        else
        {
            ActivityCompat.requestPermissions(PopWindow.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        int i = spType.getSelectedItemPosition();
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + "?location=" + currentLat + "," + currentLong + "&radius=160934" + "&type=" + placeTypeList[i] + "&sensor=true" + "&key=AIzaSyBr9ziVncg35qSITcJnEJqaexpz5qvSRl8";

        new PlaceTask().execute(url);
    }

    private void getCurrentLocation(final double lat, final double lon)
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            return;
        }
        Task<Location> task = fusedLocationProviderClient.getLastLocation();
        task.addOnSuccessListener(new OnSuccessListener<Location>()
        {
            @Override
            public void onSuccess(Location location)
            {
                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                String latString = intent.getStringExtra("lat");
                String lonString = intent.getStringExtra("lon");
                double lat = Double.parseDouble(latString);
                double lon = Double.parseDouble(lonString);

                currentLat = lat;
                currentLong = lon;

                supportMapFragment.getMapAsync(new OnMapReadyCallback()
                {
                    @Override
                    public void onMapReady(GoogleMap googleMap)
                    {
                        map = googleMap;
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentLat, currentLong), 10));
                    }
                });
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if(requestCode == 44)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                Intent intent = getIntent();
                String latString = intent.getStringExtra("lat");
                String lonString = intent.getStringExtra("lon");
                currentLat = Double.parseDouble(latString);
                currentLong = Double.parseDouble(lonString);

                getCurrentLocation(currentLat, currentLong);
            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String>
    {
        @Override
        protected String doInBackground(String... strings)
        {
            String data = null;
            try
            {
                data = downloadUrl(strings[0]);
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }

            return data;
        }

        @Override
        protected void onPostExecute(String s)
        {
            new ParserTask().execute(s);
        }
    }

    private String downloadUrl(String string) throws IOException
    {
        URL url = new URL(string);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.connect();
        InputStream stream = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line = "";

        while((line = reader.readLine()) != null)
        {
            builder.append(line);
        }

        String data = builder.toString();
        reader.close();
        return data;
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>>
    {

        @Override
        protected List<HashMap<String, String>> doInBackground(String... strings)
        {
            JsonParser jsonParser = new JsonParser();
            List<HashMap<String, String>> mapList = null;
            JSONObject object = null;
            try
            {
                object = new JSONObject(strings[0]);
                mapList = jsonParser.parseResult(object);
            }
            catch(JSONException e)
            {
                e.printStackTrace();
            }
            return mapList;
        }

        @Override
        protected void onPostExecute(List<HashMap<String, String>> hashMaps)
        {
            map.clear();
            for(int i = 0; i < hashMaps.size(); i++)
            {
                HashMap<String, String> hashMapList = hashMaps.get(i);

                Intent intent = getIntent();
                String name = intent.getStringExtra("name");
                String latString = intent.getStringExtra("lat");
                String lonString = intent.getStringExtra("lon");
                double lat = Double.parseDouble(latString);
                double lon = Double.parseDouble(lonString);

                LatLng latLng = new LatLng(lat, lon);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
            {
                onBackPressed();
                break;
            }
        }
        return super.onOptionsItemSelected(item);
    }
}