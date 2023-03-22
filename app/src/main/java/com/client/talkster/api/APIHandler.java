package com.client.talkster.api;

import okhttp3.Call;
import java.util.List;
import okhttp3.Request;
import android.util.Log;
import okhttp3.Response;
import okhttp3.Callback;
import java.util.Arrays;
import android.os.Handler;
import java.util.ArrayList;
import java.io.IOException;
import okhttp3.RequestBody;
import okhttp3.OkHttpClient;
import com.google.gson.Gson;
import android.content.Intent;
import android.content.Context;
import androidx.annotation.NonNull;
import com.client.talkster.HomeActivity;
import com.client.talkster.MainActivity;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.utils.UserAccountManager;
import com.client.talkster.controllers.IntroductionScreenActivity;
import com.client.talkster.controllers.authorization.InputMailActivity;
import com.client.talkster.controllers.authorization.MailConfirmationActivity;
import com.client.talkster.controllers.authorization.RegistrationActivity;

public class APIHandler<T, V>
{
    private final V activity;
    private OkHttpClient okHttpClient;
    public final String TALKSTER_SERVER_URL = APIConfig.TALKSTER_SERVER_INTERNET_PROTOCOL + APIConfig.TALKSTER_SERVER_IP + ":" + APIConfig.TALKSTER_SERVER_PORT;

    public APIHandler(V activity)
    {
        this.activity = activity;
        okHttpClient = new OkHttpClient();
    }

    public void apiGET(String apiUrl, String jwtToken, Context context)
    {
        Request request = new Request
                .Builder()
                .url(TALKSTER_SERVER_URL + apiUrl)
                .addHeader("Authorization", jwtToken)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                int responseCode = response.code();


                if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT))
                {

                    if(responseCode != 200)
                        return;

                    String resp = response.body().string();

                    HomeActivity homeActivity = ((HomeActivity)activity);
                    Chat chat = new Gson().fromJson(resp, Chat.class);
                    Log.d("APIHandler", "onResponse: " + resp);
                    homeActivity.runOnUiThread (() -> homeActivity.addNewChat(chat));
                    return;
                }

                switch(apiUrl)
                {
                    case APIEndpoints.TALKSTER_API_CHAT_GET_CHATS:
                    {
                        if(responseCode != 200)
                            return;

                        HomeActivity homeActivity = ((HomeActivity)activity);

                        String resp = response.body().string();

                        Chat[] chats = new Gson().fromJson(resp, Chat[].class);
                        List<Chat> chatList = new ArrayList<>(Arrays.asList(chats));

                        homeActivity.runOnUiThread (() -> homeActivity.updateChatList(chatList));
                        break;
                    }
                }
            }
        });
    }

    public void apiPOST(String apiUrl, T object, String jwtToken, Context context)
    {
        RequestBody body = RequestBody.create(APIConfig.JSON, new Gson().toJson(object));

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

                        UserJWT userJWT = new Gson().fromJson(body, UserJWT.class);
                        Intent homeIntent = new Intent(context, HomeActivity.class);
                        UserAccountManager.saveAccount(context, userJWT);

                        homeIntent.putExtra("userJWT", body);

                        context.startActivity(homeIntent);
                        ((RegistrationActivity)activity).finish();
                        break;
                    }
                    case APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION:
                    {
                        int responseCode = response.code();

                        String JWTResponse = response.body().string();

                        if(responseCode == 200)
                        {
                            ((MainActivity)activity).runOnUiThread(() -> new Handler().postDelayed(() -> {

                                Intent intent = new Intent(context, HomeActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.putExtra("userJWT", JWTResponse);

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
