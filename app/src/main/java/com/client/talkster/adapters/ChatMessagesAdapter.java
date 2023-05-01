package com.client.talkster.adapters;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.User;
import com.client.talkster.classes.chat.message.Message;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IChatViewHolder;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.MessageType;
import com.client.talkster.interfaces.chat.IGroupChatGetMessageSender;
import com.client.talkster.interfaces.theme.IThemeManagerFragmentListener;
import com.client.talkster.utils.enums.EChatType;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.github.muddz.styleabletoast.StyleableToast;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IThemeManagerFragmentListener
{
    private final long ownerID;
    private final EChatType type;
    private final Context context;
    private final List<Message> messages;
    private final List<IChatViewHolder> chatViewHolderList;
    private final String[] PERMISSIONS = {
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    
    private IGroupChatGetMessageSender groupChatGetMessageSender;

    public ChatMessagesAdapter(List<Message> messages, long ownerID, EChatType type, Context context)
    {
        this.type = type;
        this.ownerID = ownerID;
        this.context = context;
        this.messages = messages;
        chatViewHolderList = new ArrayList<>();
        groupChatGetMessageSender = null;
    }

    public List<Message> getMessages() { return messages; }

    public void setGroupChatGetMessageSender(IGroupChatGetMessageSender groupChatGetMessageSender) { this.groupChatGetMessageSender = groupChatGetMessageSender; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(type == EChatType.PRIVATE_CHAT)
        {
            View view;
            switch(viewType) {
                case 0:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_message_sender, parent, false);
                    return new SenderChatMessagesViewHolder(view);
                case 1:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_message_receiver, parent, false);
                    return new ReceiverChatMessagesViewHolder(view);
                case 2:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_media_message_sender, parent, false);
                    return new ReceiverChatMediaMessagesViewHolder(view);
                case 3:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_media_message_receiver, parent, false);
                    return new ReceiverChatMediaMessagesViewHolder(view);
                default:
                    return null;
            }
        }
        else if(type == EChatType.GROUP_CHAT)
        {
            View view;
            switch(viewType) {
                case 0:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_message_sender, parent, false);
                    return new SenderChatMessagesViewHolder(view);
                case 1:
                    view = LayoutInflater.from(context).inflate(R.layout.component_group_chat_message_receiver, parent, false);
                    return new ReceiverChatMessagesViewHolder(view);
                case 2:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_media_message_sender, parent, false);
                    return new SenderChatMediaMessagesViewHolder(view);
                case 3:
                    view = LayoutInflater.from(context).inflate(R.layout.component_chat_media_message_receiver, parent, false);
                    return new ReceiverChatMediaMessagesViewHolder(view);
                default:
                    return null;
            }
        }
        else
            return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Message message = messages.get(position);

        IChatViewHolder chatViewHolder = (IChatViewHolder) holder;
        chatViewHolderList.add(chatViewHolder);

        if(messages.get(position).getMessageType() == MessageType.TEXT_MESSAGE) {
            ((ChatMessageViewHolder)chatViewHolder).chatMessageTime.setText(message.getOnlineTime());
            ((ChatMessageViewHolder)chatViewHolder).chatMessageText.setText(message.getMessageContent());

            if(type == EChatType.GROUP_CHAT && chatViewHolder instanceof ReceiverChatMessagesViewHolder && ((ReceiverChatMessagesViewHolder) chatViewHolder).chatMessageUsername != null)
            {
                User sender = groupChatGetMessageSender.getMessageSender(message);
                ((ReceiverChatMessagesViewHolder) chatViewHolder).chatMessageUsername.setText(sender.getFullName());
                FileUtils fileUtils = new FileUtils();
                ((ReceiverChatMessagesViewHolder) chatViewHolder).userAvatarImage.setImageBitmap(fileUtils.getProfilePicture(sender.getId()));
            }
        } else {
            ((ChatMediaMessageViewHolder) chatViewHolder).chatMessageTime.setText(message.getOnlineTime());
            ((ChatMediaMessageViewHolder) chatViewHolder).filename = message.getMessageContent();
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if(messages.get(position).getMessageType() == MessageType.TEXT_MESSAGE) {
            if(messages.get(position).getSenderID() == ownerID)
                return 0;
            return 1;
        } else {
            if(messages.get(position).getSenderID() == ownerID)
                return 2;
            return 3;
        }

    }

    @Override
    public int getItemCount() { return messages.size(); }

    @Override
    public void onThemeChanged()
    {
        for (IChatViewHolder chatViewHolder : chatViewHolderList)
            chatViewHolder.onThemeChanged();
    }

    private abstract class ChatMessageViewHolder extends RecyclerView.ViewHolder implements IChatViewHolder
    {
        protected TextView chatMessageText;
        protected TextView chatMessageTime;

        public ChatMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            chatMessageText = itemView.findViewById(R.id.chatMessageText);
            chatMessageTime = itemView.findViewById(R.id.chatMessageTime);
        }
        @Override
        public abstract void onThemeChanged();
    }

    private abstract class ChatMediaMessageViewHolder extends RecyclerView.ViewHolder implements IChatViewHolder
    {
        protected View chatMessageView;
        protected Button chatFileButton;
        protected TextView chatMessageTime;
        protected String filename;
        public ChatMediaMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            chatMessageView = itemView.findViewById(R.id.chatMessageView);
            chatFileButton = itemView.findViewById(R.id.chatFileButton);
            chatMessageTime = itemView.findViewById(R.id.chatMessageTime);

            chatFileButton.setOnClickListener(view -> new Thread(() -> saveFile(filename)).start());

        }
        @Override
        public abstract void onThemeChanged();
    }

    class SenderChatMessagesViewHolder extends ChatMessageViewHolder
    {
        public SenderChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }

        @Override
        public void onThemeChanged()
        {
            chatMessageText.setBackground(ThemeManager.getSenderChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_outTimeText"));
            chatMessageText.setTextColor(ThemeManager.getColor("chat_messageTextOut"));
        }
    }

    class ReceiverChatMessagesViewHolder extends ChatMessageViewHolder
    {
        protected TextView chatMessageUsername;
        protected ImageView userAvatarImage;

        public ReceiverChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);

            if(type == EChatType.GROUP_CHAT)
                chatMessageUsername = itemView.findViewById(R.id.chatMessageUsername);
                userAvatarImage = itemView.findViewById(R.id.userAvatarImage);
        }


        @Override
        public void onThemeChanged()
        {
            chatMessageText.setBackground(ThemeManager.getReceiverChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_inTimeText"));
            chatMessageText.setTextColor(ThemeManager.getColor("chat_messageTextIn"));

            if(chatMessageUsername != null)
                chatMessageUsername.setTextColor(ThemeManager.getColor("chat_messageAction"));
        }
    }

    class SenderChatMediaMessagesViewHolder extends ChatMediaMessageViewHolder
    {
        public SenderChatMediaMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }

        @Override
        public void onThemeChanged()
        {
            chatMessageView.setBackground(ThemeManager.getSenderChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_outTimeText"));
            chatFileButton.setTextColor(ThemeManager.getColor("chat_messageTextOut"));
        }
    }

    class ReceiverChatMediaMessagesViewHolder extends ChatMediaMessageViewHolder
    {
        public ReceiverChatMediaMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }

        @Override
        public void onThemeChanged()
        {
            chatMessageView.setBackground(ThemeManager.getReceiverChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_inTimeText"));
            chatFileButton.setTextColor(ThemeManager.getColor("chat_messageTextIn"));
        }
    }

    private void saveFile(String filename) {
        int permWriteExt = ActivityCompat.checkSelfPermission(context.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permWriteExt != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context, PERMISSIONS, 1);
        } else {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Talkster");
            OutputStream outputStream;

            if (!dir.exists()) {
                dir.mkdir();
            }

            Bitmap image = new FileUtils().downloadImage(filename);
            if (image == null) {
                return;
            }

            File file = new File(dir, filename.split("\\.")[0] + ".jpg");
            try {
                outputStream = new FileOutputStream(file);
                image.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            MediaScannerConnection.scanFile(context, new String[] { file.getPath() }, new String[] { "image/jpeg" }, null);
            ((Activity)context).runOnUiThread(() -> StyleableToast.makeText(context.getApplicationContext(), "Image Downloaded!", R.style.customToast).show());
        }
    }
}
