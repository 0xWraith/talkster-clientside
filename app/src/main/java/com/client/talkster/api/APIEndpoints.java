package com.client.talkster.api;

public class APIEndpoints
{
    //-----------------------------------[Chats]
    public final static String TALKSTER_API_CHAT_GET_CHATS = "/api/v1/chat/user-chats";
    public final  static String TALKSTER_API_CHAT_GET_CHAT = "/api/v1/chat/user-chat";
    public final static String TALKSTER_API_CHAT_CREATE = "/api/v1/chat/create-chat";
    public final static String TALKSTER_API_CHAT_GET_NEW_CHAT = "/api/v1/chat/find-chat";
    //-----------------------------------[Files]
    public final static String TALKSTER_API_FILE_UPLOAD = "/api/v1/file/upload";
    public final static String TALKSTER_API_FILE_GET_PROFILE = "/api/v1/file/get-profile";
    public final static String TALKSTER_API_FILE_UPDATE_PROFILE = "/api/v1/file/set-profile";
    //-----------------------------------[Authorization]
    public final static String TALKSTER_API_AUTH_ENDPOINT_VERIFY_USER = "/api/v1/auth/verify";
    public final static String TALKSTER_API_AUTH_ENDPOINT_FIND_USER = "/api/v1/auth/find-user";
    public final static String TALKSTER_API_AUTH_ENDPOINT_REGISTER_USER = "/api/v1/auth/register";
    public final static String TALKSTER_API_AUTH_ENDPOINT_VERIFY_SESSION = "/api/v1/auth/verify-session";
    //-----------------------------------[Notifications]
    public final static String TALKSTER_API_NOTIFICATION_ADD_TOKEN = "/api/v1/notification/add-token";
    //-----------------------------------[Users]
    public final static String TALKSTER_API_USER_UPDATE_NAME = "/api/v1/user/set-name";
    public final static String TALKSTER_API_USER_GET_NAME = "/api/v1/user/get-name";
}
