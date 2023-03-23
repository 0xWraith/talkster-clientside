package com.client.talkster.controllers.authorization;

import okhttp3.Call;
import android.util.Log;
import okhttp3.Response;
import android.os.Bundle;
import android.os.Vibrator;
import java.io.IOException;
import com.google.gson.Gson;
import com.client.talkster.R;
import android.widget.Button;
import android.content.Intent;
import java.util.regex.Pattern;
import android.widget.EditText;
import android.content.Context;
import android.os.VibrationEffect;
import androidx.annotation.NonNull;
import com.client.talkster.api.APIHandler;
import com.google.gson.JsonSyntaxException;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.api.APIEndpoints;
import android.view.animation.AnimationUtils;
import com.client.talkster.interfaces.IActivity;
import androidx.appcompat.app.AppCompatActivity;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.interfaces.IAPIResponseHandler;

public class InputMailActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    private Vibrator vibrator;
    private EditText emailAddressInput;
    private AuthenticationDTO authenticationDTO;
    private final Pattern mailPattern = Pattern.compile("(?:[a-z\\d!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z\\d!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z\\d](?:[a-z\\d-]*[a-z\\d])?\\.)+[a-z\\d](?:[a-z\\d-]*[a-z\\d])?|\\[(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)\\.){3}(?:(2(5[0-5]|[0-4]\\d)|1\\d\\d|[1-9]?\\d)|[a-z\\d-]*[a-z\\d]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");

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
        Button continueButton;

        continueButton = findViewById(R.id.continueButton);
        emailAddressInput = findViewById(R.id.emailAddressInput);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        continueButton.setOnClickListener(view -> {

            String mail = emailAddressInput.getText().toString();

            if(authenticationDTO != null)
                return;

            if(!mailPattern.matcher(mail).find())
            {
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
                emailAddressInput.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_input_error));
                return;
            }

            authenticationDTO = new AuthenticationDTO(mail);
            APIHandler<AuthenticationDTO, InputMailActivity> apiHandler = new APIHandler<>(this);

            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_FIND_USER, authenticationDTO, "");
        });
    }

    @Override
    public void getBundleElements() { }

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

            String responseBody = response.body().string();
            UserJWT userJWT = new Gson().fromJson(responseBody, UserJWT.class);
            Intent mailConfirmationIntent = new Intent(this, MailConfirmationActivity.class);

            mailConfirmationIntent.putExtra(BundleExtraNames.USER_JWT, userJWT);
            mailConfirmationIntent.putExtra(BundleExtraNames.USER_MAIL, authenticationDTO.getMail());

            runOnUiThread(() -> {
                startActivity(mailConfirmationIntent);
                finish();
            });
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse JWT token: " + exception.getMessage()); }
    }
}