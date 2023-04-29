package com.client.talkster;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.adapters.ChatMessagesAdapter;
import com.client.talkster.api.APIEndpoints;
import com.client.talkster.api.APIHandler;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.FileContent;
import com.client.talkster.classes.Message;
import com.client.talkster.classes.UserAccount;
import com.client.talkster.classes.UserJWT;
import com.client.talkster.classes.theme.ToolbarElements;
import com.client.talkster.controllers.OfflineActivity;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.dto.FileDTO;
import com.client.talkster.dto.MessageDTO;
import com.client.talkster.dto.PrivateChatActionDTO;
import com.client.talkster.interfaces.IAPIResponseHandler;
import com.client.talkster.interfaces.IActivity;
import com.client.talkster.interfaces.IBroadcastRegister;
import com.client.talkster.interfaces.IThemeManagerActivityListener;
import com.client.talkster.utils.BundleExtraNames;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EPrivateChatAction;
import com.client.talkster.utils.enums.MessageType;
import com.client.talkster.utils.exceptions.UserUnauthorizedException;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.gson.Gson;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Response;

public class PrivateChatActivity extends AppCompatActivity implements IActivity, IAPIResponseHandler, IBroadcastRegister, IThemeManagerActivityListener
{
    private Chat chat;
    private ToolbarElements toolbarElements;
    private BroadcastReceiver messageReceiver;
    private ChatMessagesAdapter chatMessagesAdapter;


    private View chatView, chatInputView;
    private ImageView chatMuteIcon;
    private EditText chatInputText;
    private Menu privateChatActionMenu;
    private ShapeableImageView userAvatarImage;
    private Button galleryButton, cameraButton;
    private TextView userNameText, userStatusText;
    private LinearLayoutManager recyclerLayoutManager;
    private ConstraintLayout chatLayout, mediaChooserLayout;
    private ImageButton chatSendButton, toolbarBackButton, mediaButton, closeMediaButton, toolbarMenuIcon;

    private final String[] PERMISSIONS = {
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.CAMERA
    };

    private final String[] ALLOWED_TYPES = {
            "image/png",
            "image/jpg",
            "image/jpeg"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        loadApplicationTheme();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_private_chat);

        getBundleElements();
        getUIElements();
        registerBroadCasts();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void getUIElements()
    {
        RecyclerView chatMessagesList;

        toolbarElements = new ToolbarElements();
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();

        chatView = findViewById(R.id.chatMessageView);
        chatLayout = findViewById(R.id.chatLayout);
        chatMuteIcon = findViewById(R.id.muteIcon);
        mediaButton = findViewById(R.id.mediaButton);
        userNameText = findViewById(R.id.userNameText);
        cameraButton = findViewById(R.id.cameraButton);
        chatInputView = findViewById(R.id.chatInputView);
        chatInputText = findViewById(R.id.chatInputText);
        galleryButton = findViewById(R.id.galleryButton);
        userStatusText = findViewById(R.id.userStatusText);
        chatSendButton = findViewById(R.id.chatSendButton);
        toolbarMenuIcon = findViewById(R.id.toolbarMenuIcon);
        chatMessagesList = findViewById(R.id.chatMessagesList);
        closeMediaButton = findViewById(R.id.closeMediaButton);
        userAvatarImage = findViewById(R.id.profileImage);
        toolbarBackButton = findViewById(R.id.toolbarBackButton);
        mediaChooserLayout = findViewById(R.id.mediaChooserLayout);
        recyclerLayoutManager = (LinearLayoutManager) chatMessagesList.getLayoutManager();

        toolbarElements.setToolbarTitle(userNameText);
        toolbarElements.addToolbarIcon(toolbarMenuIcon);
        toolbarElements.addToolbarIcon(toolbarBackButton);
        toolbarElements.setToolbarSubtitle(userStatusText);
        toolbarElements.setToolbar(findViewById(R.id.toolbar));

        if(userJWT.getID() == chat.getReceiverID())
        {
            userNameText.setText(R.string.saved_messages);
            userAvatarImage.setImageResource(R.drawable.img_favourites_chat);

        }
        else
        {
            userNameText.setText(chat.getReceiverName());
            userAvatarImage.setImageBitmap(new FileUtils().getProfilePicture(chat.getReceiverID()));
        }

        userStatusText.setText(String.format(Locale.getDefault(), getString(R.string.chat_last_seen), "12:35"));

        chatMessagesAdapter = new ChatMessagesAdapter(chat.getMessages(), userJWT.getID(), this);
        chatMessagesList.setAdapter(chatMessagesAdapter);

        chatInputText.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                changeSendButtonColor(charSequence.length() > 0);
            }

