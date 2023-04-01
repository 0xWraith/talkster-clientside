package com.client.talkster.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable
{
    private long id;
    private String firstname;
    private String lastname;
    private String mail;

    public User() { }

    private User(Parcel in)
    {
        id = in.readLong();
        firstname = in.readString();
        lastname = in.readString();
        mail = in.readString();
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
    public String getLastname() { return lastname; }
    public String getFirstname() { return firstname; }
    public String getFullName() { return firstname + " " + lastname; }

    public void setMail(String mail) { this.mail = mail; }
    public void setLastname(String lastname) { this.lastname = lastname; }
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
    }

    @Override
    public int describeContents() { return 0; }
}
