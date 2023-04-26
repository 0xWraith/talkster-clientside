package com.client.talkster.classes.theme;

import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SettingsElements
{
    private final List<TextView> headerTexts;
    private final List<TextView> settingsTexts;
    private final List<TextView> settingsSubTexts;
    private final List<ImageView> settingsIcons;
    private final List<RelativeLayout> settingsBlocks;

    public SettingsElements()
    {
        headerTexts = new ArrayList<>();
        settingsIcons = new ArrayList<>();
        settingsTexts = new ArrayList<>();
        settingsBlocks = new ArrayList<>();
        settingsSubTexts = new ArrayList<>();
    }

    public List<TextView> getHeaderTexts() { return headerTexts; }
    public List<TextView> getSettingsTexts() { return settingsTexts; }
    public List<ImageView> getSettingsIcons() { return settingsIcons; }
    public List<TextView> getSettingsSubTexts() { return settingsSubTexts; }
    public List<RelativeLayout> getSettingsBlocks() { return settingsBlocks; }

    public void addHeaderText(TextView headerText) { this.headerTexts.add(headerText); }
    public void addSettingsText(TextView settingsText) { this.settingsTexts.add(settingsText); }
    public void addSettingsIcon(ImageView settingsIcon) { this.settingsIcons.add(settingsIcon); }
    public void addSettingsBlock(RelativeLayout settingsBlock) { this.settingsBlocks.add(settingsBlock); }
    public void addSettingsSubText(TextView settingsSubText) { this.settingsSubTexts.add(settingsSubText); }

}
