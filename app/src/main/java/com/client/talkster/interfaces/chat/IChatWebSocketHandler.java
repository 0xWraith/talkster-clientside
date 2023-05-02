package com.client.talkster.interfaces.chat;

public interface IChatWebSocketHandler
{
    void onMessageReceived(String messageRAW);
}
