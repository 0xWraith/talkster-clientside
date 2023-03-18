package com.client.talkster.api;


import android.util.Log;

import com.client.talkster.classes.UserJWT;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.functions.Action1;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class APIStompWebSocket
{
    private UserJWT userJWT;
    private final List<String> userTopics;
    private final List<StompHeader> headers;
    private final StompClient webSocketClient;
    public static final String TALKSTER_WEBSOCKET_URL = APIConfig.TALKSTER_SERVER_WEBSOCKET_PROTOCOL + APIConfig.TALKSTER_SERVER_IP + ":" + APIConfig.TALKSTER_WEBSOCKET_SERVER_PORT + APIConfig.TALKSTER_SERVER_WEBSOCKET_ENDPOINT;

    public APIStompWebSocket(UserJWT userJWT)
    {
        this.userJWT = userJWT;
        headers = new ArrayList<>();
        userTopics = new ArrayList<>();
        webSocketClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, TALKSTER_WEBSOCKET_URL);
        webSocketClient.lifecycle().subscribe(new StompWebSocketLifeCycle<>());
    }

    public void connect() { webSocketClient.connect(headers); }

    public List<StompHeader> getHeaders() { return headers; }
    public List<String> getUserTopics() { return userTopics; }
    public StompClient getWebSocketClient() { return webSocketClient; }

    public void addHeader(String key, String value) { headers.add(new StompHeader(key, value)); }
    public void addTopic(String topic, Subscriber<StompMessage> subscriber)
    {
        userTopics.add(topic);
        webSocketClient.topic(topic).subscribe(subscriber);
    }

    public static class StompWebSocketLifeCycle<T> implements Action1<T>
    {
        @Override
        public void call(T t)
        {
            if(!(t instanceof LifecycleEvent))
                return;

            LifecycleEvent event = (LifecycleEvent)t;

            switch (event.getType()) {

                case OPENED:
                    Log.d("OPENED", "Stomp connection opened");
                    break;

                case ERROR:
                    Log.e("ERROR", "Error", event.getException());
                    break;

                case CLOSED:
                    Log.d("CLOSED", "Stomp connection closed");
                    break;
            }
        }
    }
}
