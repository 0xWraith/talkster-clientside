package com.client.talkster;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ChatMessagesAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.PrivateChatActionDTO;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IBroadcastRegister;
import com.client.talkster.interfaces.IThemeManagerActivityListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.enums.MessageType;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.OffsetDateTime;

import okhttp3.Call;
import okhttp3.Response;

public class PrivateChatActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IBroadcastRegister, IThemeManagerActivityListener
{
    private Chat chat;
    private UserJWT userJWT;
    private ImageView chatMuteIcon;
    private Menu privateChatActionMenu;
    private TextView userNameText, userStatusText;
    private ImageButton chatSendButton, backButton, mediaButton, closeMediaButton;
    private Button galleryButton, cameraButton;
    private EditText chatInputText;
    private RecyclerView chatMessagesList;
    private View chatView;
    private BroadcastReceiver messageReceiver;
    private ShapeableImageView userAvatarImage;
    private ChatMessagesAdapter chatMessagesAdapter;
    private LinearLayoutManager recyclerLayoutManager;
    private ConstraintLayout mediaChooserLayout;
    private ImageButton menuButton;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        ThemeManager.addListener(this);

        getBundleElements();
        getUIElements();
        registerBroadCasts();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void getUIElements()
    {
        chatMuteIcon = findViewById(R.id.muteIcon);
        menuButton = findViewById(R.id.menuButton);
        userNameText = findViewById(R.id.userNameText);
        chatInputText = findViewById(R.id.chatInputText);
        chatSendButton = findViewById(R.id.chatSendButton);
        backButton = findViewById(R.id.backButton);
        mediaButton = findViewById(R.id.mediaButton);
        closeMediaButton = findViewById(R.id.closeMediaButton);
        galleryButton = findViewById(R.id.galleryButton);
        cameraButton = findViewById(R.id.cameraButton);
        userStatusText = findViewById(R.id.userStatusText);
        chatMessagesList = findViewById(R.id.chatMessagesList);
        chatView = findViewById(R.id.chatView);
        userAvatarImage = findViewById(R.id.circularBackground);
        recyclerLayoutManager = (LinearLayoutManager)chatMessagesList.getLayoutManager();
        mediaChooserLayout = findViewById(R.id.mediaChooserLayout);

        if(userJWT.getID() == chat.getReceiverID())
        {
            userNameText.setText("Saved messages");
            userAvatarImage.setImageResource(R.drawable.img_favourites_chat);

        }
        else {
            userNameText.setText(chat.getReceiverName());
            userAvatarImage.setImageBitmap(new FileUtils(userJWT).getProfilePicture(chat.getReceiverID()));
        }

        userStatusText.setText("last seen at 12:35");

        chatMessagesAdapter = new ChatMessagesAdapter(chat.getMessages(), userJWT.getID(), this);
        chatMessagesList.setAdapter(chatMessagesAdapter);

        chatSendButton.setOnClickListener(view ->
        {
            String message = chatInputText.getText().toString().trim();

            int length = message.length();

            if(length == 0 || length > 4097)
                return;

            MessageDTO messageDTO = new MessageDTO();

            chatInputText.setText("");
            messageDTO.setchatid(chat.getId());
            messageDTO.setmessagecontent(message);
            messageDTO.setsenderid(userJWT.getID());
            messageDTO.setjwttoken(userJWT.getAccessToken());
            messageDTO.setreceiverid(chat.getReceiverID());
            messageDTO.setmessagetype(MessageType.TEXT_MESSAGE);
            messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

            Intent intent = new Intent(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
            intent.putExtra(BundleExtraNames.CHAT_SEND_MESSAGE_BUNDLE, messageDTO);
            sendBroadcast(intent);
        });

        backButton.setOnClickListener(view -> finish());
        closeMediaButton.setOnClickListener(view -> mediaChooserLayout.animate().translationY(0).setDuration(250));
        mediaButton.setOnClickListener(view -> mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250));

        Context wrapper = new ContextThemeWrapper(this, R.style.PrivateChat_PopupMenu);
        PopupMenu popup = new PopupMenu(wrapper, menuButton);

        privateChatActionMenu = popup.getMenu();
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_private_chat, privateChatActionMenu);

