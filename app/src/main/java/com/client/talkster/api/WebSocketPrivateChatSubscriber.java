package com.client.talkster.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.client.talkster.controllers.talkster.ChatsFragment;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketPrivateChatSubscriber extends Subscriber<StompMessage>
{
    private Context context;
    private Fragment fragment;
    private FragmentActivity fragmentActivity;


    /*public WebSocketPrivateChatSubscriber(Fragment chatsFragment)
    {
        this.chatsFragment = chatsFragment;

        if(chatsFragment == null)
            return;

        chatContext = chatsFragment.getContext();
        chatActivity = chatsFragment.getActivity();
    }*/

    public WebSocketPrivateChatSubscriber setSubscriberFragment(Fragment fragment)
    {
        this.fragment = fragment;
        this.context = fragment.getContext();
        this.fragmentActivity = fragment.getActivity();

        return this;
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
//        fragmentActivity.runOnUiThread(() -> {
//            Toast.makeText(fragmentActivity, stompMessage.getPayload(), Toast.LENGTH_SHORT).show();
//        });
    }
}
