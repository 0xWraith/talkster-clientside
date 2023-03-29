package com.client.talkster;


import static com.google.firebase.messaging.Constants.TAG;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MotionEventCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.Lifecycle;
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
    private ChatsFragment chatsFragment;
    private PeoplesFragment peoplesFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;
    private BroadcastReceiver sendMessageReceiver;
    private BottomNavigationView bottomNavigation;
    private View rightPager;
    private View leftPager;
    private int currentPosition = 0;
    private final int MIN_DISTANCE = 300;
    private float x1, x2;

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
        chatsFragment = new ChatsFragment(userJWT);
        peoplesFragment = new PeoplesFragment(userJWT);

        iChatListener = chatsFragment;
        homeViewPager = findViewById(R.id.homeViewPager);
        homeViewPager.setUserInputEnabled(false);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        leftPager = findViewById(R.id.leftPager);
        rightPager = findViewById(R.id.rightPager);

        fragments = new ArrayList<>(Arrays.asList(chatsFragment, mapFragment, peoplesFragment));

        initializeBottomNavigation();
        initPager();
        initializeSocketConnection();
    }

    private void initializeSocketConnection()
    {
        apiStompWebSocket = new APIStompWebSocket();
        peoplesFragment.apiStompWebSocket = apiStompWebSocket;

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

        bottomNavigation.setOnItemSelectedListener(item -> {
            int ID = item.getItemId();

            if (ID == R.id.chatMenuID) {
                currentPosition = 0;
                homeViewPager.setCurrentItem(0);
            }
            else if (ID == R.id.mapMenuID){
                currentPosition = 1;
                homeViewPager.setCurrentItem(1);
            }
            else if(ID == R.id.peoplesMenuID) {
                currentPosition = 2;
                homeViewPager.setCurrentItem(2);
            }
            return true;
        });
    }

    private void selectNavigationButton(){
        switch (currentPosition){
            case 0:
                bottomNavigation.setSelectedItemId(R.id.chatMenuID);
                break;
            case 1:
                bottomNavigation.setSelectedItemId(R.id.mapMenuID);
                break;
            case 2:
                bottomNavigation.setSelectedItemId(R.id.peoplesMenuID);
                break;

        }
    }

    private void initPager(){
        leftPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (deltaX > MIN_DISTANCE) {
                            currentPosition--;
                            if(currentPosition <= 0){
                                currentPosition = 0;
                            }
                            selectNavigationButton();}
                        break;
                }
                return true;
            }
        });

        rightPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x1 - x2;
                        if (deltaX > MIN_DISTANCE) {
                            currentPosition++;
                            if(currentPosition >= 2){
                                currentPosition = 2;
                            }
                            selectNavigationButton();}
                        break;
                }
                return true;
            }
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