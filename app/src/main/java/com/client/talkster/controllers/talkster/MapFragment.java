package com.client.talkster.controllers.talkster;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import com.client.talkster.R;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.utils.enums.MessageType;
import com.google.gson.Gson;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;

public class MapFragment extends Fragment implements IFragmentActivity
{
    private UserJWT userJWT;
    public StompClient webSocket;
    private Button sendMessageButton;
    private Button checkConnectionButton;
    private EditText sendMessageInput;
    private EditText receiverInput;

    public MapFragment(UserJWT userJWT)
    {
        this.userJWT = userJWT;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        getUIElements(view);

        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        receiverInput = view.findViewById(R.id.receiverInput);
        sendMessageInput = view.findViewById(R.id.sendMessageInput);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);
        checkConnectionButton = view.findViewById(R.id.checkConnectionButton);

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

            Log.d("message", new Gson().toJson(messageDTO));

            if(receiverInput.getText().toString().length() == 0)
                webSocket.send("/app/message", new Gson().toJson(messageDTO)).subscribe();
            else
                webSocket.send("/app/private-message", new Gson().toJson(messageDTO)).subscribe();
        });

        checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SOCKET", "IsConnected:" + webSocket.isConnected() + " " + webSocket.isConnecting());
            }
        });
    }
}