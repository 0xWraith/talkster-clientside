package com.client.talkster.api.websocket;


import com.client.talkster.api.APIConfig;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

public class APIStompWebSocket
{
    private static volatile APIStompWebSocket INSTANCE;

    private final List<String> userTopics;
    private final StompClient webSocketClient;
    public static final String TALKSTER_WEBSOCKET_URL = APIConfig.TALKSTER_SERVER_WEBSOCKET_PROTOCOL + APIConfig.TALKSTER_SERVER_IP + ":" + APIConfig.TALKSTER_WEBSOCKET_SERVER_PORT + APIConfig.TALKSTER_SERVER_WEBSOCKET_ENDPOINT;

    private APIStompWebSocket()
    {
        userTopics = new ArrayList<>();
        webSocketClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, TALKSTER_WEBSOCKET_URL);
    }

    public static APIStompWebSocket getInstance()
    {
        if(INSTANCE != null)
            return INSTANCE;

        synchronized (APIStompWebSocket.class)
        {
            if(INSTANCE == null)
                INSTANCE = new APIStompWebSocket();

            return INSTANCE;
        }
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
