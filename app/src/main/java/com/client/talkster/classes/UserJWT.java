package com.client.talkster.classes;

import java.io.Serializable;

public class UserJWT implements Serializable
{
    private long id;
    private String jwttoken;

    public long getID() { return id; }
    public String getJWTToken() { return jwttoken; }

    public void setID(long id) { this.id = id; }
    public void setJWTToken(String jwttoken) { this.jwttoken = jwttoken; }

    @Override
    public String toString() {
        return "UserJWT{" +
                "id=" + id +
                ", jwttoken='" + jwttoken + '\'' +
                '}';
    }
}
