package com.client.talkster.classes;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class User implements Serializable
{
    private long id;
    private String firstname;
    private String lastname;
    private String mail;
    private String username;
    private String biography;
    private long imageID;
    private Bitmap avatar;
    private boolean mapTracker;
    private final List<User> contacts;
    private final List<Long> contactIDs;

    public User()
    {
        contacts = new ArrayList<>();
        contactIDs = new ArrayList<>();
    }

    /*private User(Parcel in)
    {
        id = in.readLong();
        firstname = in.readString();
        lastname = in.readString();
        mail = in.readString();
        username = in.readString();
        biography = in.readString();
        avatar = in.readParcelable(Bitmap.class.getClassLoader());
        contacts = in.createTypedArrayList(User.CREATOR);
        imageID = in.readLong();
    }*/


    public long getId() { return id; }
    public String getMail() { return mail; }
    public long getImageID() { return imageID; }
    public Bitmap getAvatar() { return avatar; }
    public String getStatus() { return "Online"; }
    public String getLastname() { return lastname; }
    public String getFirstname() { return firstname; }
    public List<User> getContacts() { return contacts; }
    public boolean getMapTracker() { return mapTracker; }
    public List<Long> getContactIDs() { return contactIDs; }
    public String getFullName() { return firstname + " " + lastname; }
    public String getUsername() { return username == null ? "Not created" : username; }
    public String getBiography() { return biography == null ? "Not created" : biography; }

    public void setId(long id) { this.id = id; }
    public void setMail(String mail) { this.mail = mail; }
    public void addContact(User user)
    {
        contacts.add(user);

        if (!contactIDs.contains(user.getId()))
            contactIDs.add(user.getId());
    }
    public void setAvatar(Bitmap avatar) { this.avatar = avatar; }
    public void setImageID(long imageID) { this.imageID = imageID; }
    public void setUsername(String username) { this.username = username; }
    public void setLastname(String lastname) { this.lastname = lastname; }
    public void setBiography(String biography) { this.biography = biography; }
    public void setFirstname(String firstname) { this.firstname = firstname; }
    public void setMapTracker(boolean mapTracker) { this.mapTracker = mapTracker; }

    /*public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<>()
    {
        public User[] newArray(int size) {return new User[size]; }
        public User createFromParcel(Parcel in) { return new User(in); }
    };

   @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(firstname);
        dest.writeString(lastname);
        dest.writeString(mail);
        dest.writeString(username);
        dest.writeString(biography);
        dest.writeParcelable(avatar, flags);
        dest.writeTypedList(contacts);
        dest.writeLong(imageID);
    }

    @Override
    public int describeContents() { return 0; }*/

    @NonNull
    @Override
    public String toString()
    {
        return "User{" +
                "id=" + id +
                ", mail='" + mail + '\'' +
                ", avatar=" + avatar +
                ", username='" + username + '\'' +
                ", lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", biography='" + biography + '\'' +
                ", contacts=" + contacts +
                '}';
    }
}
