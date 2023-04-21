package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.client.talkster.HomeActivity;
import com.client.talkster.PrivateChatActivity;
import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.ChatCreateDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.NameDTO;
import com.client.talkster.dto.TokenDTO;
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
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Response;

public class PeoplesFragment extends Fragment implements IFragmentActivity
{
    private UserJWT userJWT;
    private User user;
    private Button galleryButton, cameraButton, addFriendButton;
    private ImageButton imageEditButton, closeMediaButton, firstNameEditButton, firstNameSaveButton;
    private ImageButton lastNameEditButton, lastNameSaveButton;
    private ImageView profileImageView;
    private View profileView, leftPager;
    private EditText firstNameEditText, lastNameEditText, mailEditText;
    private TextView firstNameView, lastNameView;
    private ConstraintLayout mediaChooserLayout, firstNameLayout, firstNameEditLayout, lastNameLayout, lastNameEditLayout;
    private FileUtils fileUtils;
    public APIStompWebSocket apiStompWebSocket;

    private final int MIN_DISTANCE = 300;
    private float x1,x2;

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
        updateProfilePicture();
        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        profileImageView = view.findViewById(R.id.profileImageView);
        imageEditButton = view.findViewById(R.id.imageEditButton);
        galleryButton = view.findViewById(R.id.galleryButton);
        cameraButton = view.findViewById(R.id.cameraButton);
        closeMediaButton = view.findViewById(R.id.closeMediaButton);
        mediaChooserLayout = view.findViewById(R.id.mediaChooserLayout);
        profileView = view.findViewById(R.id.profileView);

        firstNameLayout = view.findViewById(R.id.firstNameLayout);
        firstNameEditLayout = view.findViewById(R.id.firstNameEditLayout);
        firstNameEditButton = view.findViewById(R.id.firstNameEditButton);
        firstNameSaveButton = view.findViewById(R.id.firstNameSaveButton);

        lastNameLayout = view.findViewById(R.id.lastNameLayout);
        lastNameEditLayout = view.findViewById(R.id.lastNameEditLayout);
        lastNameEditButton = view.findViewById(R.id.lastNameEditButton);
        lastNameSaveButton = view.findViewById(R.id.lastNameSaveButton);

        firstNameView = view.findViewById(R.id.firstNameView);
        lastNameView = view.findViewById(R.id.lastNameView);
        firstNameEditText = view.findViewById(R.id.firstNameEditText);
        lastNameEditText = view.findViewById(R.id.lastNameEditText);

        firstNameView.setText(user.getFirstname());
        firstNameEditText.setText(user.getFirstname());
        String lastname = user.getLastname();
        if (lastname.isBlank()){
            lastNameView.setText(R.string.last_name_placeholder);
            lastNameView.setTextColor(getResources().getColor(R.color.previewSecondaryText));
            lastNameEditText.setText("");
        }
        else {
            lastNameView.setText(lastname);
            lastNameEditText.setText(lastname);
        }

        addFriendButton = view.findViewById(R.id.addFriendButton);
        mailEditText = view.findViewById(R.id.mailEditText);

        leftPager = view.findViewById(R.id.leftPager);

        initPager();

        addFriendButton.setOnClickListener(view1 -> {
            ChatCreateDTO chatCreateDTO = new ChatCreateDTO();
            APIHandler<ChatCreateDTO, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            chatCreateDTO.setSenderID(userJWT.getID());
            chatCreateDTO.setReceiverEmail(mailEditText.getText().toString());
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_CHAT_CREATE, chatCreateDTO, userJWT.getAccessToken());
        });

        imageEditButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
        });

        closeMediaButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
        });

        cameraButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(getActivity())
                    .cameraOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        galleryButton.setOnClickListener(view1 -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(getActivity())
                    .galleryOnly()
                    .cropSquare()
                    .maxResultSize(256,256)
                    .start(101);
        });

        firstNameEditButton.setOnClickListener(view1 -> {
            firstNameLayout.setVisibility(View.INVISIBLE);
            firstNameEditLayout.setVisibility(View.VISIBLE);
        });

        firstNameSaveButton.setOnClickListener(view1 -> {
            updateUserName();
            firstNameLayout.setVisibility(View.VISIBLE);
            firstNameEditLayout.setVisibility(View.INVISIBLE);
        });

        lastNameEditButton.setOnClickListener(view1 -> {
            lastNameLayout.setVisibility(View.INVISIBLE);
            lastNameEditLayout.setVisibility(View.VISIBLE);
        });

        lastNameSaveButton.setOnClickListener(view1 -> {
            updateUserName();
            lastNameLayout.setVisibility(View.VISIBLE);
            lastNameEditLayout.setVisibility(View.INVISIBLE);
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

    private void initPager(){
        leftPager.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                switch(event.getAction())
                {
                    case MotionEvent.ACTION_DOWN:
                        x1 = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        x2 = event.getX();
                        float deltaX = x2 - x1;
                        if (deltaX > MIN_DISTANCE) {
                            ((HomeActivity)getActivity()).selectNavigationButton(1);}
                        break;
                }
                return true;
            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        updateProfilePicture();
    }

    public void updateProfilePicture(){
        profileImageView.setImageBitmap(fileUtils.getProfilePicture(user.getId()));
    }

    private void updateUserName(){
        String first = firstNameEditText.getText().toString();
        String last = lastNameEditText.getText().toString();
        firstNameView.setText(first);
        if (!last.isBlank()){
            lastNameView.setText(last);
            lastNameView.setTextColor(getResources().getColor(R.color.previewMainText));
        } else {
            lastNameView.setText(R.string.last_name_placeholder);
            lastNameView.setTextColor(getResources().getColor(R.color.previewSecondaryText));
            lastNameEditText.setText("");
        }
        NameDTO nameDTO = new NameDTO();
        APIHandler<NameDTO, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        nameDTO.setFirstName(first);
        nameDTO.setLastName(last);
        apiHandler.apiPUT(APIEndpoints.TALKSTER_API_USER_UPDATE_NAME, nameDTO, userJWT.getAccessToken());
    }

}