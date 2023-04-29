package com.client.talkster.api.websocket.listeners;

import android.app.Activity;
import android.widget.Toast;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketPublicChatSubscriber extends Subscriber<StompMessage>
{
    private Activity activity;

    public WebSocketPublicChatSubscriber(Activity activity)
    {
        this.activity = activity;
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
        activity.runOnUiThread(() -> Toast.makeText(activity, "" + stompMessage.getPayload(), Toast.LENGTH_SHORT).show());
    }
}
