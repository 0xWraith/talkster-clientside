package com.client.talkster.controllers.talkster;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.client.talkster.R;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.interfaces.IFragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapFragment extends Fragment implements IFragmentActivity, OnMapReadyCallback
{
    private UserJWT userJWT;
    private MapView mapView;

    public MapFragment(UserJWT userJWT)
    {
        this.userJWT = userJWT;
    }
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private Bundle mapViewBundle = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getUIElements(view);
        initGooglemaps(savedInstanceState);
        return view;
    }

    private void initGooglemaps(Bundle savedInstanceState){
        // MapView requires that the Bundle you pass contain _ONLY_ MapView SDK
        // objects or sub-Bundles.
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }

        mapView.onCreate(mapViewBundle);

        mapView.getMapAsync(this);
    }

    @Override
    public void getUIElements(View view)
    {
        mapView = (MapView) view.findViewById(R.id.mapView);
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        map.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_light));

            if (!success) {
                Log.e("Google Maps", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("Google Maps", "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}