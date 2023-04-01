package com.client.talkster.interfaces;

import com.client.talkster.classes.UserJWT;
import com.client.talkster.dto.VerifiedUserDTO;

public interface IMainActivityScreen
{
    void showIntroductionScreen();
    void showHomeScreen(VerifiedUserDTO verifiedUserDTO);
}
