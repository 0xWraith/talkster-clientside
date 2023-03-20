package com.client.talkster.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>
{
    private final long userID;
    private final Context context;
    public List<Chat> chatList;

    public ChatListAdapter(long userID, List<Chat> chatList, Context context)
    {
        this.userID = userID;
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.component_chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position)
    {


        Chat chat = chatList.get(position);
        Log.d("1 ChatListAdapter", "onBindViewHolder: " + position + " " + chat.getId());

        if(chat.getReceiverID() == chat.getOwnerID())
        {
            Log.d("2 ChatListAdapter", "onBindViewHolder: " + chat.getReceiverID() + " " + chat.getOwnerID());
            holder.userNameText.setText(R.string.saved_messages);
            holder.userAvatarImage.setImageResource(R.drawable.img_favourites_chat);
        }
        else
        {
            Log.d("3 ChatListAdapter", "onBindViewHolder: " + chat.getReceiverID() + " " + chat.getOwnerID());
            holder.userAvatarImage.setImageResource(R.drawable.avatar_wireframe);
            holder.userNameText.setText(String.format(Locale.getDefault(), "%s %s", chat.getReceiverFirstname(), chat.getReceiverLastname()));
        }


        if(chat.getMessages().size() > 0)
        {
            Log.d("4 ChatListAdapter", "onBindViewHolder: " + chat.getMessages().size());
            Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);

            switch (lastMessage.getMessageType())
            {
                case TEXT_MESSAGE:
                {
                    Log.d("5 ChatListAdapter", "onBindViewHolder: " + lastMessage.getMessageType());
                    holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.previewSecondaryText));
                    holder.chatPreviewText.setText(String.format(Locale.getDefault(), "%s: %s", userID == lastMessage.getSenderID() ? "You" : chat.getReceiverFirstname(), lastMessage.getMessageContent()));
                    break;
                }
                case AUDIO_MESSAGE:
                {
                    holder.chatPreviewText.setText(R.string.audio_message);
                    holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.aurora_4));
                    break;
                }
                case MEDIA_MESSAGE:
                {
                    holder.chatPreviewText.setText(R.string.photo);
                    holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.aurora_4));
                    break;
                }
            }
        }
        else
        {
            Log.d("6 ChatListAdapter", "onBindViewHolder: " + chat.getMessages().size());
            holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.aurora_1));
            holder.chatPreviewText.setText(R.string.cleared_history);
        }
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    static class ChatViewHolder extends RecyclerView.ViewHolder
    {
        private final TextView userNameText;
        private final TextView chatPreviewText;
        private final ShapeableImageView userAvatarImage;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameText = itemView.findViewById(R.id.userNameText);
            chatPreviewText = itemView.findViewById(R.id.chatPreviewText);
            userAvatarImage = itemView.findViewById(R.id.userAvatarImage);
        }
    }
}
