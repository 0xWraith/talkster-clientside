package com.client.talkster.interfaces.chat;

import com.client.talkster.classes.User;
import com.client.talkster.classes.chat.message.Message;

@FunctionalInterface
public interface IGroupChatGetMessageSender
{
    User getMessageSender(Message message);
}
