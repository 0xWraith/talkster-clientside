package com.client.talkster.controllers;

import android.content.Context;
import android.graphics.Color;

import com.client.talkster.R;
import com.client.talkster.interfaces.IThemeManagerActivityListener;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThemeManager
{
    private static int currentTheme = R.style.Theme_Talkster_First;
    private static final HashMap<String, Integer> themeColor = new HashMap<>();
    private static final List<IThemeManagerActivityListener> listeners = new ArrayList<>();

    public static void applyTheme(int theme)
    {
        currentTheme = theme;
        notifyListeners();
    }

    private static void notifyListeners()
    {
        for (IThemeManagerActivityListener listener : listeners)
            listener.onThemeChanged();
    }

    public static void reloadThemeColors(Context context)
    {
        int color = -1;

        color = MaterialColors.getColor(context, R.attr.windowBackgroundWhite, Color.BLACK);
        themeColor.put("windowBackgroundWhite", color);

        color = MaterialColors.getColor(context, R.attr.windowBackgroundGray, Color.BLACK);
        themeColor.put("windowBackgroundGray", color);

        color = MaterialColors.getColor(context, R.attr.actionBarDefault, Color.BLACK);
        themeColor.put("actionBarDefault", color);

        color = MaterialColors.getColor(context, R.attr.actionBarDefaultIcon, Color.BLACK);
        themeColor.put("actionBarDefaultIcon", color);

        color = MaterialColors.getColor(context, R.attr.actionBarDefaultTitle, Color.BLACK);
        themeColor.put("actionBarDefaultTitle", color);

        color = MaterialColors.getColor(context, R.attr.actionBarDefaultSubtitle, Color.BLACK);
        themeColor.put("actionBarDefaultSubtitle", color);

        color = MaterialColors.getColor(context, R.attr.navBarTabActiveIcon, Color.BLACK);
        themeColor.put("navBarTabActiveIcon", color);

        color = MaterialColors.getColor(context, R.attr.navBarTabUnactiveIcon, Color.BLACK);
        themeColor.put("navBarTabUnactiveIcon", color);

        color = MaterialColors.getColor(context, R.attr.chat_name, Color.BLACK);
        themeColor.put("chat_name", color);

        color = MaterialColors.getColor(context, R.attr.chat_message, Color.BLACK);
        themeColor.put("chat_message", color);

        color = MaterialColors.getColor(context, R.attr.chat_divider, Color.BLACK);
        themeColor.put("chat_divider", color);

        color = MaterialColors.getColor(context, R.attr.chat_mediaMessage, Color.BLACK);
        themeColor.put("chat_mediaMessage", color);

        color = MaterialColors.getColor(context, R.attr.chat_date, Color.BLACK);
        themeColor.put("chat_date", color);

        color = MaterialColors.getColor(context, R.attr.chat_muteIcon, Color.BLACK);
        themeColor.put("chat_muteIcon", color);

        color = MaterialColors.getColor(context, R.attr.chat_messageAction, Color.BLACK);
        themeColor.put("chat_messageAction", color);

        color = MaterialColors.getColor(context, R.attr.chat_inBubble, Color.BLACK);
        themeColor.put("chat_inBubble", color);

        color = MaterialColors.getColor(context, R.attr.chat_inBubbleSelected, Color.BLACK);
        themeColor.put("chat_inBubbleSelected", color);

        color = MaterialColors.getColor(context, R.attr.chat_outBubble, Color.BLACK);
        themeColor.put("chat_outBubble", color);

        color = MaterialColors.getColor(context, R.attr.chat_outBubbleGradient1, Color.BLACK);
        themeColor.put("chat_outBubbleGradient1", color);

        color = MaterialColors.getColor(context, R.attr.chat_outBubbleGradient2, Color.BLACK);
        themeColor.put("chat_outBubbleGradient2", color);

        color = MaterialColors.getColor(context, R.attr.chat_outBubbleGradient3, Color.BLACK);
        themeColor.put("chat_outBubbleGradient3", color);

        color = MaterialColors.getColor(context, R.attr.chat_outBubbleSelected, Color.BLACK);
        themeColor.put("chat_outBubbleSelected", color);

        color = MaterialColors.getColor(context, R.attr.chat_messageTextIn, Color.BLACK);
        themeColor.put("chat_messageTextIn", color);

        color = MaterialColors.getColor(context, R.attr.chat_messageTextOut, Color.BLACK);
        themeColor.put("chat_messageTextOut", color);

        color = MaterialColors.getColor(context, R.attr.chat_inTimeText, Color.BLACK);
        themeColor.put("chat_inTimeText", color);

        color = MaterialColors.getColor(context, R.attr.chat_outTimeText, Color.BLACK);
        themeColor.put("chat_outTimeText", color);

        color = MaterialColors.getColor(context, R.attr.chat_TextCursor, Color.BLACK);
        themeColor.put("chat_TextCursor", color);

        color = MaterialColors.getColor(context, R.attr.chat_TextCursorSelection, Color.BLACK);
        themeColor.put("chat_TextCursorSelection", color);

        color = MaterialColors.getColor(context, R.attr.radioBackground, Color.BLACK);
        themeColor.put("radioBackground", color);

        color = MaterialColors.getColor(context, R.attr.radioBackgroundChecked, Color.BLACK);
        themeColor.put("radioBackgroundChecked", color);

        color = MaterialColors.getColor(context, R.attr.switchTrack, Color.BLACK);
        themeColor.put("switchTrack", color);

        color = MaterialColors.getColor(context, R.attr.switchTrackChecked, Color.BLACK);
        themeColor.put("switchTrackChecked", color);

        color = MaterialColors.getColor(context, R.attr.checkboxBackground, Color.BLACK);
        themeColor.put("checkboxBackground", color);

        color = MaterialColors.getColor(context, R.attr.checkboxBackgroundChecked, Color.BLACK);
        themeColor.put("checkboxBackgroundChecked", color);

        color = MaterialColors.getColor(context, R.attr.button_TextColor, Color.BLACK);
        themeColor.put("button_TextColor", color);

        color = MaterialColors.getColor(context, R.attr.button_BackgroundGradient1, Color.BLACK);
        themeColor.put("button_BackgroundGradient1", color);

        color = MaterialColors.getColor(context, R.attr.button_BackgroundGradient2, Color.BLACK);
        themeColor.put("button_BackgroundGradient2", color);

        color = MaterialColors.getColor(context, R.attr.button_BackgroundGradient3, Color.BLACK);
        themeColor.put("button_BackgroundGradient3", color);

        color = MaterialColors.getColor(context, R.attr.dialogBackground, Color.BLACK);
        themeColor.put("dialogBackground", color);

        color = MaterialColors.getColor(context, R.attr.errorColor, Color.BLACK);
        themeColor.put("errorColor", color);
    }

    public static int getColor(String key) { return themeColor.get(key); }

    public static void addListener(IThemeManagerActivityListener listener) { listeners.add(listener); }
    public static void removeListener(IThemeManagerActivityListener listener) { listeners.remove(listener); }

    public static int getCurrentTheme() { return currentTheme; }
    public static void setTheme(int theme) { currentTheme = theme; }
}
