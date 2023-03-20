package com.client.talkster.controllers.talkster;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.adapters.ChatListAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.authorization.InputMailActivity;
import com.client.talkster.dto.AuthenticationDTO;
import com.client.talkster.dto.EmptyDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IFragmentActivity;
import com.google.gson.Gson;

import org.modelmapper.ModelMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class ChatsFragment extends Fragment implements IFragmentActivity
{
    private final UserJWT userJWT;
    private RecyclerView userChatList;
    private LinearLayout welcomeBlock;
    private ChatListAdapter chatListAdapter;

    public ChatsFragment(UserJWT userJWT)
    {
        this.userJWT = userJWT;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState); }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        getUIElements(view);
        reloadUserChats();

        return view;
    }

    @Override
    public void getUIElements(View view)
    {
        welcomeBlock = view.findViewById(R.id.welcomeBlock);
        userChatList = view.findViewById(R.id.userChatList);
        chatListAdapter = new ChatListAdapter(userJWT.getID(), new ArrayList<>(), getContext());

        userChatList.setAdapter(chatListAdapter);
        userChatList.setLayoutManager(new LinearLayoutManager(getContext()));
    }

    private void reloadUserChats()
    {
        APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
        apiHandler.apiGET(APIEndpoints.TALKSTER_API_CHAT_GET_CHATS, userJWT.getJWTToken(), getContext());
    }

    public void updateChatList(List<Chat> chatList)
    {
        chatListAdapter.chatList = chatList;

        updateChatListVisibility();

        if(chatList.size() > 0)
            chatListAdapter.notifyItemChanged(0);
    }

    private void updateChatListVisibility()
    {
        if(chatListAdapter.chatList == null || chatListAdapter.chatList.size() == 0)
        {
            welcomeBlock.setVisibility(View.VISIBLE);
            userChatList.setVisibility(View.INVISIBLE);
        }
        else
        {
            welcomeBlock.setVisibility(View.INVISIBLE);
            userChatList.setVisibility(View.VISIBLE);
        }
    }

    public void onUserReceivedMessage(String messageRAW)
    {
        boolean isChatExist = false;
        ModelMapper modelMapper = new ModelMapper();
        Message message = modelMapper.map(new Gson().fromJson(messageRAW, MessageDTO.class), Message.class);

        for(int i = 0; i < chatListAdapter.chatList.size(); i++)
        {
            Chat chat = chatListAdapter.chatList.get(i);

            if(chat.getId() != message.getChatID())
                continue;

            isChatExist = true;
            chat.getMessages().add(message);
            Collections.swap(chatListAdapter.chatList, i, 0);

            chatListAdapter.notifyItemChanged(i);
            chatListAdapter.notifyItemMoved(i, 0);

            break;
        }

        if(!isChatExist)
        {
            APIHandler<Object, FragmentActivity> apiHandler = new APIHandler<>(getActivity());
            apiHandler.apiGET(String.format(Locale.getDefault(),"%s/%d/%d", APIEndpoints.TALKSTER_API_CHAT_GET_NEW_CHAT, message.getChatID(), userJWT.getID()), userJWT.getJWTToken(), getContext());
        }
    }

    public void addNewChat(Chat chat)
    {
        chatListAdapter.chatList.add(0, chat);
        updateChatListVisibility();

        chatListAdapter.notifyItemInserted(0);
        chatListAdapter.notifyItemChanged(0);
    }
}