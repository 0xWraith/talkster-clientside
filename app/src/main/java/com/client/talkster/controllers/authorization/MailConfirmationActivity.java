package com.client.talkster.controllers.authorization;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;

import com.client.talkster.R;
import com.client.talkster.interfaces.IActivity;

import java.util.Objects;

public class MailConfirmationActivity extends AppCompatActivity implements IActivity, View.OnClickListener, TextWatcher {

    private String secretCode = "";
    private int currentInputField = 0;
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
//            Test
            if(Objects.equals(secretCode, "00000"))
            {
                setCodeInputToIncorrectState();
                return;
            }
            return;
        }

        currentInputField += direction;

        codeInput[currentInputField].requestFocus();
    }


    private void setCodeInputToIncorrectState()
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
        codeInput[0] = findViewById(R.id.codeInputFirst);
        codeInput[1] = findViewById(R.id.codeInputSecond);
        codeInput[2] = findViewById(R.id.codeInputThird);
        codeInput[3] = findViewById(R.id.codeInputFourth);
        codeInput[4] = findViewById(R.id.codeInputFifth);

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
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }
}