package com.client.talkster.dto;

import com.client.talkster.classes.User;
import com.client.talkster.classes.UserJWT;

public class VerifiedUserDTO
{
    private User user;
    private UserJWT userJWT;

    public VerifiedUserDTO() {}

    public User getUser() { return user; }
    public UserJWT getUserJWT() { return userJWT; }

    public void setUser(User user) { this.user = user; }
    public void setUserJWT(UserJWT userJWT) { this.userJWT = userJWT; }

    @Override
    public String toString() {
        return "VerifiedUserDTO{" +
                "user=" + user +
                ", userJWT=" + userJWT +
                '}';
    }
}
