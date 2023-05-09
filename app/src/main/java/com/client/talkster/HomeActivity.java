package com.client.talkster;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.websocket.APIStompWebSocket;
import com.client.talkster.api.websocket.listeners.WebSocketGroupChatCreatedSubscriber;
import com.client.talkster.api.websocket.listeners.WebSocketGroupChatSubscriber;
import com.client.talkster.api.websocket.listeners.WebSocketPrivateChatSubscriber;
import com.client.talkster.api.websocket.listeners.WebSocketMapSubscriber;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.chat.Chat;
import com.client.talkster.classes.chat.GroupChat;
import com.client.talkster.classes.chat.PrivateChat;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.dto.EmptyDTO;
import com.client.talkster.dto.LocationDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.TokenDTO;
import com.client.talkster.dto.UserChatsDTO;
import com.client.talkster.dto.UserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IBroadcastRegister;
import com.client.talkster.interfaces.chat.IChatListener;
import com.client.talkster.interfaces.chat.IChatWebSocketHandler;
import com.client.talkster.interfaces.IMapGPSPositionUpdate;
import com.client.talkster.interfaces.IMapWebSocketHandler;
import com.client.talkster.interfaces.chat.IGroupChatListener;
import com.client.talkster.interfaces.theme.IThemeManagerActivityListener;
import com.client.talkster.services.LocationService;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EChatType;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.github.muddz.styleabletoast.StyleableToast;
import okhttp3.Call;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IChatWebSocketHandler, IMapWebSocketHandler, IBroadcastRegister, IThemeManagerActivityListener, IGroupChatListener
{

    private String FCMToken;
    private IChatListener iChatListener;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;
    private BroadcastReceiver chatBroadCastReceiver;
    private BroadcastReceiver locationBroadCastReceiver;
    private IMapGPSPositionUpdate iMapGPSPositionUpdate;
    private WebSocketGroupChatSubscriber webSocketGroupChatSubscriber;

    private MapFragment mapFragment;
    private ChatsFragment chatsFragment;
    private PeoplesFragment peoplesFragment;
    private ViewPager2 homeViewPager;
    private BottomNavigationView bottomNavigation;


    private ConstraintLayout homeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        loadApplicationTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void loadApplicationTheme()
    {
        ThemeManager.addListener(this);
        setTheme(ThemeManager.getCurrentThemeStyle());
    }

    @Override
    public void removeListener() { ThemeManager.removeListener(this); }

    @Override
    public void getUIElements()
    {

        mapFragment = new MapFragment();
        chatsFragment = new ChatsFragment();
        peoplesFragment = new PeoplesFragment();

        iChatListener = chatsFragment;
        iMapGPSPositionUpdate = mapFragment;

        homeLayout = findViewById(R.id.homeLayout);
        homeViewPager = findViewById(R.id.homeViewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);


        homeViewPager.setUserInputEnabled(false);
        fragments = new ArrayList<>(Arrays.asList(chatsFragment, mapFragment, peoplesFragment));

        registerBroadCasts();
        initializeBottomNavigation();
        initializeSocketConnection();

        User user = UserAccount.getInstance().getUser();
        List<User> contacts = user.getContacts();
        List<Long> contactIDs = user.getContactIDs();

        contacts.forEach(contact ->
        {
            if(!contactIDs.contains(contact.getId()))
                contactIDs.add(contact.getId());
        });

        if(user.getMapTracker())
        {
            Intent intent = new Intent(this, LocationService.class);
            intent.setAction(BundleExtraNames.LOCATION_SERVICE_START);
            startService(intent);
        }
    }

    private void initializeSocketConnection()
    {

        long userId = UserAccount.getInstance().getUser().getId();
        apiStompWebSocket = APIStompWebSocket.getInstance();

        webSocketGroupChatSubscriber = new WebSocketGroupChatSubscriber(this);
        apiStompWebSocket.addTopic("/user/" + userId + "/private", new WebSocketPrivateChatSubscriber(this));
        apiStompWebSocket.addTopic("/user/" + userId + "/group/created", new WebSocketGroupChatCreatedSubscriber(this));
        apiStompWebSocket.addTopic("/user/" + userId + "/map", new WebSocketMapSubscriber(this));

        apiStompWebSocket.connect();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle == null || bundle.isEmpty())
            return;
        
    }
    @Override
    public void registerBroadCasts()
    {
        chatBroadCastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();
                Bundle bundle = intent.getExtras();

                if(action.equals(BundleExtraNames.CHAT_ACTION_BROADCAST))
                {
                    long chatID;
                    EChatType chatType;
                    MessageDTO messageDTO;
                    EPrivateChatAction actionType;

                    chatType = (EChatType)bundle.get(BundleExtraNames.CHAT_TYPE);
                    chatID = (long)bundle.get(BundleExtraNames.CHAT_ACTION_CHAT_ID);
                    messageDTO = bundle.getParcelable(BundleExtraNames.CHAT_ACTION_MESSAGE_DATA);
                    actionType = (EPrivateChatAction) bundle.get(BundleExtraNames.CHAT_ACTION_TYPE);


                    switch(actionType)
                    {
                        case CLEAR_CHAT_HISTORY:
                        {
                            iChatListener.onChatHistoryCleared(chatID, chatType);
                            break;
                        }

                        case DELETE_CHAT:
                        {
                            iChatListener.onChatDeleted(chatID, chatType);
                            break;
                        }
                        case MUTE_CHAT:
                        {
                            iChatListener.onChatMuted(chatID, messageDTO.getreceiverid(), chatType);
                            return;
                        }
                    }

                    if(messageDTO == null)
                        return;

                    apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
                }
            }
        };

        locationBroadCastReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(!action.equals(BundleExtraNames.LOCATION_SERVICE_BROADCAST))
                    return;

                UserAccount userAccount = UserAccount.getInstance();

                User user = userAccount.getUser();
                UserJWT userJWT = userAccount.getUserJWT();

                Location location = (Location) intent.getExtras().get(BundleExtraNames.LOCATION_SERVICE_POSITION);

                iMapGPSPositionUpdate.onMapGPSPositionUserUpdate(location);

                LocationDTO locationDTO = new LocationDTO(user.getId(), user.getFullName(), userJWT.getAccessToken(), user.getContactIDs(), location);
                apiStompWebSocket.getWebSocketClient().send("/app/map", new Gson().toJson(locationDTO)).subscribe();
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BundleExtraNames.CHAT_ACTION_BROADCAST);

        registerReceiver(chatBroadCastReceiver, intentFilter);
        registerReceiver(locationBroadCastReceiver, new IntentFilter(BundleExtraNames.LOCATION_SERVICE_BROADCAST));
    }

    @Override
    public void unregisterBroadCasts()
    {

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        getToken();
    }

    private void getToken()
    {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task ->
        {
            if (!task.isSuccessful())
                return;

            String token = task.getResult();

            if (Objects.equals(token, FCMToken))
                return;

            FCMToken = token;
            putToken();
        });
    }

    private void putToken()
    {
        TokenDTO tokenDTO = new TokenDTO();
        APIHandler<TokenDTO, HomeActivity> apiHandler = new APIHandler<>(this);
        tokenDTO.setToken(FCMToken);
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_NOTIFICATION_ADD_TOKEN, tokenDTO, UserAccount.getInstance().getUserJWT().getAccessToken());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        removeListener();
        unregisterReceiver(chatBroadCastReceiver);
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

            if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT))
            {
                String responseBody = response.body().string();
                PrivateChat chat = new Gson().fromJson(responseBody, PrivateChat.class);
                runOnUiThread (() -> iChatListener.addChat(chat));
            }
            else if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS_INFO))
            {

                if(responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);


                String responseBody = response.body().string();

                Chat[] chats = new Gson().fromJson(responseBody, Chat[].class);
                List<Chat> chatList = new ArrayList<>(Arrays.asList(chats));

                runOnUiThread (() -> chatsFragment.updateChatListInfo(chatList));
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_CREATE))
            {
                if(responseCode != 200)
                {
                    if(responseCode == 409)
                        runOnUiThread(() -> StyleableToast.makeText(this, "Friend already added!", R.style.friendAlreadyAdded).show());

                    if(responseCode == 404)
                        runOnUiThread(() -> StyleableToast.makeText(this, "Friend not found!", R.style.friendNotFound).show());

                    else
                        throw new UserUnauthorizedException("Unexpected response " + response);
                }
                else
                {
                    String responseBody = response.body().string();
                    PrivateChat chat = new Gson().fromJson(responseBody, PrivateChat.class);
                    runOnUiThread(() -> StyleableToast.makeText(this, "Friend added!", R.style.friendAdded).show());
                    runOnUiThread (() -> iChatListener.addChat(chat));

                    APIHandler<EmptyDTO, HomeActivity> apiHandler = new APIHandler<>(this);
                    apiHandler.apiGET(String.format(Locale.getDefault(), "%s/%d", APIEndpoints.TALKSTER_API_USER_GET_DATA, chat.getReceiverID()), UserAccount.getInstance().getUserJWT().getAccessToken());
                }
            }
            else if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS))
            {
                if(responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);

                String responseBody = response.body().string();

                UserChatsDTO userChatsDTO = new Gson().fromJson(responseBody, UserChatsDTO.class);

                ArrayList<PrivateChat> chatList = userChatsDTO.getPrivateChats();
                ArrayList<GroupChat> groupChatList = userChatsDTO.getGroupChats();

                runOnUiThread (() ->
                {
                    for(GroupChat groupChat : groupChatList)
                        apiStompWebSocket.addTopic(String.format(Locale.getDefault(), "/user/%d/group/message", groupChat.getId()), webSocketGroupChatSubscriber);

                    iChatListener.updateChatList(chatList, groupChatList);
                });
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_CHAT))
            {
                if (responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);

                UserAccount userAccount = UserAccount.getInstance();

                String responseBody = response.body().string();
                PrivateChat chat = new Gson().fromJson(responseBody, PrivateChat.class);

                Intent privateChatIntent = new Intent(getApplicationContext(), PrivateChatActivity.class);
                privateChatIntent.putExtra(BundleExtraNames.USER_CHAT, chat);

                startActivity(privateChatIntent);
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_UPDATE_PROFILE))
            {
                if (responseCode != 200){
                    throw new UserUnauthorizedException("Unexpected response " + response);
                }
                runOnUiThread (() -> peoplesFragment.updateProfilePicture());
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_DELETE_PROFILE))
            {
                if (responseCode != 200){
                    throw new UserUnauthorizedException("Unexpected response " + response);
                }
                runOnUiThread(() -> StyleableToast.makeText(this, "Profile picture deleted!", R.style.pictureRemoved).show());
                runOnUiThread (() -> peoplesFragment.updateProfilePicture());
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_USER_GET_DATA))
            {
                if(responseCode == 401)
                    throw new UserUnauthorizedException("TALKSTER_API_USER_GET_DATA: " + response);

                if(responseCode == 404)
                    throw new UserUnauthorizedException("TALKSTER_API_USER_GET_DATA: " + response);


                ModelMapper modelMapper = new ModelMapper();
                String responseBody = response.body().string();
                UserDTO user = new Gson().fromJson(responseBody, UserDTO.class);

                UserAccount.getInstance().getUser().addContact(modelMapper.map(user, User.class));
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_GROUP_GET))
            {
                if(responseCode == 401)
                    throw new UserUnauthorizedException("TALKSTER_API_USER_GET_DATA: " + response);

                if(responseCode == 404)
                    throw new UserUnauthorizedException("TALKSTER_API_USER_GET_DATA: " + response);

                String responseBody = response.body().string();
                GroupChat groupChat = new Gson().fromJson(responseBody, GroupChat.class);

                runOnUiThread (() -> {
                    iChatListener.addChat(groupChat);
                    apiStompWebSocket.addTopic(String.format(Locale.getDefault(), "/user/%d/group/message", groupChat.getId()), webSocketGroupChatSubscriber);
                });
            }
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster " + apiUrl, "Failed to parse: " + exception.getMessage()); }
    }

    private void initializeBottomNavigation()
    {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        homeViewPager.setAdapter(viewPagerAdapter);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int ID = item.getItemId();

            if (ID == R.id.chatMenuID)
                homeViewPager.setCurrentItem(0);

            else if (ID == R.id.mapMenuID)
                homeViewPager.setCurrentItem(1);

            else if(ID == R.id.peoplesMenuID)
                homeViewPager.setCurrentItem(2);

            return true;
        });
    }

    public void selectNavigationButton(int currentPosition)
    {
        switch (currentPosition)
        {
            case 0:
                bottomNavigation.setSelectedItemId(R.id.chatMenuID);
                break;
            case 1:
                bottomNavigation.setSelectedItemId(R.id.mapMenuID);
                break;
            case 2:
                bottomNavigation.setSelectedItemId(R.id.peoplesMenuID);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {

        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 101)
        {
            Uri uri = data.getData();
            sendProfileImage(uri);
        }
        else if (resultCode == ImagePicker.RESULT_ERROR)
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();

    }

    private void sendProfileImage(Uri uri)
    {
        UserAccount userAccount = UserAccount.getInstance();

        FileContent fileContent = new FileContent();
        APIHandler<FileContent, HomeActivity> apiHandler = new APIHandler<>(this);
        try
        {
            ContentResolver cr = getContentResolver();
            fileContent.setContent(FileUtils.getBytes(uri, cr));
            fileContent.setType(FileUtils.getType(uri, cr));
            fileContent.setFilename(FileUtils.getFilename(uri, cr));
            apiHandler.apiMultipartPUT(APIEndpoints.TALKSTER_API_FILE_UPDATE_PROFILE,fileContent, userAccount.getUserJWT().getAccessToken());

        }
        catch (IOException e)
        {
            System.out.println("This Exception was thrown inside the sendImage method");
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(String messageRAW) { iChatListener.onMessageReceived(messageRAW); }

    @Override
    public void onMapMessageReceived(String locationRAW) { iMapGPSPositionUpdate.onMapGPSPositionUpdate(locationRAW); }

    @Override
    public void onThemeChanged()
    {
        setTheme(ThemeManager.getCurrentThemeStyle());
        ThemeManager.reloadThemeColors(this);

        homeLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));

        ThemeManager.changeColorState(bottomNavigation);

        mapFragment.onThemeChanged();
        chatsFragment.onThemeChanged();
        peoplesFragment.onThemeChanged();

    }

    @Override
    public void onGroupChatCreated(long groupChatID)
    {
        APIHandler<EmptyDTO, HomeActivity> apiHandler = new APIHandler<>(this);
        apiHandler.apiGET(String.format(Locale.getDefault(), "%s/%d", APIEndpoints.TALKSTER_API_GROUP_GET, groupChatID), UserAccount.getInstance().getUserJWT().getAccessToken());
    }

    @Override
    public void onGroupChatMessageReceived(String messageRAW) { chatsFragment.onGroupChatMessageReceived(messageRAW); }
}