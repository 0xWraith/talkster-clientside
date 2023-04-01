package com.client.talkster.classes;

import android.os.Parcel;
import android.os.Parcelable;

public class UserJWT implements Parcelable
{
    private long id;
    private String accessToken;
    private String refreshToken;

    private UserJWT(Parcel in)
    {
        id = in.readLong();
        accessToken = in.readString();
        refreshToken = in.readString();
    }

    public UserJWT(long id, String accessToken, String refreshToken)
    {
        this.id = id;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
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
        dest.writeString(accessToken);
        dest.writeString(refreshToken);
    }

    @Override
    public int describeContents() { return 0; }

    public long getID() { return id; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }

    public void setID(long id) { this.id = id; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    @Override
    public String toString() {
        return "UserJWT{" +
                "id=" + id +
                ", access_token='" + accessToken + '\'' +
                ", refreshToken='" + refreshToken + '\'' +
                '}';
    }
}
