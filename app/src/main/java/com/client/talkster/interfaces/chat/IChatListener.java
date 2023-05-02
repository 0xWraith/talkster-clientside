package com.client.talkster.interfaces.chat;


import com.client.talkster.classes.chat.Chat;
import com.client.talkster.classes.chat.GroupChat;
import com.client.talkster.classes.chat.PrivateChat;
import com.client.talkster.utils.enums.EChatType;

import java.util.List;

public interface IChatListener extends IChatWebSocketHandler
{
    void addChat(Chat chat);
    void onChatHistoryCleared(long chatID, EChatType chatType);
    void onChatDeleted(long chatID, EChatType chatType);
    void onChatMuted(long chatID, long muteTime, EChatType chatType);
    void updateChatList(List<PrivateChat> privateChats, List<GroupChat> groupChats);
}
