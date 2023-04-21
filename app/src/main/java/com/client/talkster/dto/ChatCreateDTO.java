package com.client.talkster.dto;

public class ChatCreateDTO {
    protected long senderID;
    protected String receiverEmail;

    public ChatCreateDTO(){}
    public ChatCreateDTO(long senderID, String receiverEmail) {
        this.senderID = senderID;
        this.receiverEmail = receiverEmail;
    }

    public long getSenderID() {
        return senderID;
    }

    public void setSenderID(long senderID) {
        this.senderID = senderID;
    }

    public String getReceiverEmail() {
        return receiverEmail;
    }

    public void setReceiverEmail(String receiverEmail) {
        this.receiverEmail = receiverEmail;
    }
}