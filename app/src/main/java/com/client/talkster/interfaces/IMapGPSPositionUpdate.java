package com.client.talkster.interfaces;

import android.location.Location;

public interface IMapGPSPositionUpdate
{
    void onMapGPSPositionUpdate(String locationRAW);
    void onMapGPSPositionUserUpdate(Location location);
}
