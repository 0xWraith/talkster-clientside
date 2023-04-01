package com.client.talkster.controllers.authorization;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.client.talkster.HomeActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.dto.VerifiedUserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.UserAccountManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class MailConfirmationActivity extends AppCompatActivity implements IActivity, TextWatcher, IAPIResponseHandler
{

    private UserJWT userJWT;
    private Vibrator vibrator;
    private TextView mailInfoText;
    private String secretCode = "";
    private int currentInputField = 0;
    private AuthenticationDTO authenticationDTO;
    private final EditText[] codeInput = new EditText[5];
    private final APIHandler<AuthenticationDTO, MailConfirmationActivity> apiHandler = new APIHandler<>(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mail_confirmation);

        getBundleElements();
        getUIElements();
    }

    private void moveInputCodePointer(int direction)
    {
        if(direction == -1 && currentInputField == 0)
            return;

        if(direction == 1 && currentInputField == codeInput.length - 1)
        {
            authenticationDTO.setCode(secretCode);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER, authenticationDTO, userJWT.getAccessToken());
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
        mailInfoText = findViewById(R.id.mailInfoText);

        codeInput[0] = findViewById(R.id.codeInputFirst);
        codeInput[1] = findViewById(R.id.codeInputSecond);
        codeInput[2] = findViewById(R.id.codeInputThird);
        codeInput[3] = findViewById(R.id.codeInputFourth);
        codeInput[4] = findViewById(R.id.codeInputFifth);

        mailInfoText.setText(String.format(getString(R.string.mail_info), authenticationDTO.getMail()));
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        for (EditText editText : codeInput)
            editText.addTextChangedListener(this);
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        authenticationDTO = new AuthenticationDTO();
        userJWT = bundle.getParcelable(BundleExtraNames.USER_JWT);
        authenticationDTO.setMail(bundle.getString(BundleExtraNames.USER_MAIL));
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
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    public void afterTextChanged(Editable editable)
    {

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {

    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);

            Intent intent;
            int responseCode = response.code();
            String responseBody = response.body().string();

            UserJWT userJWT;

            switch (responseCode)
            {
                case 401:
                    setCodeInputToIncorrectState();
                    return;

                case 200:
                    intent = new Intent(this, HomeActivity.class);
                    VerifiedUserDTO verifiedUserDTO = new Gson().fromJson(responseBody, VerifiedUserDTO.class);

                    User user = verifiedUserDTO.getUser();
                    userJWT = verifiedUserDTO.getUserJWT();

                    intent.putExtra(BundleExtraNames.USER, user);
                    UserAccountManager.saveAccount(this, userJWT);
                    break;

                case 202:
                    intent = new Intent(this, RegistrationActivity.class);
                    userJWT = new Gson().fromJson(responseBody, UserJWT.class);
                    intent.putExtra(BundleExtraNames.USER_MAIL, authenticationDTO.getMail());
                    break;

                default:
                    throw new IOException("Unexpected response " + response);
            }
            intent.putExtra(BundleExtraNames.USER_JWT, userJWT);

            runOnUiThread(() -> {
                startActivity(intent);
                finish();
            });
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }
}