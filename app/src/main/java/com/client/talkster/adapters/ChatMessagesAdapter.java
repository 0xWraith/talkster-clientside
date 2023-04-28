package com.client.talkster.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.Message;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IThemeManagerFragmentListener;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements IThemeManagerFragmentListener
{
    private final long ownerID;
    private final Context context;
    private final List<Message> messages;
    private final List<ChatMessageViewHolder> chatMessageViewHolderList;

    public ChatMessagesAdapter(List<Message> messages, long ownerID, Context context)
    {
        this.ownerID = ownerID;
        this.context = context;
        this.messages = messages;
        chatMessageViewHolderList = new ArrayList<>();
    }

    public List<Message> getMessages() { return messages; }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if(viewType == 0)
        {
            View view = LayoutInflater.from(context).inflate(R.layout.component_chat_message_sender, parent, false);
            return new SenderChatMessagesViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(context).inflate(R.layout.component_chat_message_receiver, parent, false);
            return new ReceiverChatMessagesViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position)
    {
        Message message = messages.get(position);

        ChatMessageViewHolder chatMessageViewHolder = (ChatMessageViewHolder) holder;

        chatMessageViewHolderList.add(chatMessageViewHolder);
        chatMessageViewHolder.chatMessageTime.setText(message.getOnlineTime());
        chatMessageViewHolder.chatMessageText.setText(message.getMessageContent());
    }


    @Override
    public int getItemViewType(int position)
    {
        if(messages.get(position).getSenderID() == ownerID)
            return 0;

        return 1;
    }

    @Override
    public int getItemCount() { return messages.size(); }

    @Override
    public void onThemeChanged()
    {
        for (ChatMessageViewHolder chatMessageViewHolder : chatMessageViewHolderList)
            chatMessageViewHolder.onThemeChanged();
    }

    private abstract class ChatMessageViewHolder extends RecyclerView.ViewHolder
    {
        protected TextView chatMessageText;
        protected TextView chatMessageTime;
        public ChatMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            chatMessageText = itemView.findViewById(R.id.chatMessageText);
            chatMessageTime = itemView.findViewById(R.id.chatMessageTime);
        }
        abstract void onThemeChanged();
    }

    class SenderChatMessagesViewHolder extends ChatMessageViewHolder
    {
        public SenderChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }

        @Override
        void onThemeChanged()
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
        void onThemeChanged()
        {
            chatMessageText.setBackground(ThemeManager.getReceiverChatBubbleGradient());
            chatMessageTime.setTextColor(ThemeManager.getColor("chat_inTimeText"));
            chatMessageText.setTextColor(ThemeManager.getColor("chat_messageTextIn"));
        }
    }
}
