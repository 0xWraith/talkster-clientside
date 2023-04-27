package com.client.talkster.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.MyApplication;
import com.client.talkster.R;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.utils.Utils;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

public class ThemeListAdapter extends RecyclerView.Adapter<ThemeListAdapter.ThemeViewHolder>
{
    private final Context context;
    private final List<Theme> themeList;
    private final IRecyclerViewItemClickListener itemClickListener;

    public ThemeListAdapter(Context context, IRecyclerViewItemClickListener itemClickListener)
    {
        this.context = context;
        this.themeList = new ArrayList<>();
        this.itemClickListener = itemClickListener;
    }

    public List<Theme> getThemeList() { return themeList; }

    @NonNull
    @Override
    public ThemeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.component_theme_item, parent, false);
        return new ThemeListAdapter.ThemeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ThemeViewHolder holder, int position)
    {
        Theme theme = themeList.get(position);

        holder.themeNameText.setText(theme.getName());
        holder.themeNameText.setTextColor(ContextCompat.getColor(MyApplication.getAppContext(), theme.getThemeColor()));

        setThemeBackground(holder.themePreview, theme);
        setThemeChatSenderPreview(holder.themeChatSenderPreview, theme);
        setThemeChatReceiverPreview(holder.themeChatReceiverPreview, theme);
    }

    private void setThemeChatSenderPreview(View themeChatSenderPreview, Theme theme)
    {
        GradientDrawable gradientDrawable = ThemeManager.getChatSenderDrawable(ContextCompat.getColor(MyApplication.getAppContext(), theme.getChatSenderColor1()),
                                                                               ContextCompat.getColor(MyApplication.getAppContext(), theme.getChatSenderColor2()),
                                                                               ContextCompat.getColor(MyApplication.getAppContext(), theme.getChatSenderColor3()));
        gradientDrawable.setCornerRadius(Utils.convertDPToPx(context, 10));
        themeChatSenderPreview.setBackground(gradientDrawable);
    }

    private void setThemeChatReceiverPreview(View themeChatReceiverPreview, Theme theme)
    {
        GradientDrawable gradientDrawable = ThemeManager.getChatReceiverDrawable(ContextCompat.getColor(MyApplication.getAppContext(), theme.getChatInColor()));
        gradientDrawable.setCornerRadius(Utils.convertDPToPx(context, 10));
        themeChatReceiverPreview.setBackground(gradientDrawable);
    }

    private void setThemeBackground(ShapeableImageView themeBackground, Theme theme)
    {
       themeBackground.setBackgroundResource(theme.getChatBackgroundImage());
       themeBackground.setStrokeColorResource(theme.getThemeColor());
    }

    @Override
    public int getItemCount() { return themeList.size(); }

    public class ThemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView themeNameText;
        private final ShapeableImageView themePreview;
        private final View themeChatSenderPreview;
        private final View themeChatReceiverPreview;

        public ThemeViewHolder(@NonNull View itemView)
        {
            super(itemView);


            themePreview = itemView.findViewById(R.id.themePreview);
            themeNameText = itemView.findViewById(R.id.themeNameText);

            themeChatSenderPreview = itemView.findViewById(R.id.themeChatSenderPreview);
            themeChatReceiverPreview = itemView.findViewById(R.id.themeChatReceiverPreview);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view)
        {
            int position = getAdapterPosition();

            if (position >= 0)
                itemClickListener.onItemClick(position, view);
        }
    }
}
