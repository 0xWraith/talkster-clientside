package com.client.talkster;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.UserChangeLoginDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.enums.ECreateLoginResult;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ChangeEmailActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{
    private UserChangeLoginDTO userChangeLoginDTO;
    private final Pattern loginPattern = Pattern.compile("^[a-zA-Z]\\w{3,30}[a-zA-Z\\d]$");

    private EditText loginEditText;
    private TextView usernameResultText;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeManager.getCurrentThemeStyle());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_login);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        ImageButton button;
        userChangeLoginDTO = new UserChangeLoginDTO();


        button = findViewById(R.id.toolbarBackButton);
        button.setOnClickListener(view -> finish());

        button = findViewById(R.id.toolbarAcceptButton);
        loginEditText = findViewById(R.id.loginEditText);
        usernameResultText = findViewById(R.id.usernameResultText);

        button.setOnClickListener(view -> {

            String username = loginEditText.getText().toString();

            if(!checkUsernameRegex(loginEditText.getText().toString()) || !userChangeLoginDTO.getResult() || !Objects.equals(username, userChangeLoginDTO.getLogin()))
                return;

            if(Objects.equals(username, UserAccount.getInstance().getUser().getUsername()))
            {
                setResult(RESULT_OK);
                finish();
                return;
            }
            APIHandler<UserChangeLoginDTO, ChangeEmailActivity> apiHandler = new APIHandler<>(ChangeEmailActivity.this);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_USER_UPDATE_USERNAME, userChangeLoginDTO,  UserAccount.getInstance().getUserJWT().getAccessToken());
        });

        loginEditText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void afterTextChanged(Editable editable)
            {
                String username = editable.toString();

                if(!loginPattern.matcher(username).find())
                {
                    setLoginCreationResult(ECreateLoginResult.INVALID);
                    return;
                }
                User user = UserAccount.getInstance().getUser();
                UserJWT userJWT = UserAccount.getInstance().getUserJWT();
                APIHandler<UserChangeLoginDTO, ChangeEmailActivity> apiHandler = new APIHandler<>(ChangeEmailActivity.this);

                userChangeLoginDTO.setLogin(username);
                userChangeLoginDTO.setId(user.getId());

                setLoginCreationResult(ECreateLoginResult.CHECKING);
                apiHandler.apiPOST(APIEndpoints.TALKSTER_API_USER_CHECK_USERNAME, userChangeLoginDTO, userJWT.getAccessToken());
            }
        });
    }

    private boolean checkUsernameRegex(String username)
    {
        if(!loginPattern.matcher(username).find())
        {
            setLoginCreationResult(ECreateLoginResult.INVALID);
            return false;
        }
        return true;
    }

    private void setLoginCreationResult(ECreateLoginResult result)
    {
        switch(result)
        {
            case INVALID:
            {
                changeUsernameResultText(R.string.username_invalid, ThemeManager.getColor("errorColor"));
                break;
            }
            case TAKEN:
            {
                changeUsernameResultText(R.string.username_taken, ThemeManager.getColor("errorColor"));
                break;
            }
            case CHECKING:
            {
                changeUsernameResultText(R.string.username_checking, ThemeManager.getColor("settings_subText"));
                break;
            }
            case AVAILABLE:
            {
                usernameResultText.setTextColor(ThemeManager.getColor("settings_actionTextColor"));
                usernameResultText.setText(String.format(Locale.getDefault(), getString(R.string.username_available), loginEditText.getText().toString()));
                break;
            }
        }

    }

    private void changeUsernameResultText(int stringResId, int color)
    {
        usernameResultText.setTextColor(color);
        usernameResultText.setText(stringResId);
    }

    @Override
    public void getBundleElements() { }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            int responseCode = response.code();
            ResponseBody responseBody = response.body();

            if(Objects.equals(APIEndpoints.TALKSTER_API_USER_CHECK_USERNAME, apiUrl))
            {

                if (responseBody == null)
                    throw new IOException("Unexpected response " + response);

                if (responseCode == 401)
                    throw new UserUnauthorizedException("Unauthorized");

                ECreateLoginResult result;
                String responseBodyString = responseBody.string();
                userChangeLoginDTO = new Gson().fromJson(responseBodyString, UserChangeLoginDTO.class);

                if (Objects.equals(userChangeLoginDTO.getLogin(), loginEditText.getText().toString()))
                    result = userChangeLoginDTO.getResult() ? ECreateLoginResult.AVAILABLE : ECreateLoginResult.TAKEN;
                else
                    result = ECreateLoginResult.CHECKING;

                runOnUiThread(() -> setLoginCreationResult(result));
            }
            else if(Objects.equals(APIEndpoints.TALKSTER_API_USER_UPDATE_USERNAME, apiUrl))
            {
                if (responseCode == 401)
                    throw new UserUnauthorizedException("Unauthorized");

                if(responseCode == 409)
                {
                    runOnUiThread(() -> setLoginCreationResult(ECreateLoginResult.TAKEN));
                    return;
                }

                if (responseBody == null)
                    throw new IOException("Unexpected response " + response);

                String responseBodyString = responseBody.string();
                userChangeLoginDTO = new Gson().fromJson(responseBodyString, UserChangeLoginDTO.class);

                UserAccount.getInstance().getUser().setUsername(userChangeLoginDTO.getLogin());

                setResult(RESULT_OK);
                finish();
            }

        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }
}