package com.client.talkster.api.websocket.listeners;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.client.talkster.interfaces.chat.IChatWebSocketHandler;
import com.client.talkster.interfaces.chat.IGroupChatListener;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketGroupChatCreatedSubscriber extends Subscriber<StompMessage>
{
    private final Activity activity;

    public WebSocketGroupChatCreatedSubscriber(Activity activity) { this.activity = activity; }

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


            StringBuilder sb = new StringBuilder(stompMessage.getPayload());
            sb.deleteCharAt(0);

            if(activity instanceof IGroupChatListener)
                ((IGroupChatListener) activity).onGroupChatCreated(Long.parseLong(sb.toString()));
        });
    }
}
