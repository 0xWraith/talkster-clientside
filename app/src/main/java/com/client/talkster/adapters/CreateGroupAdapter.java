package com.client.talkster.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.User;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.HashMap;
import java.util.List;

public class CreateGroupAdapter extends RecyclerView.Adapter<CreateGroupAdapter.ContactViewHolder>
{
    private final Context context;
    private final List<User> contactList;
    private final HashMap<Integer, ContactViewHolder> viewHashMap;
    private IRecyclerViewItemClickListener IRecyclerViewItemClickListener;

    public CreateGroupAdapter(Context context, List<User> contactList, IRecyclerViewItemClickListener IRecyclerViewItemClickListener)
    {
        this.context = context;
        this.contactList = contactList;
        this.viewHashMap = new HashMap<>();
        this.IRecyclerViewItemClickListener = IRecyclerViewItemClickListener;
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.component_contacts, parent, false);
        return new CreateGroupAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position)
    {
        User user = contactList.get(position);

        viewHashMap.put(position, holder);
        holder.userNameText.setText(user.getFullName());
        holder.userStatusText.setText(user.getStatus());

        if(user.getAvatar() != null)
            holder.userAvatarImage.setImageBitmap(user.getAvatar());
        else
            holder.userAvatarImage.setImageResource(R.drawable.blank_profile);
    }

    @Override
    public int getItemCount() { return contactList.size(); }

    public void changeContactState(int position, boolean state)
    {
        ContactViewHolder contactViewHolder = viewHashMap.get(position);

        if(contactViewHolder != null)
        {
            if(state)
                contactViewHolder.selectedImage.setVisibility(View.VISIBLE);
            else
                contactViewHolder.selectedImage.setVisibility(View.GONE);
        }
    }

    public class ContactViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final TextView userNameText;
        private final TextView userStatusText;
        private final ImageView selectedImage;
        private final ShapeableImageView userAvatarImage;

        public ContactViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userNameText = itemView.findViewById(R.id.userNameText);
            selectedImage = itemView.findViewById(R.id.selectedImage);
            userStatusText = itemView.findViewById(R.id.userStatusText);
            userAvatarImage = itemView.findViewById(R.id.userAvatarImage);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            int position = getAdapterPosition();

            if(position >= 0)
                IRecyclerViewItemClickListener.onItemClick(position, view);
        }
    }
}
