package com.client.talkster.classes.chat.message;

import com.client.talkster.utils.enums.MessageType;

import java.io.Serializable;

public class Message implements Serializable
{
    private long id;
    private long chatID;
    private long senderID;
    private long receiverID;
    private MessageType messageType;
    private String messageContent;
    private String messageTimestamp;

    public Message() { }

    public String getOnlineTime()
    {
        String[] time = messageTimestamp.split("T")[1].split("\\.")[0].split(":");
        return time[0] + ":" + time[1];
    }

    public long getId() { return id; }
    public long getChatID() { return chatID; }
    public long getSenderID() { return senderID; }
    public long getReceiverID() { return receiverID; }
    public MessageType getMessageType() { return messageType; }
    public String getMessageContent() { return messageContent; }
    public String getMessageTimestamp() { return messageTimestamp; }

    public void setId(long id) { this.id = id; }
    public void setChatID(long chatID) { this.chatID = chatID; }
    public void setSenderID(long senderID) { this.senderID = senderID; }
    public void setReceiverID(long receiverID) { this.receiverID = receiverID; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public void setMessageTimestamp(String messageTimestamp) { this.messageTimestamp = messageTimestamp; }

    @Override
    public String toString() {
        return "Message{" +
                "ID=" + id +
                ", chatID=" + chatID +
                ", senderID=" + senderID +
                ", messageType=" + messageType +
                ", messageContent='" + messageContent + '\'' +
                ", messageTimestamp='" + messageTimestamp + '\'' +
                '}';
    }
}
