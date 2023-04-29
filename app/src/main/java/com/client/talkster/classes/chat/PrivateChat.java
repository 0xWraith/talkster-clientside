package com.client.talkster.classes.chat;

import com.client.talkster.utils.enums.EChatType;

public class PrivateChat extends Chat
{
    private long ownerID;
    private long receiverID;
    private String receiverLastname;
    private String receiverFirstname;

    public PrivateChat() { super(EChatType.PRIVATE_CHAT); }

    public long getOwnerID() { return ownerID; }
    public long getReceiverID() { return receiverID; }
    public String getReceiverFirstname() { return receiverFirstname; }
    public String getReceiverName() { return receiverFirstname + " " + receiverLastname; }


    public String getReceiverLastname() { return receiverLastname; }

    public void setOwnerID(long ownerID) { this.ownerID = ownerID; }
    public void setReceiverID(long receiverID) { this.receiverID = receiverID; }
    public void setReceiverLastname(String receiverLastname) { this.receiverLastname = receiverLastname; }
    public void setReceiverFirstname(String receiverFirstname) { this.receiverFirstname = receiverFirstname; }
}