            @Override
            public void afterTextChanged(Editable editable) { }
        });

        chatSendButton.setOnClickListener(view ->
        {
            String message = chatInputText.getText().toString().trim();
            sendMessage(message, MessageType.TEXT_MESSAGE);

        });

        toolbarBackButton.setOnClickListener(view -> finish());
        closeMediaButton.setOnClickListener(view -> mediaChooserLayout.animate().translationY(0).setDuration(250));
        mediaButton.setOnClickListener(view -> {
            if (!checkPermissions()) {
                ActivityCompat.requestPermissions(PrivateChatActivity.this, PERMISSIONS, 1);
            }
            else {
                mediaChooserLayout.animate().translationY(-(mediaChooserLayout.getHeight())).setDuration(250);
            }
        });

        toolbarMenuIcon.setOnClickListener(view -> {
            Intent intent = new Intent(this, ChatSettingsActivity.class);
            intent.putExtra(BundleExtraNames.USER_CHAT, chat);
            startActivityForResult(intent, 6);
        });

//        Context wrapper = new ContextThemeWrapper(this, R.style.PrivateChat_PopupMenu);
//        PopupMenu popup = new PopupMenu(wrapper, toolbarMenuIcon);

//        privateChatActionMenu = popup.getMenu();
//        MenuInflater inflater = popup.getMenuInflater();
//        inflater.inflate(R.menu.menu_private_chat, privateChatActionMenu);

