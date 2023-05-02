package com.client.talkster.activities.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.EmptyDTO;
import com.client.talkster.dto.UserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.services.LocationService;
import com.client.talkster.utils.BundleExtraNames;

import org.modelmapper.ModelMapper;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class PrivacySettingsActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeManager.getCurrentThemeStyle());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_privacy_settings);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        SwitchCompat mapTrackerSwitch;
        ImageButton toolbarBackButton;
        User user = UserAccount.getInstance().getUser();


        mapTrackerSwitch = findViewById(R.id.mapTrackerSwitch);
        toolbarBackButton = findViewById(R.id.toolbarBackButton);


        mapTrackerSwitch.setChecked(user.getMapTracker());
        toolbarBackButton.setOnClickListener(v -> finish());

        mapTrackerSwitch.setOnCheckedChangeListener((buttonView, isChecked) ->
        {
            Intent intent = new Intent(this, LocationService.class);

            user.setMapTracker(isChecked);

            if(isChecked)
                intent.setAction(BundleExtraNames.LOCATION_SERVICE_START);
            else
                intent.setAction(BundleExtraNames.LOCATION_SERVICE_STOP);

            APIHandler<EmptyDTO, PrivacySettingsActivity> apiHandler = new APIHandler<>(this);
            apiHandler.apiPUT(String.format("%s/%b", APIEndpoints.TALKSTER_API_USER_UPDATE_MAP_TRACKER, isChecked), new EmptyDTO(), UserAccount.getInstance().getUserJWT().getAccessToken());

            startService(intent);
        });
    }

    @Override
    public void getBundleElements()
    {

    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {

    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {

    }
}