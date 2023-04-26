package com.client.talkster.classes;

public class UserAccount
{
    private User user;
    private UserJWT userJWT;
    private static volatile UserAccount INSTANCE;

    private UserAccount() {}

    public static UserAccount getInstance()
    {
        if (INSTANCE != null)
            return INSTANCE;

        synchronized (UserAccount.class)
        {
            if (INSTANCE == null)
                INSTANCE = new UserAccount();

            return INSTANCE;
        }
    }

    public User getUser() { return user; }
    public UserJWT getUserJWT() { return userJWT; }

    public void setUser(User user) { this.user = user; }
    public void setUserJWT(UserJWT userJWT) { this.userJWT = userJWT; }

    @Override
    public String toString()
    {
        return "UserAccount{" +
                "user=" + user +
                ", userJWT=" + userJWT +
                '}';
    }
}
