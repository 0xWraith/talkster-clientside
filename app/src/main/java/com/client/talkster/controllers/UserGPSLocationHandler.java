package com.client.talkster.controllers;

import static android.content.Context.LOCATION_SERVICE;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.client.talkster.interfaces.IGPSPositionUpdate;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class UserGPSLocationHandler
{
    private final Context context;
    private LocationManager locationManager;
    private LocationCallback locationCallback;
    private final LocationRequest locationRequest;
    private final IGPSPositionUpdate gpsPositionUpdate;
    private static final int LOCATION_REQUEST_INTERVAL = 5000;
    private static final int LOCATION_REQUEST_FASTEST_INTERVAL = 3000;

    public UserGPSLocationHandler(Service locationService) throws RuntimeException
    {

        this.context = locationService.getApplicationContext();

        if(locationService instanceof IGPSPositionUpdate)
            gpsPositionUpdate = (IGPSPositionUpdate) locationService;
        else
            throw new RuntimeException("LocationService must implement IGPSPositionUpdate interface");

        if (!isNetworkEnabled() && !isGPSEnabled())
            throw new RuntimeException("GPS and Network are not enabled");

        locationRequest = LocationRequest.create();

        initLocationRequest();
        initLocationCallback();
    }

    public void start() { initFusedLocationProviderClient(); }

    @SuppressLint("MissingPermission")
    private void initFusedLocationProviderClient()
    {
        if (!isLocationPermissionGranted())
            return;

        LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void initLocationCallback()
    {
        locationCallback = new LocationCallback()
        {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult)
            {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                gpsPositionUpdate.onGPSPositionUpdate(location);
            }
        };
    }

    private void initLocationRequest()
    {
        locationRequest.setInterval(LOCATION_REQUEST_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_REQUEST_FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    public boolean isNetworkEnabled()
    {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public boolean isGPSEnabled()
    {
        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
    public boolean isLocationPermissionGranted() { return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED; }
}
