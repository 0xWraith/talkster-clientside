package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.client.talkster.PrivateChatActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.MessageType;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class PeoplesFragment extends Fragment implements IFragmentActivity
{
    private UserJWT userJWT;
    private User user;
    private EditText receiverInput;
    private Button sendMessageButton, galleryButton, cameraButton;
    private ImageButton imageEditButton, closeMediaButton;
    private ImageView profileImageView;
    private EditText sendMessageInput;
    private View profileView;
    private ConstraintLayout mediaChooserLayout;
    private FileUtils fileUtils;
    public APIStompWebSocket apiStompWebSocket;

    public PeoplesFragment(UserJWT userJWT, User user) { this.userJWT = userJWT; this.user = user;}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fileUtils = new FileUtils(userJWT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_peoples, container, false);
        getUIElements(view);
        setProfilePicture(fileUtils.getProfilePicture());
        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        receiverInput = view.findViewById(R.id.receiverInput);
        sendMessageInput = view.findViewById(R.id.sendMessageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        profileImageView = view.findViewById(R.id.profileImageView);
        imageEditButton = view.findViewById(R.id.imageEditButton);
        galleryButton = view.findViewById(R.id.galleryButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        closeMediaButton = view.findViewById(R.id.closeMediaButton);
        mediaChooserLayout = view.findViewById(R.id.mediaChooserLayout);
        profileView = view.findViewById(R.id.profileView);

        sendMessageButton.setOnClickListener(view1 -> {

            MessageDTO messageDTO = new MessageDTO();

            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getAccessToken());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagecontent(sendMessageInput.getText().toString());

            if(receiverInput.getText().toString().length() > 0)
                messageDTO.setreceiverid(Long.parseLong(receiverInput.getText().toString()));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            if(receiverInput.getText().toString().length() == 0)
                apiStompWebSocket.getWebSocketClient().send("/app/message", new Gson().toJson(messageDTO)).subscribe();
            else
                apiStompWebSocket.getWebSocketClient().send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();

        });

        imageEditButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
        });

        closeMediaButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            System.out.println("close button pushed");
        });

        cameraButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PeoplesFragment.this)
                    .cameraOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        galleryButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PeoplesFragment.this)
                    .galleryOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        profileView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                mediaChooserLayout.animate().translationY(0).setDuration(250);
                return false;
            }
        });
    }

    public void setProfilePicture(Bitmap bitmap){
        profileImageView.setImageBitmap(bitmap);
    }
}