package com.client.talkster.adapters;

import java.util.List;
import java.util.Locale;
import android.view.View;
import com.client.talkster.R;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import androidx.annotation.NonNull;
import com.client.talkster.classes.Chat;
import com.client.talkster.classes.Message;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.imageview.ShapeableImageView;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder>
{
    private final long userID;
    public List<Chat> chatList;
    private final Context context;
    private final IChatClickListener IChatClickListener;

    public ChatListAdapter(long userID, List<Chat> chatList, Context context, IChatClickListener IChatClickListener)
    {
        this.userID = userID;
        this.context = context;
        this.chatList = chatList;
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
            holder.userAvatarImage.setImageResource(R.drawable.avatar_wireframe);
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
