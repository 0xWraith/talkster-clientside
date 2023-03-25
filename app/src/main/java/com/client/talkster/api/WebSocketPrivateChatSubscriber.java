package com.client.talkster.api;

import android.app.Activity;
import com.client.talkster.interfaces.IChatWebSocketHandler;

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

            if(activity instanceof IChatWebSocketHandler)
                ((IChatWebSocketHandler) activity).onMessageReceived(stompMessage.getPayload());
        });
    }
}
