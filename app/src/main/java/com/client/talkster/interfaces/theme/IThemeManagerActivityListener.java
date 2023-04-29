package com.client.talkster.interfaces.theme;

public interface IThemeManagerActivityListener
{
    void removeListener();
    void onThemeChanged();
    void loadApplicationTheme();
}
