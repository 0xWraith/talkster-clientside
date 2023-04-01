package com.client.talkster.adapters;

import android.location.Location;

import com.client.talkster.dto.LocationDTO;

import java.math.RoundingMode;
import java.text.DecimalFormat;

public class LocationAdapter
{
    private double speed;
    private double latitude;
    private double longitude;

    public LocationAdapter(Location location)
    {
        this.latitude = roundLocation(location.getLatitude());
        this.longitude = roundLocation(location.getLongitude());
        this.speed = roundLocation(location.getSpeed() * 3.6);
    }

    public LocationAdapter(LocationDTO location)
    {
        this.latitude = roundLocation(location.getLatitude());
        this.longitude = roundLocation(location.getLongitude());
        this.speed = roundLocation(location.getSpeed());
    }

    private double roundLocation(double position)
    {
        DecimalFormat df = new DecimalFormat("#.####");
        df.setRoundingMode(RoundingMode.CEILING);

        return Double.parseDouble(df.format(position).replace(",", "."));
    }

    public double getSpeed() { return speed; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    @Override
    public String toString()
    {
        return "LocationAdapter{" +
                "speed=" + speed +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }

}
