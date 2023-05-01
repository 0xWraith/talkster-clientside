package com.client.talkster.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.client.talkster.R;
import com.client.talkster.controllers.UserGPSLocationHandler;
import com.client.talkster.interfaces.IGPSPositionUpdate;
import com.client.talkster.utils.BundleExtraNames;

import java.util.Objects;

public class LocationService extends Service implements IGPSPositionUpdate
{

    private UserGPSLocationHandler userGPSLocationHandler;

    @Override
    public void onCreate()
    {
        super.onCreate();
        userGPSLocationHandler = new UserGPSLocationHandler(this);
        userGPSLocationHandler.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(Objects.equals(intent.getAction(), BundleExtraNames.LOCATION_SERVICE_START))
            start();
        else if(Objects.equals(intent.getAction(), BundleExtraNames.LOCATION_SERVICE_STOP))
            stop();

        return super.onStartCommand(intent, flags, startId);
    }

    private void stop()
    {
        Log.d("LocationService", "stop");
        userGPSLocationHandler.stop();
        stopForeground(true);
        stopSelf();
    }

    private void start()
    {
        startForeground(1, getNotification(null));
    }

    private Notification getNotification(Location location)
    {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel notificationChannel = new NotificationChannel("talkster_location_service", "Location service", NotificationManager.IMPORTANCE_MIN);
        notificationManager.createNotificationChannel(notificationChannel);

        if(location == null)
            return new Notification.Builder(this, "talkster_location_service")
                    .setContentTitle("Talkster")
                    .setContentText("Location: null")
                    .setSmallIcon(R.drawable.talkster_logo)
                    .setOngoing(true).build();

        return new Notification.Builder(this, "talkster_location_service")
                .setContentTitle("Talkster")
                .setContentText("Location: " + String.valueOf(location.getLatitude()).substring(0, 6) + ", " + String.valueOf(location.getLongitude()).substring(0, 6) + ", " + location.getSpeed() * 3.6 + " km/h")
                .setSmallIcon(R.drawable.talkster_logo)
                .setOngoing(true).build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onGPSPositionUpdate(Location location)
    {
        Notification notification = getNotification(location);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        notificationManager.notify(1, notification);

        Intent intent = new Intent(BundleExtraNames.LOCATION_SERVICE_BROADCAST);
        intent.putExtra(BundleExtraNames.LOCATION_SERVICE_POSITION, location);
        sendBroadcast(intent);
    }
}
