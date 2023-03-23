package com.client.talkster.controllers;

import android.os.Bundle;
import com.client.talkster.R;
import android.widget.Button;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.controllers.authorization.InputMailActivity;

public class IntroductionScreenActivity extends AppCompatActivity implements IActivity
{
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
        Button startMessagingButton = findViewById(R.id.startMessagingButton);

        startMessagingButton.setOnClickListener(view ->
        {
            startActivity(new Intent(IntroductionScreenActivity.this, InputMailActivity.class));
            finish();
        });
    }

    @Override
    public void getBundleElements() { }
}