        popup.setOnMenuItemClickListener(item ->
        {
            int id = item.getItemId();

            if(id == R.id.clearHistoryItemID)
                return showActionDialog(EPrivateChatAction.CLEAR_CHAT_HISTORY);

            else if(id == R.id.deleteChatItemID)
                return showActionDialog(EPrivateChatAction.DELETE_CHAT);

            else if(id == R.id.muteForItemID)
                return showMutePopupWindow();

            else if(id == R.id.unmuteItemID)
                return muteForTime(0);

            else if(id == R.id.muteForeverItemID)
                return muteForTime(-1);


            return false;
        });

        menuButton.setOnClickListener(view ->
        {
            popup.show();
        });

        cameraButton.setOnClickListener(view -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PrivateChatActivity.this)
                    .cameraOnly()
                    .start(101);
        });

        galleryButton.setOnClickListener(view -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            ImagePicker.Companion.with(PrivateChatActivity.this)
                    .galleryOnly()
                    .start(101);
        });

        chatView.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            public boolean onTouch(View v, MotionEvent event) {
                // ... Respond to touch events
                mediaChooserLayout.animate().translationY(0).setDuration(250);
                return false;
            }
        });
        updateChatMuteUI();
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

        APIHandler<PrivateChatActionDTO, PrivateChatActivity> apiHandler = new APIHandler<>(this);

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
        APIHandler<PrivateChatActionDTO, PrivateChatActivity> apiHandler = new APIHandler<>(this);
        apiHandler.apiPOST(APIEndpoints.TALKSTER_API_CHAT_ACTION, new PrivateChatActionDTO(EPrivateChatAction.MUTE_CHAT, chat.getId(), userJWT.getID(), seconds), userJWT.getAccessToken());
        return true;
    }

    private void addMessage(Message message)
    {
        chatMessagesAdapter.getMessages().add(message);
        chatMessagesAdapter.notifyItemInserted(chatMessagesAdapter.getItemCount() - 1);
        recyclerLayoutManager.scrollToPositionWithOffset(chatMessagesAdapter.getItemCount() - 1,0);
    }

    private boolean showActionDialog(EPrivateChatAction action)
    {
        Dialog dialog;
        Button cancelButton;
        Button confirmButton;
        CheckBox confirmCheckBox;
        TextView confirmationTextView;
        APIHandler<PrivateChatActionDTO, PrivateChatActivity> apiHandler;

        dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_private_chat_action);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        cancelButton = dialog.findViewById(R.id.cancelButton);
        confirmButton = dialog.findViewById(R.id.confirmButton);
        confirmCheckBox = dialog.findViewById(R.id.confirmCheckBox);
        confirmationTextView = dialog.findViewById(R.id.confirmationTextView);

        cancelButton.setOnClickListener(view -> dialog.dismiss());

        apiHandler = new APIHandler<>(this);

        confirmButton.setOnClickListener(view ->
        {
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

    private void updateChatMuteUI()
    {
        if(chat.isMuted())
        {
            chatMuteIcon.setVisibility(View.VISIBLE);
            privateChatActionMenu.findItem(R.id.muteItemID).setVisible(false);
            privateChatActionMenu.findItem(R.id.unmuteItemID).setVisible(true);
            return;
        }
        chatMuteIcon.setVisibility(View.INVISIBLE);
        privateChatActionMenu.findItem(R.id.muteItemID).setVisible(true);
        privateChatActionMenu.findItem(R.id.unmuteItemID).setVisible(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        unregisterBroadCasts();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        chat = (Chat) bundle.get(BundleExtraNames.USER_CHAT);
        userJWT = (UserJWT) bundle.get(BundleExtraNames.USER_JWT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            Uri uri = data.getData();
            sendProfileImage(uri);

        } else if (resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
        }
    }

    private void sendProfileImage(Uri uri){
        FileContent fileContent = new FileContent();
        APIHandler<FileContent, PrivateChatActivity> apiHandler = new APIHandler<>(this);
        try {
            ContentResolver cr = getContentResolver();
            fileContent.setContent(FileUtils.getBytes(uri, cr));
            fileContent.setType(FileUtils.getType(uri, cr));
            fileContent.setFilename(FileUtils.getFilename(uri, cr));
            apiHandler.apiMultipartPOST(APIEndpoints.TALKSTER_API_FILE_UPLOAD,fileContent, userJWT.getAccessToken());
        } catch (IOException e){
            System.out.println("This Exception was thrown inside the sendImage method");
            e.printStackTrace();
        }
    }

    @Override
    public void onFailure(@NonNull Call call, @NonNull IOException exception, @NonNull String apiUrl)
    {
        Intent intent = new Intent(this, OfflineActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        startActivity(intent);
        finish();
    }

    @Override
    public void onResponse(@NonNull Call call, @NonNull Response response, @NonNull String apiUrl)
    {
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


                switch(action)
                {
                    case CLEAR_CHAT_HISTORY:

                        int messageCount = chatMessagesAdapter.getItemCount();

                        chat.clearMessages();
                        chatMessagesAdapter.getMessages().clear();
                        runOnUiThread(() -> chatMessagesAdapter.notifyItemRangeRemoved(0, messageCount));
                        break;

                    case DELETE_CHAT:
                        finish();
                        break;

                    case MUTE_CHAT:
                        messageDTO = new MessageDTO();
                        messageDTO.createActionMessage(EPrivateChatAction.MUTE_CHAT, userJWT.getID(), privateChatActionDTO.getMuteTime(), userJWT.getAccessToken());

                        chat.setMuteTime(privateChatActionDTO.getMuteTime());
                        runOnUiThread(() -> {
                            updateChatMuteUI();
                            Toast.makeText(this, privateChatActionDTO.getMuteTime() == 0 ? "Unmuted" : "Muted", Toast.LENGTH_SHORT).show();
                        });
                        break;
                }
                intent.putExtra(BundleExtraNames.CHAT_ACTION_MESSAGE_DATA, messageDTO);
                sendBroadcast(intent);
            }
        }
        catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void registerBroadCasts()
    {
        messageReceiver = new BroadcastReceiver()
        {
            @Override
            public void onReceive(Context context, Intent intent)
            {

                String action = intent.getAction();
                Bundle bundle = intent.getExtras();

                if (action.equals(BundleExtraNames.CHAT_PRIVATE_MESSAGE_RECEIVED))
                {

                    if ((long) bundle.get(BundleExtraNames.CHAT_ID) != chat.getId())
                        return;

                    Message message = (Message) bundle.get(BundleExtraNames.CHAT_SEND_MESSAGE_BUNDLE);

                    switch (message.getMessageType())
                    {
                        case TEXT_MESSAGE:
                        case AUDIO_MESSAGE:
                        case MEDIA_MESSAGE:
                            addMessage(message);
                            break;

                        case CLEAR_CHAT_HISTORY:
                        {
                            int size = chatMessagesAdapter.getMessages().size();

                            chat.clearMessages();
                            chatMessagesAdapter.getMessages().clear();
                            chatMessagesAdapter.notifyItemRangeRemoved(0, size);
                            break;
                        }
                        case DELETE_CHAT:
                        {
                            finish();
                            break;
                        }
                    }
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(BundleExtraNames.CHAT_PRIVATE_MESSAGE_RECEIVED);

        registerReceiver(messageReceiver, filter);
    }

    @Override
    public void unregisterBroadCasts()
    {
        unregisterReceiver(messageReceiver);
    }

    @Override
    public void onThemeChanged()
    {
        Log.d("Theme", "onThemeChanged: " + ThemeManager.getCurrentTheme());
    }
}