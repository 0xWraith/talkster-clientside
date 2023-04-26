package com.client.talkster.utils;

import android.content.Context;

public class Utils
{
    public static int convertDPToPx(Context context, int dp) { return (int)(dp * context.getResources().getDisplayMetrics().density + 0.5f); }

    public static String getFromSharedPreferences(Context context, String key) { return context.getSharedPreferences("TalksterUser", 0).getString(key, ""); }
    public static void saveToSharedPreferences(Context context, String key, String value) { context.getSharedPreferences("TalksterUser", 0).edit().putString(key, value).apply(); }
}
