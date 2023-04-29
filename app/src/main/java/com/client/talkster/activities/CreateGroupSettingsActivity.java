package com.client.talkster.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.adapters.CreateGroupAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.websocket.APIStompWebSocket;
import com.client.talkster.classes.chat.GroupChat;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.CreateGroupDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

public class CreateGroupSettingsActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    private List<User> selectedContacts;
    private List<Long> selectedContactsIDs;

    private EditText groupNameInput;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        setTheme(ThemeManager.getCurrentThemeStyle());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group_settings);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        TextView headerText2;
        RecyclerView contactsRecyclerView;
        CreateGroupAdapter createGroupAdapter;
        ImageButton toolbarBackIcon, continueButton;

        headerText2 = findViewById(R.id.headerText2);
        groupNameInput = findViewById(R.id.groupNameInput);
        continueButton = findViewById(R.id.continueButton);
        toolbarBackIcon = findViewById(R.id.toolbarBackIcon);
        contactsRecyclerView = findViewById(R.id.contactsRecyclerView);

        createGroupAdapter = new CreateGroupAdapter(this, selectedContacts, new IRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClick(int position, View v) { }

            @Override
            public void onItemLongClick(int position, View v) { }
        });

        continueButton.setOnClickListener(v ->
        {
            String groupName = groupNameInput.getText().toString();

            selectedContactsIDs = new ArrayList<>();
            UserAccount userAccount = UserAccount.getInstance();

            selectedContacts.add(userAccount.getUser());

            for(User user : selectedContacts) {
                Log.d("CreateGroupSettings", "onItemClick: " + user);
                selectedContactsIDs.add(user.getId());
            }
            APIHandler<CreateGroupDTO, CreateGroupSettingsActivity> apiHandler = new APIHandler<>(this);
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_GROUP_CREATE, new CreateGroupDTO(-1, groupName, selectedContactsIDs), userAccount.getUserJWT().getAccessToken());

        });

        toolbarBackIcon.setOnClickListener(v -> finish());
        contactsRecyclerView.setAdapter(createGroupAdapter);
        headerText2.setText(String.format(Locale.getDefault(), getString(R.string.group_members_count), selectedContacts.size()));

        createGroupAdapter.notifyDataSetChanged();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle == null)
            return;

        selectedContacts = (List<User>) bundle.get(BundleExtraNames.CREATE_GROUP_MEMBER_LIST);
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);


            int responseCode = response.code();

            if(responseCode == 401)
                throw new UserUnauthorizedException("User is unauthorized");

            String responseBody = response.body().string();

            if(apiUrl.equals(APIEndpoints.TALKSTER_API_GROUP_CREATE))
            {
                APIStompWebSocket apiStompWebSocket = APIStompWebSocket.getInstance();
                GroupChat groupChat = new Gson().fromJson(responseBody, GroupChat.class);

                apiStompWebSocket.getWebSocketClient().send("/app/group/created", new Gson().toJson(new CreateGroupDTO(groupChat.getId(), "", selectedContactsIDs))).subscribe();

                setResult(RESULT_OK);
                finish();
            }
        }
        catch (IOException | UserUnauthorizedException exception) { }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) { }
}