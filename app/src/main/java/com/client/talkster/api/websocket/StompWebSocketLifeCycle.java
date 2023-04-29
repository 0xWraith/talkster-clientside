package com.client.talkster.api.websocket;

import android.util.Log;

import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;

public class StompWebSocketLifeCycle<T> implements Action1<T>
{
    @Override
    public void call(T t)
    {
        if(!(t instanceof LifecycleEvent))
            return;

        LifecycleEvent event = (LifecycleEvent)t;

        switch (event.getType())
        {
            case OPENED:
                Log.i("OPENED", "Stomp connection opened");
                break;

            case ERROR:
                Log.e("ERROR", "Error", event.getException());
                break;

            case CLOSED:
                Log.w("CLOSED", "Stomp connection closed");
                break;
        }
    }
}
