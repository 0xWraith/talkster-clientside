package com.client.talkster.controllers.authorization;

import static com.client.talkster.api.APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;

import com.client.talkster.R;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.dto.RegistrationDTO;
import com.client.talkster.interfaces.IActivity;
import com.google.gson.Gson;

public class RegistrationActivity extends AppCompatActivity implements IActivity
{

    private UserJWT userJWT;
    private Vibrator vibrator;
    private Button continueButton;
    private EditText lastNameInput;
    private EditText firstNameInput;
    private AuthenticationDTO authenticationDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        authenticationDTO = new AuthenticationDTO();

        lastNameInput = findViewById(R.id.lastNameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        continueButton = findViewById(R.id.continueButton);

        authenticationDTO.setMail(getIntent().getStringExtra("userMail"));
        userJWT = new Gson().fromJson(getIntent().getStringExtra("userJWT"), UserJWT.class);

        continueButton.setOnClickListener(view ->
        {
            String firstName = firstNameInput.getText().toString();
            String lastName = lastNameInput.getText().toString();

            if(firstName.length() == 0 || firstName.length() > 16)
            {
                invalidFieldInput(firstNameInput);
                return;
            }

            if(lastName.length() > 16)
            {
                invalidFieldInput(lastNameInput);
                return;
            }

            RegistrationDTO registrationDTO = new RegistrationDTO();
            APIHandler<RegistrationDTO, RegistrationActivity> apiHandler = new APIHandler<>(this);

            registrationDTO.setLastname(lastName);
            registrationDTO.setFirstname(firstName);
            registrationDTO.setMail(authenticationDTO.getMail());

            apiHandler.apiPOST(TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER, registrationDTO, userJWT.getJWTToken(), this);

        });
    }

    private void invalidFieldInput(EditText field)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_TICK));

        field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_input_error));
    }
}