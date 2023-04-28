package com.client.talkster;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.UserChangeLoginDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ChangeBiographyActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    private EditText biographyTextInput;
    private UserChangeLoginDTO userChangeLoginDTO;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeManager.getCurrentThemeStyle());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_biography);

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
        biographyTextInput = findViewById(R.id.biographyTextInput);

        button.setOnClickListener(view -> {

            String biography = biographyTextInput.getText().toString();


            if(Objects.equals(biography, UserAccount.getInstance().getUser().getBiography()))
            {
                finish();
                return;
            }
            userChangeLoginDTO.setLogin(biography);

            APIHandler<UserChangeLoginDTO, ChangeBiographyActivity> apiHandler = new APIHandler<>(ChangeBiographyActivity.this);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_USER_UPDATE_BIOGRAPHY, userChangeLoginDTO,  UserAccount.getInstance().getUserJWT().getAccessToken());
        });
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

            if (responseCode == 401)
                throw new UserUnauthorizedException("Unauthorized");

            if (responseBody == null)
                throw new IOException("Unexpected response " + response);

            String responseBodyString = responseBody.string();
            userChangeLoginDTO = new Gson().fromJson(responseBodyString, UserChangeLoginDTO.class);

            UserAccount.getInstance().getUser().setBiography(userChangeLoginDTO.getLogin());

            setResult(RESULT_OK);
            finish();
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
        finally { response.close(); }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) { }
}