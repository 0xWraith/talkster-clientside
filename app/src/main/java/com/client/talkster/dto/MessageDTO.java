package com.client.talkster.dto;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.client.talkster.classes.User;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.enums.MessageType;

import java.io.Serializable;
import java.time.OffsetDateTime;

public class MessageDTO implements Parcelable
{
    private long id;
    private long chatid;
    private long senderid;
    private long receiverid;
    private String jwttoken;
    private MessageType messagetype;
    private String messagecontent;
    private String messagetimestamp;

    public MessageDTO() { }

    public long getid() { return id; }
    public long getchatid() { return chatid; }
    public long getsenderid() { return senderid; }
    public String getjwttoken() { return jwttoken; }
    public long getreceiverid() { return receiverid; }
    public MessageType getmessagetype() { return messagetype; }
    public String getmessagecontent() { return messagecontent; }
    public String getmessagetimestamp() { return messagetimestamp; }

    public void setid(long id) { this.id = id; }
    public void setchatid(long chatID) { this.chatid = chatID; }
    public void setsenderid(long senderID) { this.senderid = senderID; }
    public void setjwttoken(String jwtToken) { this.jwttoken = jwtToken; }
    public void setreceiverid(long receiverID) { this.receiverid = receiverID; }
    public void setmessagetype(MessageType messageType) { this.messagetype = messageType; }
    public void setmessagecontent(String messageContent) { this.messagecontent = messageContent; }
    public void setmessagetimestamp(String messageTimestamp) { this.messagetimestamp = messageTimestamp; }

    @Override
    public String toString() {
        return "MessageDTO{" +
                "chatid=" + chatid +
                ", senderid=" + senderid +
                ", receiverid=" + receiverid +
                ", jwttoken='" + jwttoken + '\'' +
                ", messagetype=" + messagetype +
                ", messagecontent='" + messagecontent + '\'' +
                ", messagetimestamp='" + messagetimestamp + '\'' +
                '}';
    }

    public void createActionMessage(EPrivateChatAction action, long senderid, long receiverid, String jwttoken)
    {
        setsenderid(senderid);
        setjwttoken(jwttoken);
        setreceiverid(receiverid);
        setmessagetimestamp(OffsetDateTime.now().toString());

        if(action == EPrivateChatAction.DELETE_CHAT)
            setmessagetype(MessageType.DELETE_CHAT);

        else if(action == EPrivateChatAction.CLEAR_CHAT_HISTORY)
            setmessagetype(MessageType.CLEAR_CHAT_HISTORY);

        else if(action == EPrivateChatAction.MUTE_CHAT)
            setmessagetype(MessageType.MUTE_CHAT);
        else if(action == EPrivateChatAction.BLOCK_CHAT)
            setmessagetype(MessageType.BLOCK_CHAT);
        else if(action == EPrivateChatAction.UNBLOCK_CHAT)
            setmessagetype(MessageType.UNBLOCK_CHAT);
    }

    private MessageDTO(Parcel in)
    {
        id = in.readLong();
        chatid = in.readLong();
        senderid = in.readLong();
        receiverid = in.readLong();
        jwttoken = in.readString();
        messagetype = MessageType.valueOf(in.readString());
        messagecontent = in.readString();
        messagetimestamp = in.readString();
    }

    public static final Parcelable.Creator<MessageDTO> CREATOR = new Parcelable.Creator<MessageDTO>()
    {
        public MessageDTO[] newArray(int size) { return new MessageDTO[size]; }
        public MessageDTO createFromParcel(Parcel in) { return new MessageDTO(in); }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        Log.e("MessageDTO", this.toString());

        dest.writeLong(id);
        dest.writeLong(chatid);
        dest.writeLong(senderid);
        dest.writeLong(receiverid);
        dest.writeString(jwttoken);
        dest.writeString(messagetype.toString());
        dest.writeString(messagecontent);
        dest.writeString(messagetimestamp);
    }

    @Override
    public int describeContents() { return 0; }
}
