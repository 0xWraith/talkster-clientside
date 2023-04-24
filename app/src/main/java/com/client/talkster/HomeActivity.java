package com.client.talkster;


import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

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
import com.client.talkster.api.WebSocketPublicMapSubscriber;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.dto.LocationDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.TokenDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IBroadcastRegister;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IChatWebSocketHandler;
import com.client.talkster.interfaces.IMapGPSPositionUpdate;
import com.client.talkster.interfaces.IMapWebSocketHandler;
import com.client.talkster.interfaces.IThemeManagerActivityListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
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

public class HomeActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IChatWebSocketHandler, IMapWebSocketHandler, IBroadcastRegister, IThemeManagerActivityListener
{
    private User user;
    private UserJWT userJWT;
    private String FCMToken;
    private MapFragment mapFragment;
    private ChatsFragment chatsFragment;
    private PeoplesFragment peoplesFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private IMapGPSPositionUpdate iMapGPSPositionUpdate;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;

    private BroadcastReceiver chatBroadCastReceiver;
    private BroadcastReceiver locationBroadCastReceiver;

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

        ThemeManager.addListener(this);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        mapFragment = new MapFragment(userJWT);
        chatsFragment = new ChatsFragment(userJWT);
        peoplesFragment = new PeoplesFragment(userJWT, user);

        iChatListener = chatsFragment;
        iMapGPSPositionUpdate = mapFragment;

        homeViewPager = findViewById(R.id.homeViewPager);
        homeViewPager.setUserInputEnabled(false);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        leftPager = findViewById(R.id.leftPager);
        rightPager = findViewById(R.id.rightPager);

        fragments = new ArrayList<>(Arrays.asList(chatsFragment, mapFragment, peoplesFragment));

        registerBroadCasts();
        initializeBottomNavigation();
        initPager();
        initializeSocketConnection();

//        Intent intent = new Intent(this, LocationService.class);
//        intent.setAction(BundleExtraNames.LOCATION_SERVICE_START);
//        startService(intent);

    }

    private void initializeSocketConnection()
    {
        apiStompWebSocket = new APIStompWebSocket();
        peoplesFragment.apiStompWebSocket = apiStompWebSocket;

        apiStompWebSocket.addTopic("/chatroom/public", new WebSocketPublicChatSubscriber(this));
        apiStompWebSocket.addTopic("/user/"+ userJWT.getID() +"/private", new WebSocketPrivateChatSubscriber(this));
        apiStompWebSocket.addTopic("/map/public", new WebSocketPublicMapSubscriber(this));

        apiStompWebSocket.connect();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        user = bundle.getParcelable(BundleExtraNames.USER);
        userJWT = bundle.getParcelable(BundleExtraNames.USER_JWT);
    }
    @Override
    public void registerBroadCasts()
    {
        chatBroadCastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();

                Log.d("BROADCAST", "onReceive: " + action);

                if(action.equals(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST))
                {
                    MessageDTO messageDTO = (MessageDTO) bundle.get(BundleExtraNames.CHAT_SEND_MESSAGE_BUNDLE);
                    apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
                }
                else if(action.equals(BundleExtraNames.CHAT_ACTION_BROADCAST))
                {
                    long chatID;
                    MessageDTO messageDTO;
                    EPrivateChatAction actionType;

                    chatID = (long)bundle.get(BundleExtraNames.CHAT_ACTION_CHAT_ID);
                    messageDTO = bundle.getParcelable(BundleExtraNames.CHAT_ACTION_MESSAGE_DATA);
                    actionType = (EPrivateChatAction) bundle.get(BundleExtraNames.CHAT_ACTION_TYPE);

                    switch(actionType)
                    {
                        case CLEAR_CHAT_HISTORY:
                        {
                            iChatListener.onChatHistoryCleared(chatID);
                            break;
                        }

                        case DELETE_CHAT:
                        {
                            iChatListener.onChatDeleted(chatID);
                            break;
                        }
                        case MUTE_CHAT:
                        {
                            iChatListener.onChatMuted(chatID, messageDTO.getreceiverid());
                            return;
                        }
                    }

                    if(messageDTO == null)
                        return;

                    apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
                }
            }
        };

        locationBroadCastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(!action.equals(BundleExtraNames.LOCATION_SERVICE_BROADCAST))
                    return;

                Location location = (Location) intent.getExtras().get(BundleExtraNames.LOCATION_SERVICE_POSITION);

                iMapGPSPositionUpdate.onMapGPSPositionUserUpdate(location);

                LocationDTO locationDTO = new LocationDTO(userJWT.getID(), user.getFullName(), userJWT.getAccessToken(), location);
                apiStompWebSocket.getWebSocketClient().send("/app/map", new Gson().toJson(locationDTO)).subscribe();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
        intentFilter.addAction(BundleExtraNames.CHAT_ACTION_BROADCAST);

        registerReceiver(chatBroadCastReceiver, intentFilter);
        registerReceiver(locationBroadCastReceiver, new IntentFilter(BundleExtraNames.LOCATION_SERVICE_BROADCAST));
    }

    @Override
    public void unregisterBroadCasts()
    {

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getToken();
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful())
                return;

            String token = task.getResult();

            if (Objects.equals(token, FCMToken))
                return;

            FCMToken = token;
            putToken();
        });
    }

    private void putToken()
    {
        TokenDTO tokenDTO = new TokenDTO();
        APIHandler<TokenDTO, HomeActivity> apiHandler = new APIHandler<>(this);
        tokenDTO.setToken(FCMToken);
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_NOTIFICATION_ADD_TOKEN, tokenDTO, userJWT.getAccessToken());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(chatBroadCastReceiver);
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


            if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT))
            {
                String responseBody = response.body().string();
                Chat chat = new Gson().fromJson(responseBody, Chat.class);
                runOnUiThread (() -> iChatListener.addChat(chat));
            }
            else if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS))
            {
                if(responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);
                String responseBody = response.body().string();
                Chat[] chats = new Gson().fromJson(responseBody, Chat[].class);
                List<Chat> chatList = new ArrayList<>(Arrays.asList(chats));

                runOnUiThread (() -> iChatListener.updateChatList(chatList));
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_GET_PROFILE))
            {
                if (responseCode != 200){
                    throw new UserUnauthorizedException("Unexpected response " + response);
                }
                Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                runOnUiThread (() -> peoplesFragment.setProfilePicture(bitmap));
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
    @Deprecated
    public void onSendPrivateMessage(Message message) { }

    @Override
    public void onMapMessageReceived(String locationRAW) { iMapGPSPositionUpdate.onMapGPSPositionUpdate(locationRAW); }

    @Override
    public void onThemeChanged()
    {
        setTheme(ThemeManager.getCurrentTheme());

        ThemeManager.reloadThemeColors(this);
        ColorStateList colorStateList = getColorStateList(R.color.menu_item_state);

        int[] colors = new int[]{
                colorStateList.getColorForState(new int[] { -android.R.attr.state_checked }, 0),
                colorStateList.getColorForState(new int[] { android.R.attr.state_checked }, 0)
        };

        colors[0] = ThemeManager.getColor("navBarTabUnactiveIcon");
        colors[1] = ThemeManager.getColor("navBarTabActiveIcon");

        ColorStateList newColorStateList = new ColorStateList(new int[][] { new int[]{-android.R.attr.state_checked}, new int[]{android.R.attr.state_checked}}, colors);
        bottomNavigation.setItemIconTintList(newColorStateList);

        peoplesFragment.onThemeChanged();
        Log.d("Talkster", "Theme changed" + ThemeManager.getCurrentTheme());
    }
}