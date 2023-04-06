package com.client.talkster.classes;

import android.graphics.Bitmap;

import com.client.talkster.R;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class TalksterMapIcon
{
    private final MarkerOptions markerOptions;

    public TalksterMapIcon(String title)
    {
        markerOptions = new MarkerOptions()
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.avatar))
                .snippet("Send Message");
    }

    public TalksterMapIcon(String title, LatLng position)
    {
        markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.avatar))
                .snippet("Send Message");
    }

    public MarkerOptions getMarkerOptions() { return markerOptions; }
}
