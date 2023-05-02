package com.client.talkster.dto;

import android.location.Location;

import com.client.talkster.adapters.LocationAdapter;

import java.util.List;

public class LocationDTO
{
    private long userid;
    private double speed;
    private String username;
    private String jwttoken;
    private double latitude;
    private double longitude;
    private final List<Long> sendto;

    public LocationDTO() { sendto = null; }

    public LocationDTO(long userid, String username, String jwttoken, List<Long> sendto, Location location)
    {
        this.userid = userid;
        this.sendto = sendto;
        this.username = username;
        this.jwttoken = jwttoken;
        this.speed = location.getSpeed() * 3.6;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    public long getuserid() { return userid; }
    public double getSpeed() { return speed; }
    public List<Long> getSendto() { return sendto; }
    public String getUsername() { return username; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }

    public String getJwttoken() { return jwttoken; }

    //Setters
    public void setSpeed(double speed) { this.speed = speed; }
    public void setuserid(long userid) { this.userid = userid; }
    public void setJwttoken(String jwttoken) { this.jwttoken = jwttoken; }
    public void setUsername(String username) { this.username = username; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }


    @Override
    public String toString() {
        return "LocationDTO{" +
                "userid=" + userid +
                ", username='" + username + '\'' +
                ", userJWT='" + jwttoken + '\'' +
                ", speed=" + speed +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
