package com.client.talkster.api.websocket.listeners;

import android.app.Activity;

import com.client.talkster.interfaces.chat.IChatWebSocketHandler;
import com.client.talkster.interfaces.chat.IGroupChatListener;

import rx.Subscriber;
import ua.naiksoftware.stomp.client.StompMessage;

public class WebSocketGroupChatSubscriber extends Subscriber<StompMessage>
{
    private final Activity activity;

    public WebSocketGroupChatSubscriber(Activity activity) { this.activity = activity; }

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
                ((IGroupChatListener) activity).onGroupChatMessageReceived(stompMessage.getPayload());
        });
    }
}
