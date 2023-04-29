package com.client.talkster.interfaces.chat;

public interface IGroupChatListener
{
    void onGroupChatCreated(long groupChatID);
    void onGroupChatMessageReceived(String messageRAW);
}
