package com.client.talkster.api;

public class APIEndpoints
{
    //-----------------------------------[Chats]
    public final static String TALKSTER_API_CHAT_GET_CHATS = "/api/v1/chat/user-chats";
    public final static String TALKSTER_API_CHAT_GET_NEW_CHAT = "/api/v1/chat/find-chat";
    //-----------------------------------[Authorization]
    public final static String TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER = "/api/v1/auth/verify";
    public final static String TALKSTER_API_AUTH_ENDPOINT_FIND_USER = "/api/v1/auth/find-user";
    public final static String TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER = "/api/v1/auth/register";
    public final static String TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION = "/api/v1/auth/verify-session";
}
