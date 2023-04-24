package com.client.talkster;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.IntroductionScreenActivity;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.dto.VerifiedUserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IMainActivityScreen;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.UserAccountManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements IMainActivityScreen, IAPIResponseHandler
{
    private final int SPLASH_DISPLAY_LENGTH = 750;

    private UserJWT userJWT;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserAccountManager.getAccount(this);
    }

    @Override
    public void showHomeScreen(VerifiedUserDTO verifiedUserDTO)
    {
        new Handler().postDelayed(() -> {

            Intent intent = new Intent(this, HomeActivity.class);

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            User user = verifiedUserDTO.getUser();
            UserJWT userJWT = verifiedUserDTO.getUserJWT();

            intent.putExtra(BundleExtraNames.USER_JWT, userJWT);
            intent.putExtra(BundleExtraNames.USER, user);

            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

    public void showOfflineScreen()
    {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
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
        showOfflineScreen();
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

            VerifiedUserDTO verifiedUserDTO = new Gson().fromJson(responseBody, VerifiedUserDTO.class);

            runOnUiThread(() -> showHomeScreen(verifiedUserDTO));
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse JWT token: " + exception.getMessage()); }
    }
}