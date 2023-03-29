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
import com.client.talkster.api.APIStompWebSocket;
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

    }
}