package com.client.talkster;

import android.os.Bundle;

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

public class HomeActivity extends AppCompatActivity implements IActivity
{
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

        initializeBottomNavigation();
    }

    private void initializeBottomNavigation()
    {
        fragments = new ArrayList<>();
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);

        fragments.add(new ChatsFragment());
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
}