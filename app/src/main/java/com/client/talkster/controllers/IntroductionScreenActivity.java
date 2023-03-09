package com.client.talkster.controllers;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.R;
import com.client.talkster.controllers.authorization.InputMailActivity;
import com.client.talkster.interfaces.IActivity;

public class IntroductionScreenActivity extends AppCompatActivity implements IActivity
{

    private Button startMessagingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_introduction_screen);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        startMessagingButton = findViewById(R.id.startMessagingButton);

        startMessagingButton.setOnClickListener(view -> {
            startActivity(new Intent(IntroductionScreenActivity.this, InputMailActivity.class));
            finish();
        });
    }
}