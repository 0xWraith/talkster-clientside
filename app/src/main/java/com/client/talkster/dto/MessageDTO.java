package com.client.talkster.dto;

import com.client.talkster.utils.enums.MessageType;

public class MessageDTO
{
    private long id;
    private long chatid;
    private long senderid;
    private long receiverid;
    private String jwttoken;
    private MessageType messagetype;
    private String messagecontent;
    private String messagetimestamp;

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
}
