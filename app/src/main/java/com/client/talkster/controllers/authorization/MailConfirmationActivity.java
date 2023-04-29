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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.client.talkster.HomeActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.dto.VerifiedUserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.UserAccountManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import io.github.muddz.styleabletoast.StyleableToast;
import okhttp3.Call;
import okhttp3.Response;

public class MailConfirmationActivity extends AppCompatActivity implements IActivity, TextWatcher, IAPIResponseHandler
{

    private UserJWT userJWT;
    private Vibrator vibrator;
    private TextView mailInfoText;
    private Button resendButton, clearButton;
    private String secretCode = "";
    private String mail;
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
            authenticationDTO = new AuthenticationDTO(mail);
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
            editText.setText("");
            editText.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input_mail_code_error));
            editText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_code_input_error));
        }
        secretCode = "";
        currentInputField = 0;
        runOnUiThread(() -> codeInput[currentInputField].requestFocus());
    }

    public void clearInput() {
        for(EditText editText : codeInput)
        {
            editText.setText("");
            editText.setBackground(ContextCompat.getDrawable(this, R.drawable.drawable_input_mail_code));
            editText.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_code_input_error));
        }
        secretCode = "";
        currentInputField = 0;
        runOnUiThread(() -> codeInput[currentInputField].requestFocus());
    }

    @Override
    public void getUIElements()
    {
        mailInfoText = findViewById(R.id.mailInfoText);

        resendButton = findViewById(R.id.resendButton);
        clearButton = findViewById(R.id.clearButton);

        codeInput[0] = findViewById(R.id.codeInputFirst);
        codeInput[1] = findViewById(R.id.codeInputSecond);
        codeInput[2] = findViewById(R.id.codeInputThird);
        codeInput[3] = findViewById(R.id.codeInputFourth);
        codeInput[4] = findViewById(R.id.codeInputFifth);

        mailInfoText.setText(String.format(getString(R.string.mail_info), mail));
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        for (EditText editText : codeInput) {
            editText.addTextChangedListener(this);
            editText.setOnFocusChangeListener((view, hasFocus) -> {
                if (hasFocus) {
                    codeInput[currentInputField].requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(codeInput[currentInputField], InputMethodManager.SHOW_IMPLICIT);
                }
            });
        }

        clearButton.setOnClickListener(view -> {
            clearInput();
        });

        resendButton.setOnClickListener(view -> {
            if(authenticationDTO != null)
                return;
            authenticationDTO = new AuthenticationDTO(mail);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_FIND_USER, authenticationDTO, "");
            StyleableToast.makeText(this, "New login code sent!", R.style.customToast).show();
        });
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        userJWT = bundle.getParcelable(BundleExtraNames.USER_JWT);
        mail = bundle.getString(BundleExtraNames.USER_MAIL);
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

        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
    }

    @Override
    public void afterTextChanged(Editable editable)
    {

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);

            int responseCode = response.code();
            String responseBody = response.body().string();

            if (apiUrl.contains(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER)) {
                Intent intent;
                UserJWT userJWT;
                UserAccount userAccount = UserAccount.getInstance();

                switch (responseCode)
                {
                    case 401:
                        setCodeInputToIncorrectState();
                        authenticationDTO = null;
                        return;

                    case 200:
                        intent = new Intent(this, HomeActivity.class);
                        VerifiedUserDTO verifiedUserDTO = new Gson().fromJson(responseBody, VerifiedUserDTO.class);

                        userAccount.setUser(verifiedUserDTO.getUser());
                        userAccount.setUserJWT(verifiedUserDTO.getUserJWT());

                        UserAccountManager.saveAccount(this, verifiedUserDTO.getUserJWT());
                        break;

                    case 202:
                        intent = new Intent(this, RegistrationActivity.class);
                        userJWT = new Gson().fromJson(responseBody, UserJWT.class);

                        userAccount.setUserJWT(userJWT);

                        intent.putExtra(BundleExtraNames.USER_MAIL, authenticationDTO.getMail());
                        break;

                    default:
                        throw new IOException("Unexpected response " + response);
                }

                runOnUiThread(() -> {
                    startActivity(intent);
                    finish();
                });
            }
            else if (apiUrl.contains(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_FIND_USER)) {
                userJWT = new Gson().fromJson(responseBody, UserJWT.class);
                authenticationDTO = null;
            }
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }
}