package com.client.talkster.interfaces;

@FunctionalInterface
public interface IChatMessagesListener
{
    void onMessageReceived(String messageRAW);
}
