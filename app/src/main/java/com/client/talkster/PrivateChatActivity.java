package com.client.talkster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ChatMessagesAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.MessageType;
import com.github.dhaval2404.imagepicker.ImagePicker;

import org.modelmapper.ModelMapper;

import java.io.IOException;
import java.time.OffsetDateTime;

import okhttp3.Call;
import okhttp3.Response;

public class PrivateChatActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler
{

    private Chat chat;
    private String CHAT_BROADCAST = BundleExtraNames.CHAT_RECEIVE_BROADCAST;
    private UserJWT userJWT;
    private TextView userNameText, userStatusText;
    private ImageButton chatSendButton, backButton, mediaButton, closeMediaButton;
    private Button galleryButton, cameraButton;
    private EditText chatInputText;
    private RecyclerView chatMessagesList;
    private View chatView;
    private BroadcastReceiver messageReceiver;
    private ChatMessagesAdapter chatMessagesAdapter;
    private LinearLayoutManager recyclerLayoutManager;
    private ConstraintLayout mediaChooserLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        getBundleElements();
        getUIElements();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void getUIElements()
    {
        userNameText = findViewById(R.id.userNameText);
        chatInputText = findViewById(R.id.chatInputText);
        chatSendButton = findViewById(R.id.chatSendButton);
        backButton = findViewById(R.id.backButton);
        mediaButton = findViewById(R.id.mediaButton);
        closeMediaButton = findViewById(R.id.closeMediaButton);
        galleryButton = findViewById(R.id.galleryButton);
        cameraButton = findViewById(R.id.cameraButton);
        userStatusText = findViewById(R.id.userStatusText);
        chatMessagesList = findViewById(R.id.chatMessagesList);
        chatView = findViewById(R.id.chatView);
        recyclerLayoutManager = (LinearLayoutManager)chatMessagesList.getLayoutManager();
        mediaChooserLayout = findViewById(R.id.mediaChooserLayout);

        userNameText.setText(chat.getReceiverName());
        userStatusText.setText("last seen at 12:35");

        chatMessagesAdapter = new ChatMessagesAdapter(chat.getMessages(), userJWT.getID(), this);
        chatMessagesList.setAdapter(chatMessagesAdapter);

        chatSendButton.setOnClickListener(view ->
        {
            String message = chatInputText.getText().toString().trim();

            int length = message.length();

            if(length == 0 || length > 4097)
                return;

            MessageDTO messageDTO = new MessageDTO();

            chatInputText.setText("");
            messageDTO.setchatid(chat.getId());
            messageDTO.setmessagecontent(message);
            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getJWTToken());
            messageDTO.setreceiverid(chat.getReceiverID());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            Message newMessage = new ModelMapper().map(messageDTO, Message.class);

            Intent intent = new Intent(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
            intent.putExtra(BundleExtraNames.CHAT_NEW_MESSAGE, messageDTO);
            sendBroadcast(intent);
        });

        backButton.setOnClickListener(view ->
        {
            Intent homeIntent = new Intent(PrivateChatActivity.this, HomeActivity.class);
            homeIntent.putExtra(BundleExtraNames.USER_JWT, userJWT);
           startActivity(homeIntent);
        });

        mediaButton.setOnClickListener(view ->
        {
            mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
        });

        closeMediaButton.setOnClickListener(view -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
        });

        cameraButton.setOnClickListener(view -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PrivateChatActivity.this)
                    .cameraOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        galleryButton.setOnClickListener(view -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PrivateChatActivity.this)
                    .galleryOnly()
                    .cropSquare()
                    .maxResultSize(1024,1024)
                    .start(101);
        });

        chatView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                mediaChooserLayout.animate().translationY(0).setDuration(250);
                return false;
            }
        });

    }

    private void addMessage(Message message)
    {
        chatMessagesAdapter.getMessages().add(message);
        chatMessagesAdapter.notifyItemInserted(chatMessagesAdapter.getItemCount() - 1);
        recyclerLayoutManager.scrollToPositionWithOffset(chatMessagesAdapter.getItemCount() - 1,0);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        IntentFilter filter = new IntentFilter(CHAT_BROADCAST);
        registerReceiver(messageReceiver, filter);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        unregisterReceiver(messageReceiver);
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        chat = (Chat) bundle.get(BundleExtraNames.USER_CHAT);
        userJWT = (UserJWT) bundle.get(BundleExtraNames.USER_JWT);
        CHAT_BROADCAST += chat.getId();

        messageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {
                if (!intent.getAction().equals(CHAT_BROADCAST))
                    return;


                Message message = (Message) intent.getExtras().get(BundleExtraNames.CHAT_NEW_MESSAGE);
                addMessage(message);
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            Uri uri = data.getData();
            sendImage(uri);

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendImage(Uri uri){
        FileContent fileContent = new FileContent();
        APIHandler<FileContent, PrivateChatActivity> apiHandler = new APIHandler<>(this);
        try {
            ContentResolver cr = getContentResolver();
            fileContent.setContent(FileUtils.getBytes(uri, cr));
            fileContent.setType(FileUtils.getType(uri, cr));
            fileContent.setFilename(FileUtils.getFilename(uri, cr));
            apiHandler.apiMultipartPUT(APIEndpoints.TALKSTER_API_FILE_UPDATE_PROFILE,fileContent,userJWT.getJWTToken());
        } catch (IOException e){
            System.out.println("This Exception was thrown inside the sendImage method");
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) {

    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl) {

    }
}