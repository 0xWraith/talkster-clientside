package com.client.talkster.controllers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.client.talkster.MyApplication;
import com.client.talkster.R;
import com.client.talkster.classes.theme.ButtonElements;
import com.client.talkster.classes.theme.SettingsElements;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.interfaces.theme.IThemeManagerActivityListener;
import com.client.talkster.utils.Utils;
import com.client.talkster.utils.enums.EThemeType;
import com.client.talkster.utils.exceptions.ThemeNotFoundException;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ThemeManager
{
    private static Theme prevTheme = null;
    private static Theme currentTheme = null;
    private static final List<Theme> themes = new ArrayList<>();
    private static final HashMap<String, Integer> themeColor = new HashMap<>();

    private static final HashMap<String, Integer> themeDrawable = new HashMap<>();
    private static final List<IThemeManagerActivityListener> listeners = new ArrayList<>();

    private static final int[] DRAWABLES = {R.attr.inputBackground};

    private static GradientDrawable roundedButtonGradient;
    private static GradientDrawable standardButtonGradient;

    private static GradientDrawable senderChatBubbleGradient;
    private static GradientDrawable receiverChatBubbleGradient;

    public static void applyTheme(Context context, Theme theme)
    {
        currentTheme = theme;
        notifyListeners();

        Utils.saveToSharedPreferences(context, "TalksterTheme", theme.getName());
    }


    private static void notifyListeners()
    {
        for (IThemeManagerActivityListener listener : listeners)
            listener.onThemeChanged();
    }

    public static List<Theme> getThemes() { return themes; }
    public static Theme getCurrentTheme() { return currentTheme; }
    public static void addTheme(Theme theme) { themes.add(theme); }
    public static int getCurrentThemeStyle() { return currentTheme.getThemeStyle(); }
    public static void addListener(IThemeManagerActivityListener listener) { listeners.add(listener); }
    public static void removeListener(IThemeManagerActivityListener listener) { listeners.remove(listener); }
    public static EThemeType getThemeType() { return currentTheme == null ? EThemeType.THEME_NIGHT : currentTheme.getThemeType(); }


    public static GradientDrawable getChatSenderDrawable(int color1, int ignoredColor2, int color3)
    {
        GradientDrawable gradientDrawable = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] {color1, color3});

        gradientDrawable.setShape(GradientDrawable.RECTANGLE);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);

        return gradientDrawable;
    }

    public static GradientDrawable getChatReceiverDrawable(int chatInColor)
    {
        GradientDrawable drawable = new GradientDrawable();

        drawable.setColor(chatInColor);
        drawable.setShape(GradientDrawable.RECTANGLE);

        return drawable;
    }

    public static void reloadThemeColors(Context context)
    {

        if(prevTheme == currentTheme)
            return;

        prevTheme = currentTheme;

        themeColor.put("themeName", MaterialColors.getColor(context, R.attr.themeName, Color.BLACK));
        themeColor.put("windowBackgroundWhite", MaterialColors.getColor(context, R.attr.windowBackgroundWhite, Color.BLACK));
        themeColor.put("windowBackgroundGray", MaterialColors.getColor(context, R.attr.windowBackgroundGray, Color.BLACK));
        themeColor.put("actionBarDefault", MaterialColors.getColor(context, R.attr.actionBarDefault, Color.BLACK));
        themeColor.put("actionBarDefaultIcon", MaterialColors.getColor(context, R.attr.actionBarDefaultIcon, Color.BLACK));
        themeColor.put("actionBarDefaultTitle", MaterialColors.getColor(context, R.attr.actionBarDefaultTitle, Color.BLACK));
        themeColor.put("actionBarDefaultSubtitle", MaterialColors.getColor(context, R.attr.actionBarDefaultSubtitle, Color.BLACK));
        themeColor.put("navBarBackground", MaterialColors.getColor(context, R.attr.navBarBackground, Color.BLACK));
        themeColor.put("navBarHeaderBackground", MaterialColors.getColor(context, R.attr.navBarHeaderBackground, Color.BLACK));
        themeColor.put("navBarText", MaterialColors.getColor(context, R.attr.navBarText, Color.BLACK));
        themeColor.put("navBarSubText", MaterialColors.getColor(context, R.attr.navBarSubText, Color.BLACK));
        themeColor.put("navMenuDivider", MaterialColors.getColor(context, R.attr.navMenuDivider, Color.BLACK));
        themeColor.put("navBarIcon", MaterialColors.getColor(context, R.attr.navBarIcon, Color.BLACK));
        themeColor.put("navBarTabActiveIcon", MaterialColors.getColor(context, R.attr.navBarTabActiveIcon, Color.BLACK));
        themeColor.put("navBarTabUnactiveIcon", MaterialColors.getColor(context, R.attr.navBarTabUnactiveIcon, Color.BLACK));
        themeColor.put("radioBackground", MaterialColors.getColor(context, R.attr.radioBackground, Color.BLACK));
        themeColor.put("radioBackgroundChecked", MaterialColors.getColor(context, R.attr.radioBackgroundChecked, Color.BLACK));
        themeColor.put("switchTrack", MaterialColors.getColor(context, R.attr.switchTrack, Color.BLACK));
        themeColor.put("switchTrackChecked", MaterialColors.getColor(context, R.attr.switchTrackChecked, Color.BLACK));
        themeColor.put("checkboxBackground", MaterialColors.getColor(context, R.attr.checkboxBackground, Color.BLACK));
        themeColor.put("checkboxBackgroundChecked", MaterialColors.getColor(context, R.attr.checkboxBackgroundChecked, Color.BLACK));

        themeColor.put("dialogBackground", MaterialColors.getColor(context, R.attr.dialogBackground, Color.BLACK));
        themeColor.put("errorColor", MaterialColors.getColor(context, R.attr.errorColor, Color.RED));

        //Fetches an array of the resource attributes
        @SuppressLint("ResourceType")
        TypedArray a = context.getTheme().obtainStyledAttributes(DRAWABLES);
        try {
            // Get the integer value associated with the attribute by Index in DRAWABLES
            themeDrawable.put("inputBackground", a.getResourceId(0, 0));
        } finally {
            a.recycle();
        }



        loadChatColors(context);
        loadButtonColors(context);
        loadSettingsColors(context);

        refreshButtonGradient(context);
        refreshChatBubblesGradient(context);
    }

    private static void loadChatColors(Context context)
    {

        themeColor.put("chat_backgroundImage", R.attr.chat_backgroundImage);
        themeColor.put("chat_name", MaterialColors.getColor(context, R.attr.chat_name, Color.BLACK));
        themeColor.put("chat_message", MaterialColors.getColor(context, R.attr.chat_message, Color.BLACK));
        themeColor.put("chat_divider", MaterialColors.getColor(context, R.attr.chat_divider, Color.BLACK));
        themeColor.put("chat_mediaMessage", MaterialColors.getColor(context, R.attr.chat_mediaMessage, Color.BLACK));
        themeColor.put("chat_date", MaterialColors.getColor(context, R.attr.chat_date, Color.BLACK));
        themeColor.put("chat_muteIcon", MaterialColors.getColor(context, R.attr.chat_muteIcon, Color.BLACK));
        themeColor.put("chat_messageAction", MaterialColors.getColor(context, R.attr.chat_messageAction, Color.BLACK));
        themeColor.put("chat_inBubble", MaterialColors.getColor(context, R.attr.chat_inBubble, Color.BLACK));
        themeColor.put("chat_inBubbleSelected", MaterialColors.getColor(context, R.attr.chat_inBubbleSelected, Color.BLACK));
        themeColor.put("chat_outBubble", MaterialColors.getColor(context, R.attr.chat_outBubble, Color.BLACK));
        themeColor.put("chat_outBubbleGradient1", MaterialColors.getColor(context, R.attr.chat_outBubbleGradient1, Color.BLACK));
        themeColor.put("chat_outBubbleGradient2", MaterialColors.getColor(context, R.attr.chat_outBubbleGradient2, Color.BLACK));
        themeColor.put("chat_outBubbleGradient3", MaterialColors.getColor(context, R.attr.chat_outBubbleGradient3, Color.BLACK));
        themeColor.put("chat_outBubbleSelected", MaterialColors.getColor(context, R.attr.chat_outBubbleSelected, Color.BLACK));
        themeColor.put("chat_messageTextIn", MaterialColors.getColor(context, R.attr.chat_messageTextIn, Color.BLACK));
        themeColor.put("chat_messageTextOut", MaterialColors.getColor(context, R.attr.chat_messageTextOut, Color.BLACK));
        themeColor.put("chat_inTimeText", MaterialColors.getColor(context, R.attr.chat_inTimeText, Color.BLACK));
        themeColor.put("chat_outTimeText", MaterialColors.getColor(context, R.attr.chat_outTimeText, Color.BLACK));
        themeColor.put("chat_TextCursor", MaterialColors.getColor(context, R.attr.chat_TextCursor, Color.BLACK));
        themeColor.put("chat_TextCursorSelection", MaterialColors.getColor(context, R.attr.chat_TextCursorSelection, Color.BLACK));

        themeColor.put("chat_barIconColor", MaterialColors.getColor(context, R.attr.chat_barIconColor, Color.BLACK));
        themeColor.put("chat_barIconColorActive", MaterialColors.getColor(context, R.attr.chat_barIconColorActive, Color.BLACK));
        themeColor.put("chat_barBackground", MaterialColors.getColor(context, R.attr.chat_barBackground, Color.BLACK));
    }

    private static void loadButtonColors(Context context)
    {
        themeColor.put("button_textColor", MaterialColors.getColor(context, R.attr.button_textColor, Color.BLACK));
        themeColor.put("button_iconColor", MaterialColors.getColor(context, R.attr.button_iconColor, Color.BLACK));

        themeColor.put("button_BackgroundGradient1", MaterialColors.getColor(context, R.attr.button_BackgroundGradient1, Color.BLACK));
        themeColor.put("button_BackgroundGradient2", MaterialColors.getColor(context, R.attr.button_BackgroundGradient2, Color.BLACK));
        themeColor.put("button_BackgroundGradient3", MaterialColors.getColor(context, R.attr.button_BackgroundGradient3, Color.BLACK));
    }

    private static void refreshChatBubblesGradient(Context context)
    {
        int pxRadius = Utils.convertDPToPx(context, 15);

        receiverChatBubbleGradient = new GradientDrawable();
        senderChatBubbleGradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[] { getColor("chat_outBubbleGradient1"), /*getColor("chat_outBubbleGradient2"),*/ getColor("chat_outBubbleGradient3") });

        receiverChatBubbleGradient.setColor(getColor("chat_inBubble"));
        senderChatBubbleGradient.setCornerRadii(new float[] { pxRadius, pxRadius, pxRadius, pxRadius, 0, 0, pxRadius, pxRadius });
        receiverChatBubbleGradient.setCornerRadii(new float[] { pxRadius, pxRadius, pxRadius, pxRadius, pxRadius, pxRadius, 0, 0 });

    }


    private static void refreshButtonGradient(Context context)
    {
        roundedButtonGradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[] { getColor("button_BackgroundGradient1"),
//                        getColor("button_BackgroundGradient2"),
                        getColor("button_BackgroundGradient3") });

        standardButtonGradient = new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[] { getColor("button_BackgroundGradient1"),
//                        getColor("button_BackgroundGradient2"),
                        getColor("button_BackgroundGradient3") });

        roundedButtonGradient.setCornerRadius(Utils.convertDPToPx(context, 50));
        standardButtonGradient.setCornerRadius(Utils.convertDPToPx(context, 5));
    }

    private static void loadSettingsColors(Context context)
    {
        themeColor.put("settings_text", MaterialColors.getColor(context, R.attr.settings_text, Color.parseColor("#2F8C75")));
        themeColor.put("settings_subText", MaterialColors.getColor(context, R.attr.settings_subText, Color.parseColor("#2F8C75")));
        themeColor.put("settings_iconColor", MaterialColors.getColor(context, R.attr.settings_iconColor, Color.parseColor("#2F8C75")));
        themeColor.put("settings_actionTextColor", MaterialColors.getColor(context, R.attr.settings_actionTextColor, Color.parseColor("#2F8C75")));
    }

    public static int getColor(String key)
    {
        return themeColor.getOrDefault(key, Color.BLACK);
    }
    public static int getDrawable(String key) { return themeDrawable.getOrDefault(key, null); }

    public static GradientDrawable getRoundedButtonGradient() { return roundedButtonGradient; }
    public static GradientDrawable getStandardButtonGradient() { return standardButtonGradient; }
    public static GradientDrawable getSenderChatBubbleGradient() { return senderChatBubbleGradient; }
    public static GradientDrawable getReceiverChatBubbleGradient() { return receiverChatBubbleGradient; }

    public static void changeButtonsColor(ButtonElements buttonElements)
    {
        if(buttonElements == null)
            return;

        int i = 0;

        for (Button button: buttonElements.getButtons())
        {
            if(buttonElements.isButtonRounded(i))
                changeRoundedButtonColor(button);
            else
                changeStandardButtonColor(button);

            i++;
        }

        i = 0;

        for (ImageButton imageButton: buttonElements.getImageButtons())
        {
            if(buttonElements.isImageButtonRounded(i))
                changeRoundedButtonColor(imageButton);
            else
                changeStandardButtonColor(imageButton);

            i++;
        }
    }
    public static void changeStandardButtonColor(ImageButton button)
    {
        if(button == null)
            return;

        button.setColorFilter(ThemeManager.getColor("button_textColor"));
        button.setBackground(ThemeManager.getStandardButtonGradient());
    }
    public static void changeStandardButtonColor(Button button)
    {
        if(button == null)
            return;

        button.setTextColor(ThemeManager.getColor("button_textColor"));
        button.setBackground(ThemeManager.getStandardButtonGradient());
    }

    public static void changeRoundedButtonColor(ImageButton button)
    {
        if(button == null)
            return;

        button.setColorFilter(ThemeManager.getColor("button_textColor"));
        button.setBackground(ThemeManager.getRoundedButtonGradient());
    }

    public static void changeRoundedButtonColor(Button button)
    {
        if(button == null)
            return;

        button.setTextColor(ThemeManager.getColor("button_textColor"));
        button.setBackground(ThemeManager.getRoundedButtonGradient());
    }

    public static void changeToolbarColor(ToolbarElements toolbarElements)
    {
        if(toolbarElements == null)
            return;

        List<ImageButton> toolbarIcons = toolbarElements.getToolbarIcons();

        toolbarElements.getToolbar().setBackgroundColor(ThemeManager.getColor("actionBarDefault"));

        if(toolbarElements.getToolbarTitle() != null)
            toolbarElements.getToolbarTitle().setTextColor(ThemeManager.getColor("actionBarDefaultTitle"));

        if(toolbarElements.getToolbarSubtitle() != null)
            toolbarElements.getToolbarSubtitle().setTextColor(ThemeManager.getColor("actionBarDefaultSubtitle"));


        for (ImageButton toolbarIcon : toolbarIcons)
            toolbarIcon.setColorFilter(ThemeManager.getColor("actionBarDefaultIcon"));
    }

    public static void changeSettingsColor(SettingsElements settingsElements)
    {
        for (TextView settingsText : settingsElements.getSettingsTexts())
            settingsText.setTextColor(ThemeManager.getColor("settings_text"));

        for (TextView settingsSubText : settingsElements.getSettingsSubTexts())
            settingsSubText.setTextColor(ThemeManager.getColor("settings_subText"));

        for (ImageView settingsIcon : settingsElements.getSettingsIcons())
            settingsIcon.setColorFilter(ThemeManager.getColor("settings_iconColor"));

        for (TextView headerText : settingsElements.getHeaderTexts())
            headerText.setTextColor(ThemeManager.getColor("settings_actionTextColor"));

        for (RelativeLayout settingsBlock : settingsElements.getSettingsBlocks())
            settingsBlock.setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));
    }

    public static void changeInputColor(List<EditText> inputs) {
        for (EditText input : inputs) {
            input.setBackground(MyApplication.getAppContext().getResources().getDrawable(ThemeManager.getDrawable("inputBackground")));
            input.setTextColor(ThemeManager.getColor("settings_color"));
        }
    }

    public static void changeColorState(BottomNavigationView bottomNavigation)
    {
        int[] colors = new int[] {
                ThemeManager.getColor("navBarTabUnactiveIcon"),
                ThemeManager.getColor("navBarTabActiveIcon")
        };

        ColorStateList newColorStateList = new ColorStateList(new int[][] { new int[] { -android.R.attr.state_checked }, new int[] { android.R.attr.state_checked } }, colors);
        bottomNavigation.setItemIconTintList(newColorStateList);
        bottomNavigation.setBackgroundColor(ThemeManager.getColor("navBarBackground"));
    }

    public static Drawable getThemeImage(Context context)
    {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.chat_backgroundImage, typedValue, true);
        return AppCompatResources.getDrawable(context, typedValue.resourceId);
    }

    public static Theme getThemeByName(String themeName) throws ThemeNotFoundException
    {
        for(Theme theme : themes)
            if(theme.getName().equals(themeName))
                return theme;

        throw new ThemeNotFoundException("Theme not found");
    }
}
