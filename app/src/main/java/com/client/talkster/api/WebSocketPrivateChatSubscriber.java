package com.client.talkster.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketPrivateChatSubscriber extends Subscriber<StompMessage>
{
    private Context chatContext;
    private final Fragment chatsFragment;
    private FragmentActivity chatActivity;


    public WebSocketPrivateChatSubscriber(Fragment chatsFragment)
    {
        this.chatsFragment = chatsFragment;

        if(chatsFragment == null)
            return;

        chatContext = chatsFragment.getContext();
        chatActivity = chatsFragment.getActivity();
    }

    @Override
    public void onCompleted()
    {

    }

    @Override
    public void onError(Throwable e)
    {

    }

    @Override
    public void onNext(StompMessage stompMessage)
    {
        Log.d("Message", stompMessage.getPayload());
        chatActivity.runOnUiThread(() -> {
            Toast.makeText(chatsFragment.getActivity(), stompMessage.getPayload(), Toast.LENGTH_SHORT).show();
        });
    }
}
