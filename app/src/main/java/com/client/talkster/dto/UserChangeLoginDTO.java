package com.client.talkster.dto;

public class UserChangeLoginDTO
{
    private long id;
    private String login;
    private boolean result;

    public UserChangeLoginDTO() { }

    public long getId() { return id; }
    public String getLogin() { return login; }
    public boolean getResult() { return result; }

    public void setId(long id) { this.id = id; }
    public void setLogin(String login) { this.login = login; }
    public void setResult(boolean result) { this.result = result; }
}
