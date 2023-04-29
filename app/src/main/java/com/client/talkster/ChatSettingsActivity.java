package com.client.talkster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ThemeListAdapter;
import com.client.talkster.classes.theme.CubicBezierInterpolator;
import com.client.talkster.classes.theme.SettingsElements;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.interfaces.theme.IThemeManagerActivityListener;
import com.client.talkster.utils.enums.EThemeType;

public class ChatSettingsActivity extends AppCompatActivity implements IActivity, IThemeManagerActivityListener
{
    private boolean BLOCK_TOUCH = false;
    private ToolbarElements toolbarElements;
    private SettingsElements settingsElements;

    private ThemeListAdapter themeAdapter;
    private ImageView themeTransitionImage;
    private ConstraintLayout chatSettingsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        loadApplicationTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        getUIElements();
    }

    @Override
    public void loadApplicationTheme()
    {
        ThemeManager.addListener(this);
        setTheme(ThemeManager.getCurrentThemeStyle());
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        removeListener();
    }

    @Override
    public void getUIElements()
    {
        RecyclerView themeSelector;
        ImageButton toolbarBackButton;

        toolbarElements = new ToolbarElements();
        settingsElements = new SettingsElements();

        themeSelector = findViewById(R.id.themeSelector);
        toolbarBackButton = findViewById(R.id.toolbarBackButton);
        chatSettingsLayout = findViewById(R.id.chatSettingsLayout);
        themeTransitionImage = findViewById(R.id.themeTransitionImage);

        toolbarElements.setToolbar(findViewById(R.id.toolbar));
        toolbarElements.setToolbarTitle(findViewById(R.id.toolbarTitle));
        toolbarElements.addToolbarIcon(toolbarBackButton);


        settingsElements.addHeaderText(findViewById(R.id.headerText2));
        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock1));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsModeIcon));
        settingsElements.addSettingsText(findViewById(R.id.settingsModeText));

        themeAdapter = new ThemeListAdapter(this, new IRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClick(int position, View v)
            {
                if(BLOCK_TOUCH)
                    return;

                Theme theme = themeAdapter.getThemeList().get(position);

                if(theme == ThemeManager.getCurrentTheme())
                    return;

                Animator anim;
                Bitmap bitmap;
                Canvas canvas;

                int w = chatSettingsLayout.getMeasuredWidth();
                int h = chatSettingsLayout.getMeasuredHeight();
                EThemeType previousThemeType = ThemeManager.getThemeType();

                BLOCK_TOUCH = true;
                bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(bitmap);

                chatSettingsLayout.draw(canvas);
                themeTransitionImage.setImageBitmap(bitmap);
                themeTransitionImage.setVisibility(View.VISIBLE);

                ThemeManager.applyTheme(ChatSettingsActivity.this, themeAdapter.getThemeList().get(position));

                if(previousThemeType == EThemeType.THEME_NIGHT && ThemeManager.getThemeType() == EThemeType.THEME_DAY)
                    anim = ViewAnimationUtils.createCircularReveal(themeTransitionImage, 0, 0, (float) Math.hypot(w, h), 0f);

                else if(previousThemeType == EThemeType.THEME_DAY && ThemeManager.getThemeType() == EThemeType.THEME_NIGHT)
                    anim = ViewAnimationUtils.createCircularReveal(themeTransitionImage, w, h, (float) Math.hypot(w, h), 0);

                else
                    anim = ViewAnimationUtils.createCircularReveal(themeTransitionImage, w / 2, 0, (float) Math.hypot(w, h), 0);


                anim.setDuration(400);
                anim.setInterpolator(CubicBezierInterpolator.EASE_IN_OUT_QUAD);

                anim.addListener(new AnimatorListenerAdapter()
                {
                    @Override
                    public void onAnimationEnd(Animator animation)
                    {
                        BLOCK_TOUCH = false;
                        themeTransitionImage.setImageDrawable(null);
                        themeTransitionImage.setVisibility(View.GONE);
                    }
                });
                anim.start();
            }

            @Override
            public void onItemLongClick(int position, View v) { }
        });

        themeSelector.setAdapter(themeAdapter);
        toolbarBackButton.setOnClickListener(v -> finish());

        themeAdapter.getThemeList().addAll(ThemeManager.getThemes());
        themeAdapter.notifyDataSetChanged();
    }

    @Override
    public void getBundleElements()
    {

    }

    @Override
    public void removeListener() { ThemeManager.removeListener(this); }

    @Override
    public void onThemeChanged()
    {
        setTheme(ThemeManager.getCurrentThemeStyle());
        ThemeManager.reloadThemeColors(this);

        chatSettingsLayout.setBackgroundColor(ThemeManager.getColor("windowBackgroundGray"));

        ThemeManager.changeToolbarColor(toolbarElements);
        ThemeManager.changeSettingsColor(settingsElements);
    }
}