//        popup.setOnMenuItemClickListener(item ->
//        {
//            int id = item.getItemId();
//
//            if(id == R.id.clearHistoryItemID)
//                return showActionDialog(EPrivateChatAction.CLEAR_CHAT_HISTORY);
//
//            else if(id == R.id.deleteChatItemID)
//                return showActionDialog(EPrivateChatAction.DELETE_CHAT);
//
//            else if(id == R.id.muteForItemID)
//                return showMutePopupWindow();
//
//            else if(id == R.id.unmuteItemID)
//                return muteForTime(0);
//
//            else if(id == R.id.muteForeverItemID)
//                return muteForTime(-1);
//
//            else if(id == R.id.changeThemeItemID)
//            {
//                Intent intent = new Intent(this, ChatSettingsActivity.class);
//                intent.putExtra(BundleExtraNames.USER_CHAT, chat);
//                startActivityForResult(intent, 6);
//
//                return false;
//            }
//
//
//            return false;
//        });
//
//        toolbarMenuIcon.setOnClickListener(view -> popup.show());

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
                    .galleryMimeTypes(ALLOWED_TYPES)
                    .start(101);
        });

        chatView.setOnTouchListener((v, event) -> {
            mediaChooserLayout.animate().translationY(0).setDuration(250);
            return false;
        });
        updateChatMuteUI();
    }

    private void changeSendButtonColor(boolean change)
    {
        if(!change)
            chatSendButton.setColorFilter(ThemeManager.getColor("chat_barIconColor"));
        else
            chatSendButton.setColorFilter(ThemeManager.getColor("chat_messageAction"));

    }

    private void sendMessage(String message, MessageType messageType) {
        int length = message.length();

        if(length == 0 || length > 4097)
            return;

        UserJWT userJWT = UserAccount.getInstance().getUserJWT();
        MessageDTO messageDTO = new MessageDTO();

        if (messageType == MessageType.TEXT_MESSAGE) {
            chatInputText.setText("");
        }
        messageDTO.setchatid(chat.getId());
        messageDTO.setmessagecontent(message);
        messageDTO.setsenderid(userJWT.getID());
        messageDTO.setjwttoken(userJWT.getAccessToken());
        messageDTO.setreceiverid(chat.getReceiverID());
        messageDTO.setmessagetype(messageType);
        messageDTO.setmessagetimestamp(OffsetDateTime.now().toString());

        Intent intent = new Intent(BundleExtraNames.CHAT_SEND_MESSAGE_BROADCAST);
        intent.putExtra(BundleExtraNames.CHAT_SEND_MESSAGE_BUNDLE, messageDTO);
        sendBroadcast(intent);
    }

    private boolean checkPermissions() {
        int permReadExt = ActivityCompat.checkSelfPermission(PrivateChatActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permWriteExt = ActivityCompat.checkSelfPermission(PrivateChatActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permCamera = ActivityCompat.checkSelfPermission(PrivateChatActivity.this, Manifest.permission.CAMERA);

        if (permReadExt != PackageManager.PERMISSION_GRANTED
                || permWriteExt != PackageManager.PERMISSION_GRANTED
                || permCamera != PackageManager.PERMISSION_GRANTED) {
            return false;
        } else {
            return true;
        }
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
        UserJWT userJWT = UserAccount.getInstance().getUserJWT();
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

    private void updateChatMuteUI()
    {
        if(chat.isMuted())
        {
            chatMuteIcon.setVisibility(View.VISIBLE);
//            privateChatActionMenu.findItem(R.id.muteItemID).setVisible(false);
//            privateChatActionMenu.findItem(R.id.unmuteItemID).setVisible(true);
            return;
        }
        chatMuteIcon.setVisibility(View.INVISIBLE);
//        privateChatActionMenu.findItem(R.id.muteItemID).setVisible(true);
//        privateChatActionMenu.findItem(R.id.unmuteItemID).setVisible(false);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        removeListener();
        unregisterBroadCasts();
    }

    @Override
    public void getBundleElements()
    {
        Bundle bundle = getIntent().getExtras();

        if(bundle.isEmpty())
            return;

        chat = (Chat) bundle.get(BundleExtraNames.USER_CHAT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                Uri uri = data.getData();
                sendImage(uri);
            } else if (resultCode == ImagePicker.RESULT_ERROR) {
                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Task Cancelled", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == Activity.RESULT_OK && requestCode == 6) {
            boolean isDeleted = data.getBooleanExtra("isDeleted", false);
            boolean isCleared = data.getBooleanExtra("isCleared", false);
            chat = (Chat) data.getSerializableExtra(BundleExtraNames.USER_CHAT);
            if (isDeleted) {finish(); return;}
            if (isCleared) {clearChats();}
            updateChatMuteUI();
        }
    }

    private void sendImage(Uri uri){
        FileContent fileContent = new FileContent();
        APIHandler<FileContent, PrivateChatActivity> apiHandler = new APIHandler<>(this);
        try {
            ContentResolver cr = getContentResolver();
            fileContent.setContent(FileUtils.getBytes(uri, cr));
            fileContent.setType(FileUtils.getType(uri, cr));
            fileContent.setFilename(FileUtils.getFilename(uri, cr));
            apiHandler.apiMultipartPOST(APIEndpoints.TALKSTER_API_FILE_UPLOAD,fileContent, UserAccount.getInstance().getUserJWT().getAccessToken());
        } catch (IOException e){
            System.out.println("This Exception was thrown inside the sendImage method");
            e.printStackTrace();
        }
    }

    private void clearChats() {
        int size = chatMessagesAdapter.getMessages().size();

        chat.clearMessages();
        chatMessagesAdapter.getMessages().clear();
        chatMessagesAdapter.notifyItemRangeRemoved(0, size);
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

                response.close();
            }
            else if (apiUrl.contains(APIEndpoints.TALKSTER_API_FILE_UPLOAD)) {
                if(responseCode != 200) {
                    throw new UserUnauthorizedException("Unexpected message");
                }
                if(response.body() == null)
                    throw new IOException("Unexpected response " + response);

                String responseBody = response.body().string();
                FileDTO fileDTO = new Gson().fromJson(responseBody, FileDTO.class);

                sendMessage(fileDTO.getFilename(), MessageType.MEDIA_MESSAGE);
            }
        }
        catch (IOException | UserUnauthorizedException e) { e.printStackTrace(); }
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
                            clearChats();
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
    public void removeListener()
    {
        ThemeManager.removeListener(this);
    }

    @Override
    public void onThemeChanged()
    {
        setTheme(ThemeManager.getCurrentThemeStyle());
        ThemeManager.reloadThemeColors(this);

        chatMessagesAdapter.onThemeChanged();

        ThemeManager.changeToolbarColor(toolbarElements);

        chatLayout.setBackground(ThemeManager.getThemeImage(this));
        chatMuteIcon.setColorFilter(ThemeManager.getColor("chat_muteIcon"));
        chatInputView.setBackgroundColor(ThemeManager.getColor("chat_barBackground"));
        mediaButton.setColorFilter(ThemeManager.getColor("chat_barIconColor"));

        changeSendButtonColor(chatInputText.getText().length() > 0);
    }

    @Override
    public void loadApplicationTheme()
    {
        ThemeManager.addListener(this);
        setTheme(ThemeManager.getCurrentThemeStyle());
    }
}