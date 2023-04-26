package com.client.talkster.interfaces;

import android.view.View;

public interface IRecyclerViewItemClickListener
{
    void onItemClick(int position, View v);
    void onItemLongClick(int position, View v);
}
