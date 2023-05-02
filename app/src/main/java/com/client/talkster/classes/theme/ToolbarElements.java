package com.client.talkster.classes.theme;

import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ToolbarElements
{
    private View toolbar;
    private TextView toolbarTitle;
    private TextView toolbarSubtitle;
    private final List<ImageButton> toolbarIcons;

    public ToolbarElements()
    {
        toolbarIcons = new ArrayList<>();
    }

    public View getToolbar() { return toolbar; }
    public TextView getToolbarTitle() { return toolbarTitle; }
    public TextView getToolbarSubtitle() { return toolbarSubtitle; }
    public List<ImageButton> getToolbarIcons() { return toolbarIcons; }

    public void setToolbar(View toolbar) { this.toolbar = toolbar; }
    public void setToolbarTitle(TextView toolbarTitle) { this.toolbarTitle = toolbarTitle; }
    public void addToolbarIcon(ImageButton toolbarIcon) { this.toolbarIcons.add(toolbarIcon); }
    public void setToolbarSubtitle(TextView toolbarSubtitle) { this.toolbarSubtitle = toolbarSubtitle; }

}
