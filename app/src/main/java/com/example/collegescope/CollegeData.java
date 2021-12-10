package com.example.collegescope;

import android.widget.ImageView;

public class CollegeData
{
    private String name;
    private double lat, lon, GPA;
    private int SAT;

    public CollegeData(String name, double lat, double lon, double GPA, int SAT)
    {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.GPA = GPA;
        this.SAT = SAT;
    }

    public String getName()
    {
        return name;
    }

    public double getLat()
    {
        return lat;
    }

    public double getLon()
    {
        return lon;
    }

    public double getGPA()
    {
        return GPA;
    }

    public int getSAT()
    {
        return SAT;
    }

}
