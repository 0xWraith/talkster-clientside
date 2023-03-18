package com.client.talkster.classes;

public class MessageDTO
{
    private String sendername;
    private String receivername;
    private String message;
    private String date;

    public String getDate() { return date; }
    public String getMessage() { return message; }
    public String getSendername() { return sendername; }
    public String getReceivername() { return receivername; }

    public void setDate(String date) { this.date = date; }
    public void setMessage(String message) { this.message = message; }
    public void setReceivername(String receivername) { this.receivername = receivername; }
    public void setSendername(String sendername) { this.sendername = sendername; }
}
