package com.client.talkster.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

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
                ", receiverID=" + receiverID +
                ", messageType=" + messageType +
                ", messageContent='" + messageContent + '\'' +
                ", messageTimestamp='" + messageTimestamp + '\'' +
                '}';
    }

    /*@Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i)
    {
        parcel.writeLong(id);
        parcel.writeLong(chatID);
        parcel.writeLong(senderID);
        parcel.writeLong(receiverID);
        parcel.writeString(messageType.name());
        parcel.writeString(messageContent);
        parcel.writeString(messageTimestamp);
    }

    private Message(Parcel in)
    {
        id = in.readLong();
        chatID = in.readLong();
        senderID = in.readLong();
        receiverID = in.readLong();
        messageType = MessageType.valueOf(in.readString());
        messageContent = in.readString();
        messageTimestamp = in.readString();
    }

    public static final Parcelable.Creator<Message> CREATOR = new Parcelable.Creator<Message>()
    {
        public Message[] newArray(int size) { return new Message[size]; }
        public Message createFromParcel(Parcel in) { return new Message(in); }
    };*/
}
