package com.client.talkster.dto;

import com.client.talkster.utils.enums.MessageType;

public class PersonalMessageDTO
{
    private String message;
    private String senderid;
    private String receiverid;
    private MessageType messagetype;
    private String timestamp;

    public String getMessage() { return message; }
    public String getSenderid() { return senderid; }
    public String getTimestamp() { return timestamp; }
    public String getReceiverid() { return receiverid; }
    public MessageType getMessagetype() { return messagetype; }


    public void setMessage(String message) { this.message = message; }
    public void setSenderid(String senderid) { this.senderid = senderid; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public void setReceiverid(String receiverid) { this.receiverid = receiverid; }
    public void setMessagetype(MessageType messagetype) { this.messagetype = messagetype; }
}
