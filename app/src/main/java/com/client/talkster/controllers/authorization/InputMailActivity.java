package com.client.talkster.controllers.authorization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.client.talkster.R;
import com.client.talkster.interfaces.IActivity;

public class InputMailActivity extends AppCompatActivity implements IActivity
{

    private Button continueButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input_mail);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        continueButton = findViewById(R.id.continueButton);

        continueButton.setOnClickListener(view -> {
            startActivity(new Intent(InputMailActivity.this, MailConfirmationActivity.class));
            finish();
        });
    }
}