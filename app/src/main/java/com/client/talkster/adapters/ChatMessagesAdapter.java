package com.client.talkster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.Message;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IChatViewHolder;
import com.client.talkster.interfaces.IThemeManagerFragmentListener;
import com.client.talkster.utils.enums.MessageType;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IThemeManagerFragmentListener
{
    private final long ownerID;
    private final Context context;
    private final List<Message> messages;
    private final List<IChatViewHolder> chatViewHolderList;

    public ChatMessagesAdapter(List<Message> messages, long ownerID, Context context)
    {
        this.ownerID = ownerID;
        this.context = context;
        this.messages = messages;
        chatViewHolderList = new ArrayList<>();
    }

    public List<Message> getMessages() { return messages; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
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
                break;
            case 3:
                break;
        }
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
        } else {
            ((ChatMediaMessageViewHolder)chatViewHolder).chatMessageTime.setText(message.getOnlineTime());
            ((ChatMediaMessageViewHolder)chatViewHolder).chatFileButton.setText(message.getMessageContent().split(" ")[0]);
            ((ChatMediaMessageViewHolder)chatViewHolder).filename = message.getMessageContent().split(" ")[1];
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
        public ReceiverChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }

        @Override
        public void onThemeChanged()
        {
            chatMessageText.setBackground(ThemeManager.getReceiverChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_inTimeText"));
            chatMessageText.setTextColor(ThemeManager.getColor("chat_messageTextIn"));
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
}
