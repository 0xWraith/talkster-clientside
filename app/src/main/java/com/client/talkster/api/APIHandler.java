package com.client.talkster.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.client.talkster.HomeActivity;
import com.client.talkster.MainActivity;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.IntroductionScreenActivity;
import com.client.talkster.controllers.authorization.InputMailActivity;
import com.client.talkster.controllers.authorization.MailConfirmationActivity;
import com.client.talkster.controllers.authorization.RegistrationActivity;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.utils.UserAccountManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class APIHandler<T, V>
{
    private final V activity;
    private final OkHttpClient okHttpClient;
//    private final String TALKSTER_SERVER_URL = "http://147.175.160.77:8000/api/v1";
    private final String TALKSTER_SERVER_URL = "http://10.10.1.103:8000/api/v1";

    private final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public APIHandler(V activity)
    {
        this.activity = activity;
        okHttpClient = new OkHttpClient();
    }

    public void apiPOST(String apiUrl, T object, String jwtToken, Context context)
    {
        RequestBody body = RequestBody.create(JSON, new Gson().toJson(object));

        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .post(body)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                Log.d("ERROR", e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                switch(apiUrl)
                {
                    case APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_FIND_USER:
                    {

                        Intent mailConfirmationIntent =  new Intent(context, MailConfirmationActivity.class);
                        mailConfirmationIntent.putExtra("userJWT", response.body().string());
                        mailConfirmationIntent.putExtra("userMail", ((AuthenticationDTO)object).getMail());
                        context.startActivity(mailConfirmationIntent);
                        ((InputMailActivity)activity).finish();

                        break;
                    }
                    case APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER:
                    {
                        int responseCode = response.code();

                        if(responseCode == 401)
                        {
                            ((MailConfirmationActivity)activity).setCodeInputToIncorrectState();
                            return;
                        }
                        //if response OK - we are already registered
                        if(responseCode == 200)
                        {
                            String body = response.body().string();
                            Intent homeIntent = new Intent(context, HomeActivity.class);

                            UserJWT userJWT = new Gson().fromJson(body, UserJWT.class);

                            homeIntent.putExtra("userJWT", body);

                            UserAccountManager.saveAccount(context, userJWT);

                            context.startActivity(homeIntent);
                            ((MailConfirmationActivity)activity).finish();

                            return;
                        }
                        //if 202 - we need to register
                        if(responseCode == 202)
                        {
                            Intent registrationIntent = new Intent(context, RegistrationActivity.class);

                            registrationIntent.putExtra("userJWT", response.body().string());
                            registrationIntent.putExtra("userMail", ((AuthenticationDTO)object).getMail());

                            context.startActivity(registrationIntent);
                            ((MailConfirmationActivity)activity).finish();
                            return;
                        }
                        break;
                    }
                    case APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER:
                    {
                        if(response.code() != 200)
                            return;

                        String body = response.body().string();
                        Intent homeIntent = new Intent(context, HomeActivity.class);

                        UserJWT userJWT = new Gson().fromJson(body, UserJWT.class);

                        homeIntent.putExtra("userJWT", body);

                        UserAccountManager.saveAccount(context, userJWT);

                        context.startActivity(homeIntent);
                        ((RegistrationActivity)activity).finish();
                    }
                    case APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION:
                    {
                        int responseCode = response.code();

                        if(responseCode == 200)
                        {
                            ((MainActivity)activity).runOnUiThread(() -> new Handler().postDelayed(() -> {

                                Intent intent = new Intent(context, HomeActivity.class);

                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                intent.putExtra("userJWT", response.body().string());

                                context.startActivity(intent);
                                ((MainActivity)activity).finish();
                            }, 500));
                        }
                        else
                        {
                            ((MainActivity)activity).runOnUiThread(() -> new Handler().postDelayed(() -> {
                                Intent intent = new Intent(context, IntroductionScreenActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(intent);
                                ((MainActivity)activity).finish();
                            }, 500));
                        }
                    }
                }
            }
        });
    }

}
