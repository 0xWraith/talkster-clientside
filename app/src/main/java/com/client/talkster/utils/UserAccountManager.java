package com.client.talkster.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.client.talkster.MainActivity;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.interfaces.IMainActivityScreen;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import okhttp3.internal.Util;

public class UserAccountManager
{
    public static void saveAccount(Context context, UserJWT userJWT) { Utils.saveToSharedPreferences(context, "account_data", new Gson().toJson(userJWT)); }
    public static void getAccount(MainActivity mainActivity)
    {
        UserJWT userJWT;
        Context context = mainActivity.getApplicationContext();

        try
        {
            userJWT = new Gson().fromJson(context.getSharedPreferences("TalksterUser", 0).getString("account_data", ""), UserJWT.class);

            if(userJWT == null || userJWT.getAccessToken() == null)
            {
                ((IMainActivityScreen)mainActivity).showIntroductionScreen();
                return;
            }

            APIHandler<UserJWT, MainActivity> apiHandler = new APIHandler<>(mainActivity);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION, userJWT, userJWT.getAccessToken());
        }
        catch (IllegalStateException | JsonSyntaxException exception) { ((IMainActivityScreen)mainActivity).showIntroductionScreen(); }
    }
}
