package com.client.talkster.api.websocket.listeners;

import android.app.Activity;

import com.client.talkster.interfaces.IMapWebSocketHandler;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketMapSubscriber extends Subscriber<StompMessage>
{
    private Activity activity;

    public WebSocketMapSubscriber(Activity activity)
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
        activity.runOnUiThread(() ->  {

            if(activity instanceof IMapWebSocketHandler)
                ((IMapWebSocketHandler) activity).onMapMessageReceived(stompMessage.getPayload());
        });
    }
}
