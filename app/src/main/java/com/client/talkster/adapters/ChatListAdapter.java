package com.client.talkster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>
{
    public List<Chat> chatList;
    private final Context context;
    private final IChatClickListener IChatClickListener;

    public ChatListAdapter(Context context, IChatClickListener IChatClickListener)
    {
        this.context = context;
        this.chatList = new ArrayList<>();
        this.IChatClickListener = IChatClickListener;
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

        if(chat.getReceiverID() == chat.getOwnerID())
        {
            holder.userNameText.setText(R.string.saved_messages);
            holder.userAvatarImage.setImageResource(R.drawable.img_favourites_chat);
        }
        else
        {
            holder.userAvatarImage.setImageResource(R.drawable.account_circle_64);
            holder.userNameText.setText(chat.getReceiverName());
        }

        holder.chatMuteIcon.setVisibility(chat.isMuted() ? View.VISIBLE : View.GONE);

        if(chat.getMessages().size() > 0)
        {
            Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);

            switch (lastMessage.getMessageType())
            {
                case TEXT_MESSAGE:
                {
                    holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.previewSecondaryText));
                    holder.chatPreviewText.setText(lastMessage.getMessageContent());
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
            return;
        }
        holder.chatPreviewText.setText(R.string.cleared_history);
        holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.aurora_1));
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        private final TextView userNameText;
        private final ImageView chatMuteIcon;
        private final TextView chatPreviewText;
        private final ShapeableImageView userAvatarImage;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            chatMuteIcon = itemView.findViewById(R.id.muteIcon);
            userNameText = itemView.findViewById(R.id.userNameText);
            chatPreviewText = itemView.findViewById(R.id.chatPreviewText);
            userAvatarImage = itemView.findViewById(R.id.userAvatarImage);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            int position = getAdapterPosition();

            if (position >= 0)
                IChatClickListener.onItemClick(position, view);
        }

        @Override
        public boolean onLongClick(View view)
        {
            int position = getAdapterPosition();

            if (position >= 0)
            {
                IChatClickListener.onItemLongClick(position, view);
                return true;
            }
            return false;
        }
    }
    public interface IChatClickListener
    {
        void onItemClick(int position, View v);
        void onItemLongClick(int position, View v);
    }
}
