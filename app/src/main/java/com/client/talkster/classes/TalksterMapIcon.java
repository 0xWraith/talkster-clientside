package com.client.talkster.classes;

import android.graphics.Bitmap;

import com.client.talkster.R;
import com.client.talkster.utils.FileUtils;
import com.google.android.gms.maps.model.BitmapDescriptor;
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
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dummy))
                .snippet("Send Message");
    }

    public TalksterMapIcon(String title, LatLng position)
    {
        markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_dummy))
                .snippet("Send Message");
    }

    public TalksterMapIcon(String title, LatLng position, UserJWT userJWT, long userID)
    {
        FileUtils fileUtils = new FileUtils(userJWT);
        BitmapDescriptor icon;
        Bitmap bitmap = FileUtils.getMarker(fileUtils.getProfilePicture(userID));
        if (bitmap != null) {
            icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        } else {
            icon = BitmapDescriptorFactory.fromResource(R.drawable.marker_dummy);
        }

        markerOptions = new MarkerOptions()
                .position(position)
                .title(title)
                .icon(icon)
                .snippet("Send Message");
    }

    public MarkerOptions getMarkerOptions() { return markerOptions; }
}
