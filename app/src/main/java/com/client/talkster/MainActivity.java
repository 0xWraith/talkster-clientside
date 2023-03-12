package com.client.talkster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.client.talkster.controllers.IntroductionScreenActivity;
import com.client.talkster.utils.UserAccountManager;

public class MainActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserAccountManager.getAccount(this);
    }
}