package com.client.talkster.controllers.talkster;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.client.talkster.R;
import com.client.talkster.dto.PersonalMessageDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.client.talkster.utils.enums.MessageType;
import com.google.gson.Gson;

import java.time.OffsetDateTime;

import ua.naiksoftware.stomp.client.StompClient;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatsFragment extends Fragment implements IFragmentActivity
{

    public StompClient webSocket;
    private Button sendMessageButton;
    private Button checkConnectionButton;
    private EditText sendMessageInput;
    private EditText receiverInput;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChatsFragment(StompClient webSocket) {
        // Required empty public constructor
        this.webSocket = webSocket;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ChatsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ChatsFragment newInstance(String param1, String param2) {
        ChatsFragment fragment = new ChatsFragment(null);
        Log.d("hope", "hope");
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

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

            PersonalMessageDTO personalMessageDTO = new PersonalMessageDTO();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                personalMessageDTO.setTimestamp(OffsetDateTime.now().toString());

            personalMessageDTO.setMessage(sendMessageInput.getText().toString());
            personalMessageDTO.setSenderid("server");
            personalMessageDTO.setReceiverid(receiverInput.getText().toString());
            personalMessageDTO.setMessagetype(MessageType.TEXT_MESSAGE);


            Log.d("message", new Gson().toJson(personalMessageDTO));

            if(receiverInput.getText().toString().length() == 0)
                webSocket.send("/app/message", new Gson().toJson(personalMessageDTO)).subscribe();
            else
                webSocket.send("/app/private-message", new Gson().toJson(personalMessageDTO)).subscribe();
        });

        checkConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("SOCKET", "IsConnected:" + webSocket.isConnected() + " " + webSocket.isConnecting());
            }
        });

    }

    public void messageReceived(String payload)
    {
        Toast.makeText(getContext(), payload, Toast.LENGTH_SHORT).show();
    }
}