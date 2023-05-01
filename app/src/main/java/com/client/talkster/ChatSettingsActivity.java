package com.client.talkster;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ThemeListAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.chat.PrivateChat;
import com.client.talkster.classes.theme.CubicBezierInterpolator;
import com.client.talkster.classes.theme.SettingsElements;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.PrivateChatActionDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.interfaces.theme.IThemeManagerActivityListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.enums.EThemeType;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class ChatSettingsActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IThemeManagerActivityListener
{
    private PrivateChat chat;
    private boolean isDeleted = false, isCleared = false;
    private boolean BLOCK_TOUCH = false;
    private ToolbarElements toolbarElements;
    private SettingsElements settingsElements;
    private ThemeListAdapter themeAdapter;
    private ImageView themeTransitionImage, profileImage;
    private ConstraintLayout chatSettingsLayout;

    private LinearLayout unmuteBlock, muteForeverBlock, muteForBlock, clearHistoryBlock, deleteChatBlock;
    private TextView profileText;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        loadApplicationTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_settings);

        getBundleElements();
        getUIElements();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        chat = (PrivateChat) bundle.get(BundleExtraNames.USER_CHAT);
    }

    private void sendResult() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isDeleted", isDeleted);
        resultIntent.putExtra("isCleared", isCleared);
        resultIntent.putExtra(BundleExtraNames.USER_CHAT, chat);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        sendResult();
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

        unmuteBlock = findViewById(R.id.unmuteBlock);
        muteForeverBlock = findViewById(R.id.muteForeverBlock);
        muteForBlock = findViewById(R.id.muteForBlock);
        clearHistoryBlock = findViewById(R.id.clearHistoryBlock);
        deleteChatBlock = findViewById(R.id.deleteChatBlock);

        profileText = findViewById(R.id.profileText);
        profileImage = findViewById(R.id.profileImage);

        toolbarElements.setToolbar(findViewById(R.id.toolbar));
        toolbarElements.setToolbarTitle(findViewById(R.id.toolbarTitle));
        toolbarElements.addToolbarIcon(toolbarBackButton);

        settingsElements.addHeaderText(findViewById(R.id.headerText2));
        settingsElements.addHeaderText(findViewById(R.id.headerText3));

        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock1));
        settingsElements.addSettingsIcon(findViewById(R.id.settingsModeIcon));
        settingsElements.addSettingsText(findViewById(R.id.settingsModeText));

        settingsElements.addSettingsBlock(findViewById(R.id.settingsBlock2));
        settingsElements.addSettingsText(findViewById(R.id.unmuteText));
        settingsElements.addSettingsText(findViewById(R.id.muteForeverText));
        settingsElements.addSettingsText(findViewById(R.id.muteForText));
        settingsElements.addSettingsText(findViewById(R.id.clearHistoryText));
        settingsElements.addSettingsText(findViewById(R.id.deleteChatText));
        settingsElements.addSettingsIcon(findViewById(R.id.unmuteIcon));
        settingsElements.addSettingsIcon(findViewById(R.id.muteForeverIcon));
        settingsElements.addSettingsIcon(findViewById(R.id.muteForIcon));
        settingsElements.addSettingsIcon(findViewById(R.id.clearHistoryIcon));
        settingsElements.addSettingsIcon(findViewById(R.id.deleteChatIcon));

        profileText.setText(chat.getReceiverName());
        profileImage.setImageBitmap(FileUtils.circleCrop(new FileUtils().getProfilePicture(chat.getReceiverID())));

        unmuteBlock.setOnClickListener(view -> {
            muteForTime(0);
        });
        muteForeverBlock.setOnClickListener(view -> {
            muteForTime(-1);
        });
        muteForBlock.setOnClickListener(view -> {
            showMutePopupWindow();
        });
        clearHistoryBlock.setOnClickListener(view -> {
            showActionDialog(EPrivateChatAction.CLEAR_CHAT_HISTORY);
        });
        deleteChatBlock.setOnClickListener(view -> {
            showActionDialog(EPrivateChatAction.DELETE_CHAT);
        });

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
        toolbarBackButton.setOnClickListener(v -> sendResult());

        themeAdapter.getThemeList().addAll(ThemeManager.getThemes());
        themeAdapter.notifyDataSetChanged();
    }

    private boolean showMutePopupWindow()
    {

        Button confirmButton;
        NumberPicker numberPicker;

        String[] timeIntervals = {
                "30 minutes",
                "1 hour", "2 hours", "4 hours", "8 hours",
                "1 day", "2 days", "3 days", "4 days", "5 days", "6 days",
                "1 week", "2 weeks", "3 weeks",
                "1 month", "3 months",
                "1 year"};

        int[] timeIntervalsSeconds = {
                1800,
                3600, 7200, 14400, 28800,
                86400, 172800, 259200, 345600, 432000, 518400,
                604800, 1209600, 1814400,
                2592000, 7776000,
                31536000};

        APIHandler<PrivateChatActionDTO, ChatSettingsActivity> apiHandler = new APIHandler<>(this);

        View popupView = getLayoutInflater().inflate(R.layout.popup_mute_timer_action, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
//        popupWindow.setAnimationStyle(R.style.PopupAnimation);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 0);

        confirmButton = popupView.findViewById(R.id.confirmButton);
        numberPicker = popupView.findViewById(R.id.muteTimePicker);

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(timeIntervals.length - 1);
        numberPicker.setDisplayedValues(timeIntervals);

        confirmButton.setOnClickListener(view ->
        {
            popupWindow.dismiss();
            muteForTime(timeIntervalsSeconds[numberPicker.getValue()]);
        });

        return true;
    }

    private boolean muteForTime(int seconds)
    {
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();
        APIHandler<PrivateChatActionDTO, ChatSettingsActivity> apiHandler = new APIHandler<>(this);
        apiHandler.apiPOST(APIEndpoints.TALKSTER_API_CHAT_ACTION, new PrivateChatActionDTO(EPrivateChatAction.MUTE_CHAT, chat.getId(), userJWT.getID(), seconds), userJWT.getAccessToken());

        return true;
    }

    private boolean showActionDialog(EPrivateChatAction action)
    {
        Dialog dialog;
        Button cancelButton;
        Button confirmButton;
        CheckBox confirmCheckBox;
        TextView confirmationTextView;
        APIHandler<PrivateChatActionDTO, ChatSettingsActivity> apiHandler;

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_private_chat_action);
        Window window = dialog.getWindow();
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        cancelButton = dialog.findViewById(R.id.cancelButton);
        confirmButton = dialog.findViewById(R.id.confirmButton);
        confirmCheckBox = dialog.findViewById(R.id.confirmCheckBox);
        confirmationTextView = dialog.findViewById(R.id.confirmationTextView);

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        apiHandler = new APIHandler<>(this);

        confirmButton.setOnClickListener(view ->
        {
            UserJWT userJWT = UserAccount.getInstance().getUserJWT();
            apiHandler.apiPOST(APIEndpoints.TALKSTER_API_CHAT_ACTION, new PrivateChatActionDTO(action, chat.getId(), userJWT.getID(), chat.getReceiverID(), confirmCheckBox.isChecked()), userJWT.getAccessToken());
            dialog.dismiss();
        });


        if(action == EPrivateChatAction.CLEAR_CHAT_HISTORY)
        {
            confirmButton.setText(R.string.clear);
            confirmCheckBox.setText(String.format(getString(R.string.clear_history_both), chat.getReceiverName()));
            confirmationTextView.setText(String.format(getString(R.string.clear_history_confirm), chat.getReceiverName()));
        }
        else if(action == EPrivateChatAction.DELETE_CHAT)
        {
            confirmButton.setText(R.string.delete_chat);
            confirmCheckBox.setText(String.format(getString(R.string.delete_chat_both), chat.getReceiverName()));
            confirmationTextView.setText(String.format(getString(R.string.delete_chat_confirm), chat.getReceiverName()));
        }

        dialog.show();
        return true;
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
        findViewById(R.id.profileBlock).setBackgroundColor(ThemeManager.getColor("windowBackgroundWhite"));
        profileText.setTextColor(ThemeManager.getColor("navBarText"));
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl) {
        try
        {
            int responseCode = response.code();

            if(apiUrl.equals(APIEndpoints.TALKSTER_API_CHAT_ACTION))
            {
                if(responseCode != 200)
                    return;

                if(response.body() == null)
                    throw new IOException("Unexpected response " + response);

                String responseBody = response.body().string();
                PrivateChatActionDTO privateChatActionDTO = new Gson().fromJson(responseBody, PrivateChatActionDTO.class);

                if(privateChatActionDTO == null || privateChatActionDTO.getOwnerChatID() != chat.getId())
                    throw new IOException("Unexpected response " + response);

                MessageDTO messageDTO = null;
                UserJWT userJWT = UserAccount.getInstance().getUserJWT();
                EPrivateChatAction action = privateChatActionDTO.getAction();
                Intent intent = new Intent(BundleExtraNames.CHAT_ACTION_BROADCAST);

                if(privateChatActionDTO.getActionForBoth())
                {
                    messageDTO = new MessageDTO();
                    messageDTO.setchatid(privateChatActionDTO.getReceiverChatID());
                    messageDTO.createActionMessage(action, userJWT.getID(), chat.getReceiverID(), userJWT.getAccessToken());
                }

                intent.putExtra(BundleExtraNames.CHAT_ACTION_TYPE, action);
                intent.putExtra(BundleExtraNames.CHAT_ACTION_CHAT_ID, chat.getId());
                intent.putExtra(BundleExtraNames.CHAT_TYPE, chat.getType());


                switch(action)
                {
                    case CLEAR_CHAT_HISTORY:
                        //TODO: signal history clear
                        isCleared = true;
                        break;

                    case DELETE_CHAT:
                        //TODO: signal chat delete
                        isDeleted = true;
                        sendResult();
                        break;

                    case MUTE_CHAT:
                        //TODO signal mute
                        messageDTO = new MessageDTO();
                        messageDTO.createActionMessage(EPrivateChatAction.MUTE_CHAT, userJWT.getID(), privateChatActionDTO.getMuteTime(), userJWT.getAccessToken());

                        chat.setMuteTime(privateChatActionDTO.getMuteTime());
                        runOnUiThread(() -> {
                            Toast.makeText(this, privateChatActionDTO.getMuteTime() == 0 ? "Unmuted" : "Muted", Toast.LENGTH_SHORT).show();
                        });
                        break;
                }
                intent.putExtra(BundleExtraNames.CHAT_ACTION_MESSAGE_DATA, messageDTO);
                sendBroadcast(intent);

                response.close();
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl) {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }
}