package com.client.talkster.controllers.authorization;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.interfaces.IActivity;
import com.google.gson.Gson;

import java.util.Objects;

public class MailConfirmationActivity extends AppCompatActivity implements IActivity, View.OnClickListener, TextWatcher
{

    private UserJWT userJWT;
    private Vibrator vibrator;
    private String secretCode = "";
    private int currentInputField = 0;
    private AuthenticationDTO authenticationDTO;
    private final APIHandler<AuthenticationDTO, MailConfirmationActivity> apiHandler = new APIHandler<>(this);
    private EditText[] codeInput = new EditText[5];
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_confirmation);
        getUIElements();
    }

    private void moveInputCodePointer(int direction)
    {
        if(direction == -1 && currentInputField == 0)
            return;

        if(direction == 1 && currentInputField == codeInput.length - 1)
        {
            authenticationDTO.setCode(secretCode);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER, authenticationDTO, userJWT.getJWTToken(), this);
            return;
        }

        currentInputField += direction;
        codeInput[currentInputField].requestFocus();
    }


    public void setCodeInputToIncorrectState()
    {
        for(EditText editText : codeInput)
        {
            editText.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input_mail_code_error));
            editText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_code_input_error));
        }
    }


    @Override
    public void getUIElements()
    {
        authenticationDTO = new AuthenticationDTO();

        codeInput[0] = findViewById(R.id.codeInputFirst);
        codeInput[1] = findViewById(R.id.codeInputSecond);
        codeInput[2] = findViewById(R.id.codeInputThird);
        codeInput[3] = findViewById(R.id.codeInputFourth);
        codeInput[4] = findViewById(R.id.codeInputFifth);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        authenticationDTO.setMail(getIntent().getStringExtra("userMail"));
        userJWT = new Gson().fromJson(getIntent().getStringExtra("userJWT"), UserJWT.class);

        for (EditText editText : codeInput)
        {
            editText.setOnClickListener(this);
            editText.addTextChangedListener(this);
        }
    }

    @Override
    public void onClick(View view)
    {

    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int before, int count)
    {

    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void onTextChanged(CharSequence charSequence, int start, int before, int count)
    {
        if(count == 1)
        {
            secretCode += charSequence;
            codeInput[currentInputField].setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input_mail_code_entered));
            codeInput[currentInputField].startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_code_input_up));
            moveInputCodePointer(1);
        }
        else
        {
            secretCode = new StringBuilder(secretCode).deleteCharAt(currentInputField).toString();
            codeInput[currentInputField].setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input_mail_code));
            moveInputCodePointer(-1);
            codeInput[currentInputField].startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_code_input_down));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_TICK));
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}