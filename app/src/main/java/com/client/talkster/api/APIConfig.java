package com.client.talkster.api;

import okhttp3.MediaType;

public class APIConfig
{
    public static final int TALKSTER_SERVER_PORT = 8000;
    public static final int TALKSTER_WEBSOCKET_SERVER_PORT = 8080;
    public static final String TALKSTER_SERVER_IP = "10.10.1.103";
//    public static final String TALKSTER_SERVER_IP = "147.175.160.77";
//    public static final String TALKSTER_SERVER_IP = "10.62.28.243";

    public static final String TALKSTER_SERVER_WEBSOCKET_PROTOCOL = "ws://";
    public static final String TALKSTER_SERVER_INTERNET_PROTOCOL = "http://";
    public static final String TALKSTER_SERVER_WEBSOCKET_ENDPOINT = "/websocket/websocket";
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
}