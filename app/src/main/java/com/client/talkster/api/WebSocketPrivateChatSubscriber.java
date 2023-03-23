package com.client.talkster.api;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.client.talkster.HomeActivity;
import com.client.talkster.controllers.talkster.ChatsFragment;
import com.client.talkster.interfaces.IChatMessagesListener;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketPrivateChatSubscriber extends Subscriber<StompMessage>
{
    private final Activity activity;

    public WebSocketPrivateChatSubscriber(Activity activity) { this.activity = activity; }

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
        activity.runOnUiThread(() -> {
            if(activity instanceof IChatMessagesListener)
                ((IChatMessagesListener) activity).onMessageReceived(stompMessage.getPayload());
        });
    }
}
