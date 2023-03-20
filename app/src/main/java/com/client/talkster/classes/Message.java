package com.client.talkster.classes;

import com.client.talkster.utils.enums.MessageType;

public class Message
{
    private long ID;
    private long chatID;
    private long senderID;
    private long receiverID;
    private MessageType messageType;
    private String messageContent;
    private String messageTimestamp;

    public Message() { }

    public long getID() { return ID; }
    public long getChatID() { return chatID; }
    public long getSenderID() { return senderID; }
    public long getReceiverID() { return receiverID; }
    public MessageType getMessageType() { return messageType; }
    public String getMessageContent() { return messageContent; }
    public String getMessageTimestamp() { return messageTimestamp; }

    public void setID(long ID) { this.ID = ID; }
    public void setChatID(long chatID) { this.chatID = chatID; }
    public void setSenderID(long senderID) { this.senderID = senderID; }
    public void setReceiverID(long receiverID) { this.receiverID = receiverID; }
    public void setMessageType(MessageType messageType) { this.messageType = messageType; }
    public void setMessageContent(String messageContent) { this.messageContent = messageContent; }
    public void setMessageTimestamp(String messageTimestamp) { this.messageTimestamp = messageTimestamp; }

    @Override
    public String toString() {
        return "Message{" +
                "ID=" + ID +
                ", chatID=" + chatID +
                ", senderID=" + senderID +
                ", receiverID=" + receiverID +
                ", messageType=" + messageType +
                ", messageContent='" + messageContent + '\'' +
                ", messageTimestamp='" + messageTimestamp + '\'' +
                '}';
    }
}
