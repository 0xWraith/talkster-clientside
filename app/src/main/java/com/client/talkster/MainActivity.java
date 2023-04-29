package com.client.talkster;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.controllers.IntroductionScreenActivity;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.VerifiedUserDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IMainActivityScreen;
import com.client.talkster.utils.UserAccountManager;
import com.client.talkster.utils.Utils;
import com.client.talkster.utils.enums.EThemeType;
import com.client.talkster.utils.exceptions.ThemeNotFoundException;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements IMainActivityScreen, IAPIResponseHandler
{
    private final int SPLASH_DISPLAY_LENGTH = 750;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        createApplicationThemes();
        setTheme(ThemeManager.getCurrentThemeStyle());

        super.onCreate(savedInstanceState);

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        UserAccountManager.getAccount(this);
    }

    @Override
    public void showHomeScreen(VerifiedUserDTO verifiedUserDTO)
    {
        new Handler().postDelayed(() -> {

            UserAccount userAccount = UserAccount.getInstance();

            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            userAccount.setUser(verifiedUserDTO.getUser());
            userAccount.setUserJWT(verifiedUserDTO.getUserJWT());

            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

    public void showOfflineScreen()
    {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    public void showIntroductionScreen()
    {
       new Handler().postDelayed(() ->
       {
            Intent intent = new Intent(this, IntroductionScreenActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
            finish();

        }, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {
        showOfflineScreen();
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
        try
        {
            if(response.body() == null)
                throw new IOException("Unexpected response " + response);

            int responseCode = response.code();
            String responseBody = response.body().string();

            if(responseCode != 200)
            {
                Utils.saveToSharedPreferences(this, "account_data", "");
                runOnUiThread(this::showIntroductionScreen);
                return;
            }

            VerifiedUserDTO verifiedUserDTO = new Gson().fromJson(responseBody, VerifiedUserDTO.class);

            runOnUiThread(() -> showHomeScreen(verifiedUserDTO));
        }
        catch (IOException e) { e.printStackTrace(); }
        catch (IllegalStateException | JsonSyntaxException exception) { Log.e("Talkster", "Failed to parse JWT token: " + exception.getMessage()); }
    }

    private void createApplicationThemes()
    {
        Theme theme;

        if(ThemeManager.getThemes().size() == 0) 
        {

            theme = new Theme("Dark Forest",
                    EThemeType.THEME_NIGHT,
                    R.style.Theme_Talkster_First,
                    R.color.theme1_color,
                    R.color.theme1_in_bubble,
                    R.color.theme1_out_bubble1,
                    R.color.theme1_out_bubble2,
                    R.color.theme1_out_bubble3,
                    R.drawable.bg_chat_forest_blur);

            ThemeManager.addTheme(theme);

            theme = new Theme("Dark Amethyst",
                    EThemeType.THEME_NIGHT,
                    R.style.Theme_Talkster_Second,
                    R.color.theme2_color,
                    R.color.theme2_in_bubble,
                    R.color.theme2_out_bubble1,
                    R.color.theme2_out_bubble2,
                    R.color.theme2_out_bubble3,
                    R.drawable.bg_chat_dark_amethyst);


            ThemeManager.addTheme(theme);


            theme = new Theme("Light Amethyst",
                    EThemeType.THEME_DAY,
                    R.style.Theme_Talkster_Third,
                    R.color.theme3_color,
                    R.color.theme3_in_bubble,
                    R.color.theme3_out_bubble1,
                    R.color.theme3_out_bubble2,
                    R.color.theme3_out_bubble3,
                    R.drawable.bg_chat_light_amethyst);

            ThemeManager.addTheme(theme);
        }

        try
        {
            String themeName = Utils.getFromSharedPreferences(this, "TalksterTheme");
            theme = ThemeManager.getThemeByName(themeName);
        }
        catch (IllegalStateException | ThemeNotFoundException exception) { theme = ThemeManager.getThemes().get(0); }

        ThemeManager.applyTheme(this, theme);
        ThemeManager.reloadThemeColors(this);
    }
}