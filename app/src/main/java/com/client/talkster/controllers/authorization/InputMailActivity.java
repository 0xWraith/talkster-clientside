package com.client.talkster.controllers.authorization;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.interfaces.IActivity;

import java.util.regex.Pattern;

public class InputMailActivity extends AppCompatActivity implements IActivity
{

    private Button continueButton;
    private EditText emailAddressInput;

    private Vibrator vibrator;
    private final Pattern mailPattern = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])");
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
        emailAddressInput = findViewById(R.id.emailAddressInput);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        continueButton.setOnClickListener(view -> {

            String mail = emailAddressInput.getText().toString();

            if(!mailPattern.matcher(mail).find())
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_TICK));

                emailAddressInput.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_input_error));

                return;
            }

            AuthenticationDTO authenticationDTO = new AuthenticationDTO();
            APIHandler<AuthenticationDTO, InputMailActivity> apiHandler = new APIHandler<>(this);

            authenticationDTO.setMail(mail);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_FIND_USER, authenticationDTO, "", this);
        });
    }
}