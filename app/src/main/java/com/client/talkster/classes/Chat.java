package com.client.talkster.classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Chat implements Serializable
{
    private long id;
    private long ownerID;
    private long muteTime;
    private long receiverID;
    private String receiverLastname;
    private String receiverFirstname;
    private String updatedAt;

    private List<Message> messages;

    public Chat()
    {
        messages = new ArrayList<>();
    }

    public void clearMessages() { messages.clear(); }

    public long getId() { return id; }
    public long getOwnerID() { return ownerID; }
    public long getMuteTime() { return muteTime; }
    public long getReceiverID() { return receiverID; }
    public String getUpdatedAt() { return updatedAt; }
    public List<Message> getMessages() { return messages; }
    public String getReceiverLastname() { return receiverLastname; }
    public String getReceiverFirstname() { return receiverFirstname; }

    public void setId(long id) { this.id = id; }
    public void setOwnerID(long ownerID) { this.ownerID = ownerID; }
    public void setMuteTime(long muteTime) { this.muteTime = muteTime; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public void setReceiverID(long receiverID) { this.receiverID = receiverID; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
    public void setReceiverLastname(String receiverLastname) { this.receiverLastname = receiverLastname; }
    public void setReceiverFirstname(String receiverFirstname) { this.receiverFirstname = receiverFirstname; }

    @Override
    public String toString() {
        return "Chat{" +
                "ID=" + id +
                ", muteTime=" + muteTime +
                ", ownerID=" + ownerID +
                ", receiverID=" + receiverID +
                ", receiverLastname='" + receiverLastname + '\'' +
                ", receiverFirstname='" + receiverFirstname + '\'' +
                ", messages=" + messages +
                ", updatedAt='" + updatedAt + '\'' +
                '}';
    }

    public boolean isMuted()
    {
        if(muteTime == -1)
            return true;

        return muteTime > System.currentTimeMillis() / 1000L;
    }

    public String getReceiverName() { return receiverFirstname + " " + receiverLastname; }
}
