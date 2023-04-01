package com.client.talkster.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

public class PermissionChecker
{
    public static boolean isPermissionGranted(Context context, String permission) { return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED; }
}
