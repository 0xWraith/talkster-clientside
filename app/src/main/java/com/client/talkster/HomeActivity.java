package com.client.talkster;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IChatMessagesListener;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class HomeActivity extends AppCompatActivity implements IActivity
{
    private UserJWT userJWT;
    private StompClient client;


    private MapFragment mapFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private ChatsFragment chatsFragment;
    private ArrayList<Fragment> fragments;
    private PeoplesFragment peoplesFragment;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        userJWT = new Gson().fromJson(getIntent().getStringExtra("userJWT"), UserJWT.class);

        mapFragment = new MapFragment(userJWT);
        chatsFragment = new ChatsFragment(userJWT);
        peoplesFragment = new PeoplesFragment();

        homeViewPager = findViewById(R.id.homeViewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        fragments = new ArrayList<>();

        fragments.add(chatsFragment);
        fragments.add(mapFragment);
        fragments.add(peoplesFragment);

        if(chatsFragment != null)
            iChatListener = (IChatListener) chatsFragment;

        initializeBottomNavigation();
        initializeSocketConnection();
    }

    private void initializeSocketConnection()
    {
//        Subscriber<StompMessage> webSocketPrivateChatSubscriber = new WebSocketPrivateChatSubscriber().setSubscriberFragment(fragments.get(0));

        /*APIStompWebSocket apiStompWebSocket = new APIStompWebSocket(userJWT);
        apiStompWebSocket.addTopic("/chatroom/public", new Subscriber<StompMessage>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(StompMessage stompMessage) {
                Toast.makeText(HomeActivity.this, stompMessage.getPayload(), Toast.LENGTH_SHORT).show();
            }
        });*/
//        apiStompWebSocket.addTopic("/user/"+ userJWT.getID() +"/private", webSocketPrivateChatSubscriber);
//        apiStompWebSocket.connect();

//        ((ChatsFragment)fragments.get(0)).webSocket = apiStompWebSocket.getWebSocketClient();

        client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, APIStompWebSocket.TALKSTER_WEBSOCKET_URL);

        mapFragment.webSocket = client;

        client.topic("/chatroom/public").subscribe(new Subscriber<StompMessage>() {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {

            }

            @Override
            public void onNext(StompMessage stompMessage) {
                Log.d("msg", stompMessage.getPayload());
                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, "" + stompMessage.getPayload(), Toast.LENGTH_SHORT).show();
                    iChatListener.onMessageReceived("funguje");
                });
            }
        });

        client.topic("/user/"+ userJWT.getID() +"/private").subscribe(new Subscriber<StompMessage>() {
            @Override
            public void onCompleted()
            {

            }

            @Override
            public void onError(Throwable e)
            {

            }

            @Override
            public void onNext(StompMessage stompMessage) { runOnUiThread(() -> iChatListener.onMessageReceived(stompMessage.getPayload())); }
        });

        client.lifecycle().subscribe(event -> {
            switch (event.getType()) {

                case OPENED:
                    Log.d("OPENED", "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e("ERROR", "Error", event.getException());
                    break;

                case CLOSED:
                    Log.d("CLOSED", "Stomp connection closed");
                    break;
            }
        });
        client.connect();
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

    public void updateChatList(List<Chat> chatList) { iChatListener.updateChatList(chatList); }

    public void addNewChat(Chat chat) { iChatListener.addChat(chat); }
}