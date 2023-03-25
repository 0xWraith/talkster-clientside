package com.client.talkster.interfaces;

import com.client.talkster.classes.Message;

import java.io.Serializable;

public interface IChatWebSocketHandler
{
    void onMessageReceived(String messageRAW);
    void onSendPrivateMessage(Message message);
}
