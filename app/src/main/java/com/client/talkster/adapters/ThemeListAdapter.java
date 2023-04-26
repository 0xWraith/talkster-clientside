package com.client.talkster.adapters;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.client.talkster.R;
import com.client.talkster.classes.theme.Theme;
import com.client.talkster.controllers.ThemeManager;
import com.client.talkster.interfaces.IRecyclerViewItemClickListener;
import com.client.talkster.utils.Utils;

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
        holder.themeNameText.setTextColor(theme.getThemeColor());

        setThemeBackground(holder.themePreview, theme);
        setThemeChatSenderPreview(holder.themeChatSenderPreview, theme);
        setThemeChatReceiverPreview(holder.themeChatReceiverPreview, theme);
    }

    private void setThemeChatSenderPreview(View themeChatSenderPreview, Theme theme)
    {
        GradientDrawable gradientDrawable = ThemeManager.getChatSenderDrawable(theme.getChatSenderColor1(), theme.getChatSenderColor2(), theme.getChatSenderColor3());
        gradientDrawable.setCornerRadius(Utils.convertDPToPx(context, 10));
        themeChatSenderPreview.setBackground(gradientDrawable);
    }

    private void setThemeChatReceiverPreview(View themeChatReceiverPreview, Theme theme)
    {
        GradientDrawable gradientDrawable = ThemeManager.getChatReceiverDrawable(theme.getChatInColor());
        gradientDrawable.setCornerRadius(Utils.convertDPToPx(context, 10));
        themeChatReceiverPreview.setBackground(gradientDrawable);
    }

    private void setThemeBackground(RelativeLayout themeBackground, Theme theme)
    {
        LayerDrawable layerDrawable;
        GradientDrawable gradientDrawable;

        layerDrawable = (LayerDrawable) AppCompatResources.getDrawable(context, R.drawable.drawable_theme_background);

        if(layerDrawable == null)
            return;

        gradientDrawable = (GradientDrawable) layerDrawable.findDrawableByLayerId(R.id.themeBorder);

        gradientDrawable.setStroke(Utils.convertDPToPx(context, 2), theme.getThemeColor());
        layerDrawable.setDrawableByLayerId(R.id.themeBackground, AppCompatResources.getDrawable(context, theme.getChatBackgroundImage()));

        themeBackground.setBackground(layerDrawable);
    }

    @Override
    public int getItemCount() { return themeList.size(); }

    public class ThemeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        private final TextView themeNameText;
        private final RelativeLayout themePreview;
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
