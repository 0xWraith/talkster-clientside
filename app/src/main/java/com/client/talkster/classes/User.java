package com.client.talkster.classes;

public class User
{
    private final long id;
    private String firstname;
    private String lastname;
    private String mail;

    public User(long id) { this.id = id; }

    public User(long id, String firstname, String mail)
    {
        this.id = id;
        this.mail = mail;
        this.lastname = "";
        this.firstname = firstname;
    }

    public User(long id, String firstname, String lastname, String mail)
    {
        this.id = id;
        this.mail = mail;
        this.lastname = lastname;
        this.firstname = firstname;
    }

    public long getId() { return id; }
    public String getMail() { return mail; }
    public String getLastname() { return lastname; }
    public String getFirstname() { return firstname; }

    public void setMail(String mail) { this.mail = mail; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
}
