package com.client.talkster.adapters;

import android.content.Context;
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
import com.client.talkster.utils.FileUtils;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>
{
    public List<Chat> chatList;
    private final Context context;
    private final IChatClickListener IChatClickListener;
    private final FileUtils fileUtils;

    public ChatListAdapter(Context context, IChatClickListener IChatClickListener, FileUtils fileUtils)
    {
        this.context = context;
        this.chatList = new ArrayList<>();
        this.IChatClickListener = IChatClickListener;
        this.fileUtils = fileUtils;
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
            //holder.userAvatarImage.setImageBitmap(fileUtils.getProfilePicture(chat.getReceiverID()));
            holder.userAvatarImage.setImageResource(R.drawable.img_favourites_chat);
        }
        else
        {
            holder.userAvatarImage.setImageBitmap(fileUtils.getProfilePicture(chat.getReceiverID()));
            holder.userNameText.setText(chat.getReceiverName());
        }


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
        holder.chatPreviewText.setText(context.getString(R.string.empty_chat, chat.getReceiverFirstname()));
        holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.aquamarine_dark));
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
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
