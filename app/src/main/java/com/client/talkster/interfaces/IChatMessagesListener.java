package com.client.talkster.interfaces;

import com.client.talkster.classes.Message;

public interface IChatMessagesListener
{
    void onMessageReceived(Message message);
}
