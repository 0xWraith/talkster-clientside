package com.client.talkster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.client.talkster.classes.User;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.theme.SettingsElements;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IThemeManagerActivityListener;

public class SettingsActivity extends AppCompatActivity implements IActivity, View.OnClickListener, IThemeManagerActivityListener
{


    private ToolbarElements toolbarElements;
    private SettingsElements settingsElements;

    private ConstraintLayout subToolbar;
    private ConstraintLayout settingsLayout;

    private TextView loginText;
    private TextView biographyText;
    private TextView userNameText;
    private TextView userMailText;
    private TextView userStatusText;
    private ImageView userAvatarImage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        loadApplicationTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getUIElements();
    }

    @Override
    public void getUIElements()
    {
        ImageButton toolbarBackIcon;
        LinearLayout settingsBlockItem;
        User user = UserAccount.getInstance().getUser();

        toolbarElements = new ToolbarElements();
        settingsElements = new SettingsElements();

        loginText = findViewById(R.id.loginText);
        subToolbar = findViewById(R.id.subToolbar);
        userMailText = findViewById(R.id.userMailText);
        userNameText = findViewById(R.id.userNameText);
        biographyText = findViewById(R.id.biographyText);
        userStatusText = findViewById(R.id.userStatusText);
        settingsLayout = findViewById(R.id.settingsLayout);
        userAvatarImage = findViewById(R.id.userAvatarImage);
        toolbarBackIcon = findViewById(R.id.toolbarBackIcon);

        toolbarElements.addToolbarIcon(toolbarBackIcon);
        toolbarElements.setToolbar(findViewById(R.id.toolbar));
        toolbarElements.setToolbarTitle(findViewById(R.id.toolbarTitle));

        settingsElements.addSettingsText(loginText);
        settingsElements.addSettingsText(userMailText);
        settingsElements.addSettingsText(biographyText);

        settingsElements.addHeaderText(findViewById(R.id.headerText1));
        settingsElements.addHeaderText(findViewById(R.id.headerText2));
        settingsElements.addHeaderText(findViewById(R.id.headerText3));
        settingsElements.addHeaderText(findViewById(R.id.headerText4));

        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon1));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon2));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon3));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon4));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon5));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon6));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon7));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsIcon8));

        settingsElements.addSettingsText(findViewById(R.id.settingsText1));
        settingsElements.addSettingsText(findViewById(R.id.settingsText2));
        settingsElements.addSettingsText(findViewById(R.id.settingsText3));
        settingsElements.addSettingsText(findViewById(R.id.settingsText4));
        settingsElements.addSettingsText(findViewById(R.id.settingsText5));
        settingsElements.addSettingsText(findViewById(R.id.settingsText6));
        settingsElements.addSettingsText(findViewById(R.id.settingsText7));
        settingsElements.addSettingsText(findViewById(R.id.settingsText8));


        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock1));
        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock2));
        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock3));
        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock4));

        settingsElements.addSettingsSubText(findViewById(R.id.settingsSubText1));
        settingsElements.addSettingsSubText(findViewById(R.id.settingsSubText2));
        settingsElements.addSettingsSubText(findViewById(R.id.settingsSubText3));

        loginText.setText(user.getLogin());
        userMailText.setText(user.getMail());
        userNameText.setText(user.getFullName());
        userStatusText.setText(user.getStatus());
        biographyText.setText(user.getBiography());

        toolbarBackIcon.setOnClickListener(this);
        userAvatarImage.setImageBitmap(UserAccount.getInstance().getUser().getAvatar());

        settingsBlockItem = findViewById(R.id.settingsMailBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsUsernameBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsBiographyBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsChatBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsSecurityBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsNotificationBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsDevicesBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsLanguageBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsFAQBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsPolicyBlock);
        settingsBlockItem.setOnClickListener(this);

        settingsBlockItem = findViewById(R.id.settingsBugBlock);
        settingsBlockItem.setOnClickListener(this);
    }

    @Override
    public void getBundleElements()
    {

    }

    @Override
    public void onClick(View view)
    {
        int id = view.getId();

        if(id == R.id.toolbarBackIcon)
            finish();

        else if(id == R.id.settingsChatBlock)
        {
            Intent intent = new Intent(this, ChatSettingsActivity.class);
            startActivity(intent);
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        removeListener();
    }

    @Override
    public void removeListener() { ThemeManager.removeListener(this); }

    @Override
    public void onThemeChanged()
    {
        setTheme(ThemeManager.getCurrentThemeStyle());
        ThemeManager.reloadThemeColors(this);

        ThemeManager.changeToolbarColor(toolbarElements);
        ThemeManager.changeSettingsColor(settingsElements);

        subToolbar.setBackgroundColor(ThemeManager.getColor("actionBarDefault"));
        userNameText.setTextColor(ThemeManager.getColor("actionBarDefaultTitle"));
        userStatusText.setTextColor(ThemeManager.getColor("actionBarDefaultSubtitle"));
        settingsLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundGray"));
    }

    @Override
    public void loadApplicationTheme()
    {
        ThemeManager.addListener(this);
        setTheme(ThemeManager.getCurrentThemeStyle());
    }
}