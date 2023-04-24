package com.client.talkster.interfaces;

import com.client.talkster.classes.Chat;

import java.util.List;

public interface IChatListener extends IChatWebSocketHandler
{
    void addChat(Chat chat);
    void updateChatList(List<Chat> chats);
    void onChatHistoryCleared(long chatID);
    void onChatDeleted(long chatID);
    void onChatMuted(long chatID, long muteTime);
}
