package com.client.talkster;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.api.WebSocketPrivateChatSubscriber;
import com.client.talkster.api.WebSocketPublicChatSubscriber;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IChatMessagesListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;
import rx.Subscriber;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class HomeActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IChatMessagesListener
{
    private UserJWT userJWT;
    private MapFragment mapFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;
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
}