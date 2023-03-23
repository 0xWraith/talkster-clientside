package com.client.talkster.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;

import com.client.talkster.MainActivity;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.IntroductionScreenActivity;

public class UserAccountManager
{
    public static void saveAccount(Context context, UserJWT userJWT)
    {
        SharedPreferences settings = context.getSharedPreferences("TalksterUser", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putLong("ID", userJWT.getID());
        editor.putString("accessJWTToken", userJWT.getJWTToken());
        editor.putString("refreshJWTToken", userJWT.getJWTToken());
        editor.apply();
    }
    public static void getAccount(MainActivity mainActivity)
    {
        UserJWT userJWT = new UserJWT();
        Context context = mainActivity.getApplicationContext();
        SharedPreferences settings = context.getSharedPreferences("TalksterUser", 0);

        userJWT.setID(settings.getLong("ID", 0));
        userJWT.setJWTToken(settings.getString("accessJWTToken", ""));

        Log.d("JWT", userJWT.toString());

        APIHandler<UserJWT, MainActivity> apiHandler = new APIHandler<>(mainActivity);
        apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION, userJWT, userJWT.getJWTToken(), context);
    }
}
