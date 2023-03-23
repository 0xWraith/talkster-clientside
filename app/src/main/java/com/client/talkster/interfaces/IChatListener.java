package com.client.talkster.interfaces;

import com.client.talkster.classes.Chat;

import java.util.List;

public interface IChatListener
{
    void addChat(Chat chat);
    void updateChatList(List<Chat> chats);
    void onMessageReceived(String message);
}
