package com.client.talkster.controllers;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.HomeActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.VerifiedUserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import io.github.muddz.styleabletoast.StyleableToast;
import okhttp3.Call;
import okhttp3.Response;

public class OfflineActivity extends AppCompatActivity implements IAPIResponseHandler, IActivity {

    private Button retryButton;
    private ProgressBar progressBar;
    private boolean doReload = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline);

        getUIElements();
    }

    @Override
    public void getUIElements() {
        retryButton = findViewById(R.id.retryButton);
        progressBar = findViewById(R.id.progressBar);

        retryButton.setOnClickListener(view -> {
            progressBar.setVisibility(View.VISIBLE);
            getAccount();
        });
    }

    @Override
    public void getBundleElements() {

    }

    @Override
    public void onResume() {
        super.onResume();
        if (isNetworkConnected() && doReload){
            getAccount();
        }
        doReload = true;
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public void showIntroductionScreen()
    {
        Intent intent = new Intent(this, IntroductionScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    public void showHomeScreen(VerifiedUserDTO verifiedUserDTO)
    {
        Intent intent = new Intent(this, HomeActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        User user = verifiedUserDTO.getUser();
        UserJWT userJWT = verifiedUserDTO.getUserJWT();

        intent.putExtra(BundleExtraNames.USER_JWT, userJWT);
        intent.putExtra(BundleExtraNames.USER, user);

        startActivity(intent);
        finish();
    }

    private void getAccount()
    {
        UserJWT userJWT;
        Context context = this.getApplicationContext();

        if (!isNetworkConnected()){
            StyleableToast.makeText(this, "Failed to connect!", R.style.customToast).show();
            progressBar.setVisibility(View.INVISIBLE);
            return;
        }

        try
        {
            userJWT = new Gson().fromJson(context.getSharedPreferences("TalksterUser", 0).getString("account_data", ""), UserJWT.class);

            if(userJWT == null || userJWT.getAccessToken() == null)
            {
                showIntroductionScreen();
                return;
            }

            APIHandler<UserJWT, OfflineActivity> apiHandler = new APIHandler<>(this);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION, userJWT, userJWT.getAccessToken());
        }
        catch (IllegalStateException | JsonSyntaxException exception) { showIntroductionScreen(); }
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl) {
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

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) {
        runOnUiThread(() -> StyleableToast.makeText(this, "Failed to connect!", R.style.customToast).show());
        progressBar.setVisibility(View.INVISIBLE);
        return;
    }
}