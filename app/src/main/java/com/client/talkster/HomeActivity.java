package com.client.talkster;


import static com.google.firebase.messaging.Constants.TAG;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.api.WebSocketPrivateChatSubscriber;
import com.client.talkster.api.WebSocketPublicChatSubscriber;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.TokenDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IChatWebSocketHandler;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IChatWebSocketHandler
{
    private UserJWT userJWT;
    private String FCMToken;
    private MapFragment mapFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;
    private BroadcastReceiver sendMessageReceiver;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        mapFragment = new MapFragment(userJWT);
        ChatsFragment chatsFragment = new ChatsFragment(userJWT);
        PeoplesFragment peoplesFragment = new PeoplesFragment();

        iChatListener = chatsFragment;
        homeViewPager = findViewById(R.id.homeViewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        fragments = new ArrayList<>(Arrays.asList(chatsFragment, mapFragment, peoplesFragment));

        initializeBottomNavigation();
        initializeSocketConnection();
    }

    private void initializeSocketConnection()
    {
        apiStompWebSocket = new APIStompWebSocket();
        mapFragment.apiStompWebSocket = apiStompWebSocket;

        apiStompWebSocket.addTopic("/chatroom/public", new WebSocketPublicChatSubscriber(this));
        apiStompWebSocket.addTopic("/user/"+ userJWT.getID() +"/private", new WebSocketPrivateChatSubscriber(this));
        apiStompWebSocket.connect();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        userJWT = bundle.getParcelable(BundleExtraNames.USER_JWT);

        Log.d("Talkfster", "UserJWT: " + new Gson().toJson(userJWT));

        sendMessageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (!intent.getAction().equals(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST))
                    return;

                MessageDTO messageDTO = (MessageDTO) intent.getExtras().get(BundleExtraNames.CHAT_NEW_MESSAGE);

                Log.d("Talkster", "Sending message: " + new Gson().toJson(messageDTO));

                apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
            }
        };
    }

    @Override
    protected void onStart() {
        super.onStart();
        getToken();
    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        if (Objects.equals(token, FCMToken)){
                            return;
                        }
                        FCMToken = token;
                        putToken();
                    }
                });
    }

    private void putToken(){
        TokenDTO tokenDTO = new TokenDTO();
        APIHandler<TokenDTO, HomeActivity> apiHandler = new APIHandler<>(this);
        tokenDTO.setToken(FCMToken);
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_NOTIFICATION_ADD_TOKEN,tokenDTO,userJWT.getJWTToken());
        Log.d(TAG, FCMToken);
        Log.d(TAG, "FCM Token Posted!");
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
        registerReceiver(sendMessageReceiver, filter);
        Log.d("Talkster", "Registered receiver");
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

            if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT))
            {
                Chat chat = new Gson().fromJson(responseBody, Chat.class);
                runOnUiThread (() -> iChatListener.addChat(chat));
            }
            else if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS))
            {
                if(responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);

                Chat[] chats = new Gson().fromJson(responseBody, Chat[].class);
                List<Chat> chatList = new ArrayList<>(Arrays.asList(chats));

                runOnUiThread (() -> iChatListener.updateChatList(chatList));
            }
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }

    private void initializeBottomNavigation()
    {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        homeViewPager.setAdapter(viewPagerAdapter);

        homeViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { super.onPageScrolled(position, positionOffset, positionOffsetPixels); }
            @Override
            public void onPageSelected(int position)
            {
                int ID = -1;
                switch(position)
                {
                    case 0:
                        ID = R.id.chatMenuID;
                        break;
                    case 1:
                        ID = R.id.mapMenuID;
                        break;
                    case 2:
                        ID = R.id.peoplesMenuID;
                }
                bottomNavigation.setSelectedItemId(ID);
                super.onPageSelected(position);
            }
        });

        bottomNavigation.setOnItemSelectedListener(item -> {
            int ID = item.getItemId();

            if(ID == R.id.chatMenuID)
                homeViewPager.setCurrentItem(0);

            else if(ID == R.id.mapMenuID)
                homeViewPager.setCurrentItem(1);

            else if(ID == R.id.peoplesMenuID)
                homeViewPager.setCurrentItem(2);

            return true;
        });
    }

    @Override
    public void onMessageReceived(String messageRAW) { iChatListener.onMessageReceived(messageRAW); }

    @Override
    public void onSendPrivateMessage(Message message)
    {
        Log.d("Talkster", "onSendPrivateMessage: " + message.toString());
    }
}