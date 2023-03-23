package com.client.talkster;

import okhttp3.Call;

import android.content.SharedPreferences;
import android.util.Log;
import okhttp3.Response;
import android.os.Bundle;
import android.os.Handler;
import java.io.IOException;
import com.google.gson.Gson;
import android.content.Intent;
import androidx.annotation.NonNull;
import com.google.gson.JsonSyntaxException;
import com.client.talkster.classes.UserJWT;
import androidx.appcompat.app.AppCompatActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.UserAccountManager;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IMainActivityScreen;
import com.client.talkster.controllers.IntroductionScreenActivity;

public class MainActivity extends AppCompatActivity implements IMainActivityScreen, IAPIResponseHandler
{
    private final int SPLASH_DISPLAY_LENGTH = 750;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserAccountManager.getAccount(this);
    }

    @Override
    public void showHomeScreen(UserJWT userJWT)
    {
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(this, HomeActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(BundleExtraNames.USER_JWT, userJWT);

            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void showIntroductionScreen()
    {
       new Handler().postDelayed(() ->
       {
            Intent intent = new Intent(this, IntroductionScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {

    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);

            int responseCode = response.code();
            String responseBody = response.body().string();

            if(responseCode != 200)
            {
                SharedPreferences.Editor editor = getSharedPreferences("TalksterUser", 0).edit();

                editor.putString("account_data", "");
                editor.apply();

                runOnUiThread(this::showIntroductionScreen);
                return;
            }
            UserJWT userJWT = new Gson().fromJson(responseBody, UserJWT.class);
            runOnUiThread(() -> showHomeScreen(userJWT));
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse JWT token: " + exception.getMessage()); }
    }
}