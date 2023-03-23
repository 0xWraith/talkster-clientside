package com.client.talkster.interfaces;

import com.client.talkster.classes.UserJWT;

public interface IMainActivityScreen
{
    void showIntroductionScreen();
    void showHomeScreen(UserJWT userJWT);
}
