package com.client.talkster.classes.theme;

import com.client.talkster.utils.enums.EThemeType;

public class Theme
{
    private final String name;
    private final int themeColor;
    private final int themeStyle;
    private final EThemeType themeType;


    private final int chat_inBubble;
    private final int chat_outBubbleGradient1;
    private final int chat_outBubbleGradient2;
    private final int chat_outBubbleGradient3;

    private final int chat_backgroundImage;


    public Theme(String name, EThemeType themeType, int themeStyle, int themeColor, int chat_inBubble, int chat_outBubbleGradient1, int chat_outBubbleGradient2, int chat_outBubbleGradient3, int chat_backgroundImage)
    {
        this.name = name;
        this.themeType = themeType;
        this.themeColor = themeColor;
        this.themeStyle = themeStyle;
        this.chat_inBubble = chat_inBubble;
        this.chat_backgroundImage = chat_backgroundImage;
        this.chat_outBubbleGradient1 = chat_outBubbleGradient1;
        this.chat_outBubbleGradient2 = chat_outBubbleGradient2;
        this.chat_outBubbleGradient3 = chat_outBubbleGradient3;
    }

    public String getName() { return name; }
    public int getThemeStyle() { return themeStyle; }
    public int getThemeColor() { return themeColor; }
    public int getChatInColor() { return chat_inBubble; }
    public EThemeType getThemeType() { return themeType; }
    public int getChatBackgroundImage() { return chat_backgroundImage; }
    public int getChatSenderColor1() { return chat_outBubbleGradient1; }
    public int getChatSenderColor2() { return chat_outBubbleGradient2; }
    public int getChatSenderColor3() { return chat_outBubbleGradient3; }

}
