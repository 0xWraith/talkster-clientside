package com.client.talkster.interfaces;

import com.client.talkster.classes.Chat;

import java.util.List;

public interface IChatListener extends IChatMessagesListener
{
    void addChat(Chat chat);
    void updateChatList(List<Chat> chats);
}
