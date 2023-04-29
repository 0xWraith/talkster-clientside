package com.client.talkster.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.User;
import com.client.talkster.classes.chat.Chat;
import com.client.talkster.classes.chat.GroupChat;
import com.client.talkster.classes.chat.message.Message;
import com.client.talkster.classes.chat.PrivateChat;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.interfaces.theme.IThemeManagerFragmentListener;
import com.client.talkster.utils.FileUtils;
import com.client.talkster.utils.enums.EChatType;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatViewHolder> implements IThemeManagerFragmentListener
{
    public List<Chat> chatList;
    public HashMap<Long, ChatViewHolder> viewHashMap = new HashMap<>();
    private final Context context;
    private final IRecyclerViewItemClickListener IRecyclerViewItemClickListener;
    private final FileUtils fileUtils;

    public ChatListAdapter(Context context, IRecyclerViewItemClickListener IRecyclerViewItemClickListener, FileUtils fileUtils)
    {
        this.context = context;
        this.fileUtils = fileUtils;
        this.chatList = new ArrayList<>();
        this.IRecyclerViewItemClickListener = IRecyclerViewItemClickListener;
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

        holder.chatMuteIcon.setVisibility(chat.isMuted() ? View.VISIBLE : View.GONE);

        if(chat.getType() == EChatType.PRIVATE_CHAT)
            privateChatBind(holder, (PrivateChat) chat);
        else
            groupChatBind(holder, (GroupChat) chat);
    }

    private void groupChatBind(ChatViewHolder holder, GroupChat chat)
    {
        holder.userNameText.setText(chat.getGroupName());
        holder.userAvatarImage.setImageResource(R.drawable.blank_profile);

        if(chat.getMessages().size() == 0)
        {
            holder.chatPreviewText.setText(R.string.group_created_chat_message);
            holder.chatPreviewText.setTextColor(ThemeManager.getColor("chat_messageAction"));

            return;
        }

        Message lastMessage = chat.getMessages().get(chat.getMessages().size() - 1);

        switch (lastMessage.getMessageType())
        {
            case TEXT_MESSAGE:
            {
                User sender = chat.getGroupMembers().stream().filter(user -> user.getId() == lastMessage.getSenderID()).findFirst().orElse(null);

                if(sender == null)
                {
                    holder.chatPreviewText.setText("No user");
                    holder.chatPreviewText.setTextColor(ThemeManager.getColor("chat_messageAction"));
                    return;
                }

                holder.chatPreviewText.setTextColor(ContextCompat.getColor(context, R.color.previewSecondaryText));
                holder.chatPreviewText.setText(String.format(Locale.getDefault(), "%s: %s", sender.getFirstname(), lastMessage.getMessageContent()));
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

    private void privateChatBind(ChatViewHolder holder, PrivateChat chat)
    {

        viewHashMap.put(chat.getReceiverID(), holder);

        Log.d("ChatListAdapter", "privateChatBind: " + chat);
        Log.d("ChatListAdapter", "privateChatBind: " + chat.getReceiverID() + " " + chat.getOwnerID());

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
        holder.chatPreviewText.setTextColor(ThemeManager.getColor("chat_messageAction"));
    }

    @Override
    public int getItemCount() { return chatList.size(); }

    @Override
    public void onThemeChanged()
    {
        for(ChatViewHolder viewHolder : viewHashMap.values())
        {
            viewHolder.userNameText.setTextColor(ThemeManager.getColor("chat_name"));
            viewHolder.chatMuteIcon.setColorFilter(ThemeManager.getColor("chat_muteIcon"));
            viewHolder.chatPreviewText.setTextColor(ThemeManager.getColor("chat_message"));
        }
    }

    public class ChatViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener
    {
        private final TextView userNameText;
        private final ImageView chatMuteIcon;
        private final TextView chatPreviewText;
        public final ShapeableImageView userAvatarImage;

        public ChatViewHolder(@NonNull View itemView)
        {
            super(itemView);

            chatMuteIcon = itemView.findViewById(R.id.muteIcon);
            userNameText = itemView.findViewById(R.id.userNameText);
            chatPreviewText = itemView.findViewById(R.id.chatPreviewText);
            userAvatarImage = itemView.findViewById(R.id.userAvatarImage);

            userNameText.setTextColor(ThemeManager.getColor("chat_name"));
            chatMuteIcon.setColorFilter(ThemeManager.getColor("chat_muteIcon"));
            chatPreviewText.setTextColor(ThemeManager.getColor("chat_message"));

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            int position = getAdapterPosition();

            if (position >= 0)
                IRecyclerViewItemClickListener.onItemClick(position, view);
        }

        @Override
        public boolean onLongClick(View view)
        {
            int position = getAdapterPosition();

            if (position >= 0)
            {
                IRecyclerViewItemClickListener.onItemLongClick(position, view);
                return true;
            }
            return false;
        }
    }
}
