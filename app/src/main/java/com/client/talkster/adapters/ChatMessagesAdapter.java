package com.client.talkster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.Message;
import com.client.talkster.interfaces.IActivity;

import java.util.List;

public class ChatMessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private final long ownerID;
    private final Context context;
    private final List<Message> messages;

    public ChatMessagesAdapter(List<Message> messages, long ownerID, Context context)
    {
        this.ownerID = ownerID;
        this.context = context;
        this.messages = messages;
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

    private abstract class ChatMessageViewHolder extends RecyclerView.ViewHolder
    {
        protected TextView chatMessageText;
        public ChatMessageViewHolder(@NonNull View itemView)
        {
            super(itemView);
            chatMessageText = itemView.findViewById(R.id.chatMessageText);
        }
    }

    class SenderChatMessagesViewHolder extends ChatMessageViewHolder
    {
        public SenderChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }

    class ReceiverChatMessagesViewHolder extends ChatMessageViewHolder
    {
        public ReceiverChatMessagesViewHolder(@NonNull View itemView)
        {
            super(itemView);
        }
    }
}
