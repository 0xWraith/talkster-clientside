package com.client.talkster.api;


import android.util.Log;

import androidx.fragment.app.Fragment;

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
    private final List<String> userTopics;
    private final StompClient webSocketClient;
    public static final String TALKSTER_WEBSOCKET_URL = APIConfig.TALKSTER_SERVER_WEBSOCKET_PROTOCOL + APIConfig.TALKSTER_SERVER_IP + ":" + APIConfig.TALKSTER_WEBSOCKET_SERVER_PORT + APIConfig.TALKSTER_SERVER_WEBSOCKET_ENDPOINT;

    public APIStompWebSocket()
    {
        userTopics = new ArrayList<>();
        webSocketClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, TALKSTER_WEBSOCKET_URL);
    }

    public void connect()
    {
        webSocketClient.lifecycle().subscribe(new StompWebSocketLifeCycle<>());
        webSocketClient.connect();
    }

    public List<String> getUserTopics() { return userTopics; }
    public StompClient getWebSocketClient() { return webSocketClient; }

    public void addTopic(String topic, Subscriber<StompMessage> subscriber)
    {
        userTopics.add(topic);
        webSocketClient.topic(topic).subscribe(subscriber);
    }
}
