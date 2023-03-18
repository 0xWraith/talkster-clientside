package com.client.talkster;


import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.api.WebSocketPrivateChatSubscriber;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.interfaces.IActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class HomeActivity extends AppCompatActivity implements IActivity
{
    private UserJWT userJWT;
    private StompClient client;
    private ViewPager2 homeViewPager;
    private ArrayList<Fragment> fragments;
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
        homeViewPager = findViewById(R.id.homeViewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);
        userJWT = new Gson().fromJson(getIntent().getStringExtra("userJWT"), UserJWT.class);

        fragments = new ArrayList<>();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);

        fragments.add(new ChatsFragment(client));
        fragments.add(new MapFragment());
        fragments.add(new PeoplesFragment());

        initializeSocketConnection();
        initializeBottomNavigation();

    }

    private void initializeSocketConnection()
    {
//        APIStompWebSocket apiStompWebSocket = new APIStompWebSocket(userJWT);
//        apiStompWebSocket.addTopic("/chatroom/public", new WebSocketPrivateChatSubscriber(fragments.get(0)));

        client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, APIStompWebSocket.TALKSTER_WEBSOCKET_URL);
//        client.topic("/chatroom/public").subscribe(new WebSocketPrivateChatSubscriber(fragments.get(0)));
        client.topic("/chatroom/public").subscribe(new Subscriber<StompMessage>() {
            @Override
            public void onCompleted() {
                Log.d("/app/chatroom/public", "onCompleted");
            }

            @Override
            public void onError(Throwable e) {
                Log.d("/app/chatroom/public", "onError " + e.getMessage());
            }

            @Override
            public void onNext(StompMessage stompMessage) {
                Log.d("Message", stompMessage.getPayload());

                runOnUiThread(() -> {
                    Toast.makeText(HomeActivity.this, stompMessage.getPayload(), Toast.LENGTH_SHORT).show();
                });
            }
        });

        ((ChatsFragment)fragments.get(0)).webSocket = client;

        client.lifecycle().subscribe(new APIStompWebSocket.StompWebSocketLifeCycle<>());
        client.connect();

    }

    private void initializeBottomNavigation()
    {

        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);

        homeViewPager.setAdapter(viewPagerAdapter);
        homeViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

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
}