package com.client.talkster;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.client.talkster.adapters.ViewPagerAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.api.WebSocketPrivateChatSubscriber;
import com.client.talkster.api.WebSocketPublicChatSubscriber;
import com.client.talkster.api.WebSocketPublicMapSubscriber;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.controllers.talkster.MapFragment;
import com.client.talkster.controllers.talkster.PeoplesFragment;
import com.client.talkster.dto.LocationDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.TokenDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IChatListener;
import com.client.talkster.interfaces.IChatWebSocketHandler;
import com.client.talkster.interfaces.IMapGPSPositionUpdate;
import com.client.talkster.interfaces.IMapWebSocketHandler;
import com.client.talkster.services.LocationService;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.PermissionChecker;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class HomeActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IChatWebSocketHandler, IMapWebSocketHandler
{
    private User user;
    private UserJWT userJWT;
    private String FCMToken;
    private MapFragment mapFragment;
    private ChatsFragment chatsFragment;
    private PeoplesFragment peoplesFragment;
    private ViewPager2 homeViewPager;
    private IChatListener iChatListener;
    private IMapGPSPositionUpdate iMapGPSPositionUpdate;
    private ArrayList<Fragment> fragments;
    private APIStompWebSocket apiStompWebSocket;
    private BroadcastReceiver sendMessageReceiver;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        mapFragment = new MapFragment(userJWT);
        chatsFragment = new ChatsFragment(userJWT);
        peoplesFragment = new PeoplesFragment(userJWT, user);

        iChatListener = chatsFragment;
        iMapGPSPositionUpdate = mapFragment;

        homeViewPager = findViewById(R.id.homeViewPager);
        homeViewPager.setUserInputEnabled(false);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        fragments = new ArrayList<>(Arrays.asList(chatsFragment, mapFragment, peoplesFragment));

        initializeBottomNavigation();
        initializeSocketConnection();

        Intent intent = new Intent(this, LocationService.class);
        intent.setAction(BundleExtraNames.LOCATION_SERVICE_START);
        startService(intent);

    }

    private void initializeSocketConnection()
    {
        apiStompWebSocket = new APIStompWebSocket();
        peoplesFragment.apiStompWebSocket = apiStompWebSocket;

        apiStompWebSocket.addTopic("/chatroom/public", new WebSocketPublicChatSubscriber(this));
        apiStompWebSocket.addTopic("/user/"+ userJWT.getID() +"/private", new WebSocketPrivateChatSubscriber(this));
        apiStompWebSocket.addTopic("/map/public", new WebSocketPublicMapSubscriber(this));

        apiStompWebSocket.connect();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        user = bundle.getParcelable(BundleExtraNames.USER);
        userJWT = bundle.getParcelable(BundleExtraNames.USER_JWT);

        sendMessageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                String action = intent.getAction();

                if(action.equals(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST))
                {
                    MessageDTO messageDTO = (MessageDTO) intent.getExtras().get(BundleExtraNames.CHAT_NEW_MESSAGE);
                    apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
                }
                else if(action.equals(BundleExtraNames.LOCATION_SERVICE_BROADCAST))
                {
                    Location location = (Location) intent.getExtras().get(BundleExtraNames.LOCATION_SERVICE_POSITION);

                    iMapGPSPositionUpdate.onMapGPSPositionUserUpdate(location);

                    LocationDTO locationDTO = new LocationDTO(userJWT.getID(), user.getFullName(), userJWT.getAccessToken(), location);
                    apiStompWebSocket.getWebSocketClient().send("/app/map", new Gson().toJson(locationDTO)).subscribe();
                }
            }
        };
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
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_NOTIFICATION_ADD_TOKEN, tokenDTO, userJWT.getAccessToken());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        registerReceiver(sendMessageReceiver, new IntentFilter(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST));
        registerReceiver(sendMessageReceiver, new IntentFilter(BundleExtraNames.LOCATION_SERVICE_BROADCAST));
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


            int responseCode = response.code();


            if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT))
            {
                String responseBody = response.body().string();
                Chat chat = new Gson().fromJson(responseBody, Chat.class);
                runOnUiThread (() -> iChatListener.addChat(chat));
            }
            else if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS))
            {
                if(responseCode != 200)
                    throw new UserUnauthorizedException("Unexpected response " + response);
                String responseBody = response.body().string();
                Chat[] chats = new Gson().fromJson(responseBody, Chat[].class);
                List<Chat> chatList = new ArrayList<>(Arrays.asList(chats));

                runOnUiThread (() -> iChatListener.updateChatList(chatList));
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_CHAT_GET_CHAT))
            {
                if (responseCode != 200){
                    throw new UserUnauthorizedException("Unexpected response " + response);
                }
                String responseBody = response.body().string();
                Chat chat = new Gson().fromJson(responseBody, Chat.class);
                Intent privateChatIntent = new Intent(getApplicationContext(), PrivateChatActivity.class);

                privateChatIntent.putExtra(BundleExtraNames.USER_JWT, userJWT);
                privateChatIntent.putExtra(BundleExtraNames.USER_CHAT, chat);

                startActivity(privateChatIntent);
            }
            else if(apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_UPDATE_PROFILE)) {
                if (responseCode != 200){
                    throw new UserUnauthorizedException("Unexpected response " + response);
                }
                runOnUiThread (() -> peoplesFragment.updateProfilePicture());
            }
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse: " + exception.getMessage()); }
    }

    private void initializeBottomNavigation()
    {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(this, fragments);
        homeViewPager.setAdapter(viewPagerAdapter);

        bottomNavigation.setOnItemSelectedListener(item -> {
            int ID = item.getItemId();

            if (ID == R.id.chatMenuID) {
                homeViewPager.setCurrentItem(0);
            }
            else if (ID == R.id.mapMenuID){
                homeViewPager.setCurrentItem(1);
            }
            else if(ID == R.id.peoplesMenuID) {
                homeViewPager.setCurrentItem(2);
            }
            return true;
        });
    }

    public void selectNavigationButton(int currentPosition){
        switch (currentPosition){
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            Uri uri = data.getData();
            sendProfileImage(uri);

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Request code:"+requestCode+" result code:"+resultCode, Toast.LENGTH_SHORT).show();
        }
    }

    private void sendProfileImage(Uri uri){
        FileContent fileContent = new FileContent();
        APIHandler<FileContent, HomeActivity> apiHandler = new APIHandler<>(this);
        try {
            ContentResolver cr = getContentResolver();
            fileContent.setContent(FileUtils.getBytes(uri, cr));
            fileContent.setType(FileUtils.getType(uri, cr));
            fileContent.setFilename(FileUtils.getFilename(uri, cr));
            apiHandler.apiMultipartPUT(APIEndpoints.TALKSTER_API_FILE_UPDATE_PROFILE,fileContent, userJWT.getAccessToken());
        } catch (IOException e){
            System.out.println("This Exception was thrown inside the sendImage method");
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(String messageRAW) { iChatListener.onMessageReceived(messageRAW); }

    @Override
    @Deprecated
    public void onSendPrivateMessage(Message message) { }

    @Override
    public void onMapMessageReceived(String locationRAW) { iMapGPSPositionUpdate.onMapGPSPositionUpdate(locationRAW); }
}