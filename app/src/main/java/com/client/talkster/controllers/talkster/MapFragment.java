package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.client.talkster.R;
import com.client.talkster.adapters.LocationAdapter;
import com.client.talkster.classes.TalksterMapIcon;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.LocationDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IMapGPSPositionUpdate;
import com.client.talkster.utils.BundleExtraNames;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import java.util.HashMap;

public class MapFragment extends Fragment implements IFragmentActivity, OnMapReadyCallback, IMapGPSPositionUpdate
{
    private GoogleMap map;
    private UserJWT userJWT;
    private MapView mapView;
    private final float ZOOM_LEVEL = 15.0f;
    private Bundle mapViewBundle = null;
    private LocationAdapter userLastLocation;


    private Marker userMarker;
    private HashMap<Long, Marker> userMarkers = new HashMap<>();

    public MapFragment(UserJWT userJWT) { this.userJWT = userJWT; }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        getUIElements(view);
        initGoogleMaps(savedInstanceState);

        return view;
    }

    private void initGoogleMaps(Bundle savedInstanceState)
    {

        if (savedInstanceState != null)
            mapViewBundle = savedInstanceState.getBundle(BundleExtraNames.MAPVIEW_BUNDLE_KEY);

        mapView.onCreate(mapViewBundle);
        mapView.getMapAsync(this);
    }

    private void moveCameraToUserLocation(LocationAdapter locationAdapter)
    {
        if(locationAdapter == null || map == null)
            return;

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()), ZOOM_LEVEL));
    }

    @Override
    public void getUIElements(View view)
    {
        mapView = view.findViewById(R.id.mapView);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        mapView.onStop();
    }

    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap map)
    {
        this.map = map;

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.style_night));

        map.getUiSettings().setMapToolbarEnabled(false);
        map.getUiSettings().setMyLocationButtonEnabled(false);
        map.getUiSettings().setCompassEnabled(false);

        map.setOnMarkerClickListener(marker -> {

            LatLng latLng = marker.getPosition();
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)
                    .zoom(ZOOM_LEVEL)
                    .tilt(30)
                    .build();

            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            marker.showInfoWindow();
            return true;
        });

        map.setOnInfoWindowClickListener(marker -> {
            Log.d("MapFragment", "onInfoWindowClick: " + marker.getTitle());
        });

        moveCameraToUserLocation(userLastLocation);
    }

    @Override
    public void onPause()
    {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy()
    {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onMapGPSPositionUserUpdate(Location location)
    {
        LocationAdapter locationAdapter = userLastLocation = new LocationAdapter(location);

        if(map == null)
            return;

        if(userMarker != null)
            userMarker.setPosition(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()));
        else
            userMarker = map.addMarker(new TalksterMapIcon("You", new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()), userJWT).getMarkerOptions());
    }

    @Override
    public void onMapGPSPositionUpdate(String locationRAW)
    {
        if(map == null)
            return;

        long id = 0;
        LocationDTO locationDTO = new Gson().fromJson(locationRAW, LocationDTO.class);
        LocationAdapter locationAdapter = new LocationAdapter(locationDTO);

        id = locationDTO.getuserid();

        if(id == userJWT.getID())
            return;

        if(userMarkers.containsKey(id))
            userMarkers.get(id).setPosition(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()));
        else
            userMarkers.put(id, map.addMarker(new TalksterMapIcon(locationDTO.getUsername(), new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude())).getMarkerOptions()));

    }
}