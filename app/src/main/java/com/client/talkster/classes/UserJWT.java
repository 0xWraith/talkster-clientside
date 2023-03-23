package com.client.talkster.classes;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class UserJWT implements Parcelable
{
    private long id;
    private String jwttoken;
    private String refresh_token;

    private UserJWT(Parcel in)
    {
        id = in.readLong();
        jwttoken = in.readString();
        refresh_token = in.readString();
    }

    public UserJWT(long id, String jwttoken, String refresh_token)
    {
        this.id = id;
        this.jwttoken = jwttoken;
        this.refresh_token = refresh_token;
    }

    public static final Parcelable.Creator<UserJWT> CREATOR = new Parcelable.Creator<UserJWT>()
    {
        public UserJWT[] newArray(int size) { return new UserJWT[size]; }
        public UserJWT createFromParcel(Parcel in) { return new UserJWT(in); }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeLong(id);
        dest.writeString(jwttoken);
        dest.writeString(refresh_token);
    }

    @Override
    public int describeContents() { return 0; }

    public long getID() { return id; }
    public String getJWTToken() { return jwttoken; }

    public String getRefreshToken() { return refresh_token; }
    public void setID(long id) { this.id = id; }
    public void setJWTToken(String jwttoken) { this.jwttoken = jwttoken; }

    public void setRefreshToken(String refresh_token) { this.refresh_token = refresh_token; }

    @Override
    public String toString() {
        return "UserJWT{" +
                "id=" + id +
                ", jwttoken='" + jwttoken + '\'' +
                ", refreshToken='" + refresh_token + '\'' +
                '}';
    }
}
