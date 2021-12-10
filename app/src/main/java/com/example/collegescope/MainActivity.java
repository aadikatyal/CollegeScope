package com.example.collegescope;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.app.ProgressDialog;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity
{
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1 ;
    private ListView listView;
    private Button btnFind;
    private TextView tvGPA, tv;
    private ArrayList<String> list = new ArrayList<>();
    private ArrayList<CollegeData> data = new ArrayList<CollegeData>();

    private ArrayList<Coordinate> coordinates = new ArrayList<Coordinate>();

    private String collegeJSONData;
    private String databaseSAT, databaseGPA;
    private double maxRange;

    private int counter = 0;

    private Switch switchMap;

    private TextView tvPlaceholder;

    private Spinner spType;
    private SupportMapFragment supportMapFragment;
    private GoogleMap map;
    private FusedLocationProviderClient fusedLocationProviderClient2;
    private double currentLat = 0, currentLong = 0;
    private String placeTypeList[] = {"university"};
    private String placeNameList[] = {"College"};

    private View separator2, separator;

    private JSONArray collegesJSONArray;

    private FusedLocationProviderClient fusedLocationProviderClient;

    private ImageView imageView;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference firebaseRootRef;
    private DatabaseReference itemRef;


    private ProgressDialog progressDialog;

    private double currentLatitude, currentLongitude;

    private SeekBar sbRange;
    private TextView tvRange;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvGPA = findViewById(R.id.tvGPA);
        listView = findViewById(R.id.listView);
        tv = findViewById(R.id.tvMain);
        sbRange = findViewById(R.id.sbRange);
        tvRange = findViewById(R.id.tvRange);
        btnFind = findViewById(R.id.btnFind);
        separator2 = findViewById(R.id.separator2);
        switchMap = findViewById(R.id.switchMap);
        separator2.setBackgroundColor(Color.parseColor("#d3d3d3"));

        separator = findViewById(R.id.separator);

        separator.setBackgroundColor(Color.parseColor("#d3d3d3"));

        separator.setVisibility(View.GONE);

        switchMap.setVisibility(View.GONE);

        tvPlaceholder = findViewById(R.id.tvPlaceholder);

        imageView = findViewById(R.id.imageView3);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        progressDialog = new ProgressDialog(this);

        spType = findViewById(R.id.sp_type);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.google_map);

        spType.setAdapter(new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, placeNameList));

        supportMapFragment.getView().setVisibility(View.GONE);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        firebaseRootRef = firebaseDatabase.getReference();
        itemRef = firebaseRootRef.child(firebaseAuth.getUid());
        //tvDirect = findViewById(R.id.tvDirect);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        btnFind.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                separator.setVisibility(View.VISIBLE);
                tvPlaceholder.setText("Click on a Listing Below!");

                if(list.size() == 0)
                {
                    method();
                }
            }
        });

        switchMap.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    supportMapFragment.getView().setVisibility(View.VISIBLE);
                    switchMap.setText("Map");
                }
                else
                {
                    supportMapFragment.getView().setVisibility(View.GONE);
                    switchMap.setText("Info");
                }
            }
        });

        if(sbRange != null)
        {
            sbRange.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
                {
                    sbRange.setMax(100);

                    seekBar.setProgress(progress);

                    double rangeNum = (double)progress;
                    String range = String.valueOf(rangeNum);
                    maxRange = rangeNum;

                    tvRange.setText("Range: " + range + " miles");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar)
                {
                    //tvDirect.setText("");
                    //separator2.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    //separator.setBackgroundColor(Color.parseColor("#d3d3d3"));
                    list.clear();
                    data.clear();
                    separator.setVisibility(View.GONE);
                    switchMap.setVisibility(View.GONE);
                    tvPlaceholder.setText("");
                    imageView.setVisibility(View.GONE);
                    supportMapFragment.getView().setVisibility(View.GONE);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar)
                {

                }
            });
        }

        getData();
    }

    private void createListView(final String name, final double GPA, final int SAT, final double lat, final double lon)
    {
        tv.setText("Recommended Colleges");

        list.add(name /* + "\nAverage GPA: " + GPA + "\nAverage SAT: " + SAT */);

        CollegeData collegeData = new CollegeData(name, lat, lon, GPA, SAT);
        data.add(collegeData);

        listView.setDivider(new ColorDrawable(Color.parseColor("#d3d3d3")));
        listView.setDividerHeight(3);

        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.list_item_layout, list);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                CollegeData item = data.get(position);

                String latString = item.getLat() + "";
                String lonString = item.getLon() + "";
                String name = item.getName();
                double GPA = item.getGPA();
                int SAT = item.getSAT();

                //supportMapFragment.getView().setVisibility(View.VISIBLE);

                tvPlaceholder.setText(name + "\nAverage GPA: " + GPA + "\nAverage SAT: " + SAT);

                imageView.setVisibility(View.VISIBLE);

                if(name.contains("Princeton"))
                {
                    imageView.setImageResource(R.drawable.pu);
                }
                if(name.contains("Rutgers"))
                {
                    imageView.setImageResource(R.drawable.ru);
                }
                if(name.contains("New Jersey Institute"))
                {
                    imageView.setImageResource(R.drawable.njit);
                }
                if(name.contains("Stevens"))
                {
                    imageView.setImageResource(R.drawable.stevens);
                }
                if(name.contains("Drexel"))
                {
                    imageView.setImageResource(R.drawable.drexel);
                }
                if(name.contains("Rider"))
                {
                    imageView.setImageResource(R.drawable.rider);
                }
                if(name.contains("Rowan"))
                {
                    imageView.setImageResource(R.drawable.rowan);
                }
                if(name.contains("Ramapo"))
                {
                    imageView.setImageResource(R.drawable.ramapo);
                }

                switchMap.setVisibility(View.VISIBLE);

                method2(latString + " ", lonString + " ", name);
            }
        });
    }

    private void readData(final MyCallback myCallback)
    {
        ValueEventListener valueEventListener = new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot snapshot)
            {
                String SAT = snapshot.child("userSAT").getValue(String.class);
                String GPA = snapshot.child("userGPA").getValue(String.class);

                myCallback.onCallback(SAT, GPA);
            }

            @Override
            public void onCancelled(DatabaseError error)
            {
                Log.d("ERROR", error.getMessage());
            }
        };
        itemRef.addListenerForSingleValueEvent(valueEventListener);
    }

    private interface MyCallback
    {
        void onCallback(String SAT, String GPA);
    }

    public void getData()
    {
        readData(new MyCallback()
        {
            @Override
            public void onCallback(final String SAT, final String GPA)
            {
                databaseSAT = SAT;
                databaseGPA = GPA;

                Log.d("Database", "Database SAT: " + SAT + ", Database GPA: " + GPA);

                InputStream is = null;
                collegeJSONData = null;
                try
                {
                    is = getAssets().open("data.txt");
                    int size = is.available();
                    byte[] buffer = new byte[size];
                    is.read(buffer);
                    collegeJSONData = new String(buffer);
                }
                catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }
                finally
                {
                    try
                    {
                        if (is != null)
                        {
                            is.close();
                        }
                    }
                    catch (IOException ioe)
                    {

                    }
                }

                BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
                bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener()
                {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item)
                    {
                        switch (item.getItemId())
                        {
                            /*
                            case R.id.mapItem:
                            {
                                Intent intent = new Intent(MainActivity.this, MapsActivity.class);
                                startActivity(intent);
                                break;
                            }

                             */

                            case R.id.homeItem:
                                break;

                            case R.id.profileItem:
                            {
                                Intent intent2 = new Intent(MainActivity.this, ProfileActivity.class);
                                startActivity(intent2);
                                break;
                            }
                        }
                        return true;
                    }
                });
            }
        });
    }

    public void method()
    {
        try
        {
            JSONObject collegesJSONObject = new JSONObject(collegeJSONData);
            collegesJSONArray = collegesJSONObject.getJSONArray("colleges");
            for (int i = 0; i < collegesJSONArray.length(); i++)
            {
                JSONObject collegeJSONObject = collegesJSONArray.getJSONObject(i);
                final String name = collegeJSONObject.getString("name");
                final double JSONGPA = collegeJSONObject.getDouble("GPA");
                final int JSONSAT = collegeJSONObject.getInt("SAT");

                final double lat = collegeJSONObject.getDouble("lat");
                final double lon = collegeJSONObject.getDouble("lon");

                try
                {
                    if ((Integer.parseInt(databaseSAT)) >= (JSONSAT - 30) && (Double.parseDouble(databaseGPA)) >= (JSONGPA - 0.2))
                    {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                        {
                            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            {
                                return;
                            }
                        }
                        else
                        {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
                        }
                        Task<Location> task = fusedLocationProviderClient.getLastLocation();
                        task.addOnSuccessListener(new OnSuccessListener<Location>()
                        {
                            @Override
                            public void onSuccess(Location location)
                            {

                                Location startPoint = new Location("locationA");
                                startPoint.setLatitude(currentLatitude);
                                startPoint.setLongitude(currentLongitude);

                                Log.d("CURRENT", currentLatitude + " HI");

                                Location endPoint = new Location("locationB");
                                endPoint.setLatitude(lat);
                                endPoint.setLongitude(lon);

                                double distanceMeters = startPoint.distanceTo(endPoint);
                                double distanceMiles = distanceMeters * 0.000621371;

                                boolean match = false;

                                Log.d("Dist", distanceMiles + "");
                                if (distanceMiles <= maxRange)
                                {
                                    match = true;
                                }

                                if (match)
                                {
                                    createListView(name, JSONGPA, JSONSAT, lat, lon);
                                    counter++;
                                }
                            }
                        });
                    }
                }
                catch (NumberFormatException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (JSONException jsonE)
        {
            jsonE.printStackTrace();
        }
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
                currentLat = lat;
                currentLong = lon;

                supportMapFragment.getMapAsync(new OnMapReadyCallback()
                {
                    @Override
                    public void onMapReady(GoogleMap googleMap)
                    {
                        map = googleMap;
                        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(MainActivity.this, R.raw.mapstyle_night));
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
                getCurrentLocation(currentLat, currentLong);
            }
        }
    }

    private class PlaceTask extends AsyncTask<String, Integer, String>
    {
        private double lat, lon;
        private String name;

        public PlaceTask(double currentLat, double currentLong, String name)
        {
            lat = currentLat;
            lon = currentLong;
            this.name = name;
        }

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
            new ParserTask(lat, lon, name).execute(s);
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
        private double lat, lon;
        private String name;
        public ParserTask(double currentLat, double currentLong, String name)
        {
            lat = currentLat;
            lon = currentLong;
            this.name = name;
        }

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
                LatLng latLng = new LatLng(lat, lon);
                MarkerOptions options = new MarkerOptions();
                options.position(latLng);
                options.title(name);
                map.addMarker(options);
            }
        }
    }

    public void method2(String latString, String lonString, String name)
    {

        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
            currentLat = Double.parseDouble(latString);
            currentLong = Double.parseDouble(lonString);

            getCurrentLocation(currentLat, currentLong);
        }
        else
        {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 44);
        }

        int i = spType.getSelectedItemPosition();
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" + "?location=" + currentLat + "," + currentLong + "&radius=160934" + "&type=" + placeTypeList[i] + "&sensor=true" + "&key=AIzaSyBr9ziVncg35qSITcJnEJqaexpz5qvSRl8";

        new PlaceTask(currentLat, currentLong, name).execute(url);
    }
}