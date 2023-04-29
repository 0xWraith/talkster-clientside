package com.client.talkster.dto;

public class UserDTO
{
    private long id;
    private String firstname;
    private String lastname;
    private String mail;
    private String username;
    private String biography;
    private long imageID;

    public UserDTO() {}

    public long getId() { return id; }
    public String getMail() { return mail; }
    public long getImageID() {return imageID;}
    public String getUsername() { return username; }
    public String getLastname() { return lastname; }
    public String getBiography() { return biography; }
    public String getFirstname() { return firstname; }

    public void setId(long id) { this.id = id; }
    public void setMail(String mail) { this.mail = mail; }
    public void setImageID(long imageID) {this.imageID = imageID;}
    public void setUsername(String username) { this.username = username; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setBiography(String biography) { this.biography = biography; }
}
