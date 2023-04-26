package com.client.talkster.classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable
{
    private long id;
    private String firstname;
    private String lastname;
    private String mail;
    private String login;
    private String biography;
    private Bitmap avatar;

    public User() { }

    private User(Parcel in)
    {
        id = in.readLong();
        firstname = in.readString();
        lastname = in.readString();
        mail = in.readString();
        login = in.readString();
        biography = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
    }

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
    public Bitmap getAvatar() { return avatar; }
    public String getStatus() { return "Online"; }
    public String getLastname() { return lastname; }
    public String getFirstname() { return firstname; }
    public String getFullName() { return firstname + " " + lastname; }
    public String getLogin() { return login == null ? "Not created" : login; }
    public String getBiography() { return biography == null ? "Not created" : biography; }

    public void setMail(String mail) { this.mail = mail; }
    public void setLogin(String login) { this.login = login; }
    public void setAvatar(Bitmap avatar) { this.avatar = avatar; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setBiography(String biography) { this.biography = biography; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>()
    {
        public User[] newArray(int size) { return new User[size]; }
        public User createFromParcel(Parcel in) { return new User(in); }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(mail);
        dest.writeString(login);
        dest.writeString(biography);
        dest.writeParcelable(avatar, flags);
    }

    @Override
    public int describeContents() { return 0; }

    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", mail='" + mail + '\'' +
                ", login='" + login + '\'' +
                ", biography='" + biography + '\'' +
                ", avatar=" + avatar +
                '}';
    }
}
