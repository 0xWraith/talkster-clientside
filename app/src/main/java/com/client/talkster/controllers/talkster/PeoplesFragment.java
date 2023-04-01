package com.client.talkster.controllers.talkster;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.client.talkster.R;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.utils.enums.MessageType;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
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
    private EditText receiverInput;
    private Button sendMessageButton, profileButton;
    private ImageView profileImageView;
    private EditText sendMessageInput;
    public APIStompWebSocket apiStompWebSocket;

    public PeoplesFragment(UserJWT userJWT) { this.userJWT = userJWT; }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_peoples, container, false);
        getUIElements(view);
        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        receiverInput = view.findViewById(R.id.receiverInput);
        sendMessageInput = view.findViewById(R.id.sendMessageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        profileButton = view.findViewById(R.id.profileButton);
        profileImageView = view.findViewById(R.id.profileImageView);

        sendMessageButton.setOnClickListener(view1 -> {

            MessageDTO messageDTO = new MessageDTO();

            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getJWTToken());
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

        profileButton.setOnClickListener(view1 -> {
            getProfilePicture();
        });
    }

    private void getProfilePicture(){
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_FILE_GET_PROFILE, userJWT.getJWTToken());
    }

    public void setProfilePicture(Bitmap bitmap){
        profileImageView.setImageBitmap(bitmap);
    }
}