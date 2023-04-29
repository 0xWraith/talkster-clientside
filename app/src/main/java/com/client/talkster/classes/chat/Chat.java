package com.client.talkster.classes.chat;

import android.content.ClipDescription;

import com.client.talkster.classes.chat.message.Message;
import com.client.talkster.utils.enums.EChatType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Chat implements Serializable
{
    protected long id;
    protected long muteTime;
    protected EChatType type;
    protected String updatedAt;
    protected List<Message> messages;

    public Chat(EChatType type)
    {
        this.type = type;
        messages = new ArrayList<>();
    }

    public void clearMessages() { messages.clear(); }

    public long getId() { return id; }
    public EChatType getType() { return type; }
    public long getMuteTime() { return muteTime; }
    public String getUpdatedAt() { return updatedAt; }
    public List<Message> getMessages()
    {
        return messages == null ? messages = new ArrayList<>() : messages;
    }

    public void setId(long id) { this.id = id; }
    public void setType(EChatType type) { this.type = type; }
    public void setMuteTime(long muteTime) { this.muteTime = muteTime; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setMessages(List<Message> messages) { this.messages = messages; }

    public boolean isMuted()
    {
        if(muteTime == -1)
            return true;

        return muteTime > System.currentTimeMillis() / 1000L;
    }

    @Override
    public String toString()
    {
        return "Chat{" +
                "id=" + id +
                ", muteTime=" + muteTime +
                ", type=" + type +
                ", updatedAt='" + updatedAt + '\'' +
                ", messages=" + messages +
                '}';
    }

    public Message getLastMessage()
    {
        if(messages == null || messages.size() == 0)
            return null;

        return messages.get(messages.size() - 1);
    }
}
