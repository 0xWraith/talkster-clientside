package com.client.talkster.dto;

public class AuthenticationDTO
{
//    protected Long id;
    protected String code;
    protected String mail;

    public AuthenticationDTO() {}
    public AuthenticationDTO(String mail) { this.mail = mail; }

    //    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getMail() { return mail; }

//    public void setId(Long id) { this.id = id; }
    public void setCode(String code) { this.code = code; }
    public void setMail(String mail) { this.mail = mail; }
}
