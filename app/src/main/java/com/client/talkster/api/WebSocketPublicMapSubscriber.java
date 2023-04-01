package com.client.talkster.api;

import android.app.Activity;
import android.widget.Toast;

import com.client.talkster.interfaces.IChatWebSocketHandler;
import com.client.talkster.interfaces.IMapWebSocketHandler;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketPublicMapSubscriber extends Subscriber<StompMessage>
{
    private Activity activity;

    public WebSocketPublicMapSubscriber(Activity activity)
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
        activity.runOnUiThread(() -> {

            if(activity instanceof IMapWebSocketHandler)
                ((IMapWebSocketHandler) activity).onMapMessageReceived(stompMessage.getPayload());
        });
    }
}
