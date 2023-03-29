package com.client.talkster.controllers.talkster;

import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.client.talkster.R;
import com.client.talkster.api.APIStompWebSocket;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.utils.enums.MessageType;
import com.google.gson.Gson;

import java.time.OffsetDateTime;

public class PeoplesFragment extends Fragment implements IFragmentActivity
{
    private UserJWT userJWT;
    private EditText receiverInput;
    private Button sendMessageButton;
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
    }
}