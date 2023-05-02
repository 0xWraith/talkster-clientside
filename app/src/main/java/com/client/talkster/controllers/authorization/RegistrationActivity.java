package com.client.talkster.controllers.authorization;

import static com.client.talkster.api.APIEndpoints.TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.HomeActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.dto.RegistrationDTO;
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

public class RegistrationActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    private Vibrator vibrator;
    private ImageButton continueButton;
    private EditText lastNameInput;
    private EditText firstNameInput;
    private AuthenticationDTO authenticationDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {

        lastNameInput = findViewById(R.id.lastNameInput);
        firstNameInput = findViewById(R.id.firstNameInput);
        continueButton = findViewById(R.id.continueButton);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

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

            UserJWT userJWT = UserAccount.getInstance().getUserJWT();
            apiHandler.apiPOST(TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER, registrationDTO, userJWT.getAccessToken());

        });
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        authenticationDTO = new AuthenticationDTO(bundle.get(BundleExtraNames.USER_MAIL).toString());
    }

    private void invalidFieldInput(EditText field)
    {
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
        field.startAnimation(AnimationUtils.loadAnimation(this, R.anim.animation_mail_input_error));
        runOnUiThread(() -> StyleableToast.makeText(this, "Invalid input!", R.style.customToast).show());
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

            if(responseCode != 200)
                throw new IOException("Unexpected response " + response);

            VerifiedUserDTO verifiedUserDTO = new Gson().fromJson(responseBody, VerifiedUserDTO.class);

            UserAccount userAccount = UserAccount.getInstance();
            userAccount.setUser(verifiedUserDTO.getUser());
            userAccount.setUserJWT(verifiedUserDTO.getUserJWT());

            Intent homeIntent = new Intent(this, HomeActivity.class);

            UserAccountManager.saveAccount(this, verifiedUserDTO.getUserJWT());

            //homeIntent.putExtra(BundleExtraNames.USER_JWT, userJWT);

            startActivity(homeIntent);
            finish();
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse JWT token: " + exception.getMessage()); }
    }
}