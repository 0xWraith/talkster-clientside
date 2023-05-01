package com.client.talkster.controllers.talkster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.client.talkster.HomeActivity;
import com.client.talkster.R;
import com.client.talkster.adapters.LocationAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.TalksterMapIcon;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.LocationDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.interfaces.IMapGPSPositionUpdate;
import com.client.talkster.interfaces.theme.IThemeManagerFragmentListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapFragment extends Fragment implements IFragmentActivity, OnMapReadyCallback, IMapGPSPositionUpdate, IThemeManagerFragmentListener
{


    private float x1,x2;
    private boolean setLocation = false;
    private final int MIN_DISTANCE = 300;
    private final float ZOOM_LEVEL = 15.0f;

    private Bundle mapViewBundle = null;
    private ToolbarElements toolbarElements;
    private LocationAdapter userLastLocation;
    private HashMap<Long, Marker> userMarkers = new HashMap<>();

    private ImageView toolbarLogoIcon;

    private GoogleMap map;
    private MapView mapView;
    private Marker userMarker;
    private EditText searchEditText;
    private ConstraintLayout mapLayout;
    private View rightPager, leftPager;
    private ImageButton plusButton, minusButton;
    private List<EditText> inputs = new ArrayList<>();

    private final String[] PERMISSIONS = {
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
    };

    public MapFragment() { }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int permCoarseLoc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION);
        int permFineLoc = ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);

        if (permCoarseLoc != PackageManager.PERMISSION_GRANTED
                || permFineLoc != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), PERMISSIONS, 1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        getUIElements(view);
        initGoogleMaps(savedInstanceState);
        //new Thread(() -> ).start();
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

        map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()), ZOOM_LEVEL));
    }

    @Override
    public void getUIElements(View view)
    {
        ImageButton toolbarMenuIcon;

        toolbarElements = new ToolbarElements();

        toolbarMenuIcon = view.findViewById(R.id.toolbarMenuIcon);
        toolbarLogoIcon = view.findViewById(R.id.toolbarLogoIcon);

        mapLayout = view.findViewById(R.id.mapLayout);
        mapView = view.findViewById(R.id.mapView);

        rightPager = view.findViewById(R.id.rightPager);
        leftPager = view.findViewById(R.id.leftPager);

        plusButton = view.findViewById(R.id.plusButton);
        minusButton = view.findViewById(R.id.minusButton);

        toolbarElements.setToolbar(view.findViewById(R.id.toolbar));
        toolbarElements.addToolbarIcon(toolbarMenuIcon);

        searchEditText = view.findViewById(R.id.toolbarInput);
        inputs.add(searchEditText);

        initPager();

        plusButton.setOnClickListener(view1 -> {
            float zoom = map.getCameraPosition().zoom;
            zoom += 1.5f;
            if (zoom > map.getMaxZoomLevel())
                zoom = map.getMaxZoomLevel();
            map.animateCamera( CameraUpdateFactory.zoomTo(zoom) );
        });

        minusButton.setOnClickListener(view1 -> {
            float zoom = map.getCameraPosition().zoom;
            zoom -= 1.5f;
            if (zoom < map.getMinZoomLevel())
                zoom = map.getMinZoomLevel();
            map.animateCamera( CameraUpdateFactory.zoomTo(zoom) );
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mapView.onResume();
        updateMarkerIcons();

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
            long id = (long) marker.getTag();
            APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHAT+"/"+id, UserAccount.getInstance().getUserJWT().getAccessToken());
        });

        setLocation = true;
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

        if (setLocation) {
            moveCameraToUserLocation(userLastLocation);
            setLocation = false;
        }

        if(userMarker != null)
            userMarker.setPosition(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()));

        else
        {
            UserJWT userJWT = UserAccount.getInstance().getUserJWT();

            userMarker = map.addMarker(new TalksterMapIcon("You", new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()), userJWT, userJWT.getID()).getMarkerOptions());
            userMarker.setTag(userJWT.getID());
        }
    }

    @Override
    public void onMapGPSPositionUpdate(String locationRAW)
    {
        if(map == null)
            return;

        long id = 0;
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();
        LocationDTO locationDTO = new Gson().fromJson(locationRAW, LocationDTO.class);
        LocationAdapter locationAdapter = new LocationAdapter(locationDTO);

        id = locationDTO.getuserid();

        if(id == userJWT.getID())
            return;

        if(userMarkers.containsKey(id)) {
            userMarkers.get(id).setPosition(new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()));
            userMarkers.get(id).setTitle(locationDTO.getUsername());
        }
        else {
            userMarkers.put(id, map.addMarker(new TalksterMapIcon(locationDTO.getUsername(), new LatLng(locationAdapter.getLatitude(), locationAdapter.getLongitude()), userJWT, id).getMarkerOptions()));
            userMarkers.get(id).setTag(id);
        }
    }

    public void updateMarkerIcons()
    {
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        FileUtils fileUtils = new FileUtils();
        Bitmap bitmap = FileUtils.getMarker(fileUtils.getProfilePicture(userJWT.getID()));
        if (userMarker != null) {
            userMarker.setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
        for (Long id : userMarkers.keySet()){
            bitmap = FileUtils.getMarker(fileUtils.getProfilePicture(id));
            userMarkers.get(id).setIcon(BitmapDescriptorFactory.fromBitmap(bitmap));
        }
    }

    private void initPager(){
        leftPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (deltaX > MIN_DISTANCE) {
                            ((HomeActivity)getActivity()).selectNavigationButton(0);}
                        break;
                }
                return true;
            }
        });

        rightPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x1 - x2;
                        if (deltaX > MIN_DISTANCE) {
                            ((HomeActivity)getActivity()).selectNavigationButton(2);}
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onThemeChanged()
    {
        ThemeManager.changeToolbarColor(toolbarElements);

        toolbarLogoIcon.setColorFilter(ThemeManager.getColor("actionBarDefaultIcon"));
        mapLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));
        searchEditText.setCompoundDrawableTintList(ColorStateList.valueOf(ThemeManager.getColor("settings_subText")));
        ThemeManager.changeInputColor(inputs);
    }
}