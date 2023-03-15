package com.client.talkster;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.interfaces.IActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import okio.ByteString;

public class HomeActivity extends AppCompatActivity implements IActivity
{
    private WebSocket webSocket;
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

        initializeSocketConnection();
        initializeBottomNavigation();
    }

    private void initializeSocketConnection()
    {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url("ws://147.175.160.77:8000/websocket").build();
        webSocket = client.newWebSocket(request, new ChatWebSocketListener());
    }

    private void initializeBottomNavigation()
    {
        fragments = new ArrayList<>();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);

        fragments.add(new ChatsFragment(webSocket));
        fragments.add(new MapFragment());
        fragments.add(new PeoplesFragment());

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

    private static class ChatWebSocketListener extends WebSocketListener
    {
        @Override
        public void onOpen(WebSocket webSocket, Response response)
        {
            Log.d("ERROR", "onOpen");
            //webSocket.send("{\"command\":\"subscribe\",\"destination\":\"/user/queue/messages\"}");
        }

        @Override
        public void onMessage(WebSocket webSocket, String text)
        {
            Log.d("ERROR", "onMessage");
        }

        @Override
        public void onMessage(WebSocket webSocket, ByteString bytes)
        {
            Log.d("onMessage", bytes.toString());
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason)
        {
            Log.d("ERROR", "onClosing");
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason)
        {
            Log.d("ERROR", "onClosed");
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, @Nullable Response response)
        {
            Log.d("ERROR", "onFailure " + webSocket + " " + t + " " + response);
        }
    }
}