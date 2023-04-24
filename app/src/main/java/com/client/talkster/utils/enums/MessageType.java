package com.client.talkster.utils.enums;

import java.io.Serializable;

public enum MessageType implements Serializable
{
    TEXT_MESSAGE,
    MEDIA_MESSAGE,
    AUDIO_MESSAGE,
    CLEAR_CHAT_HISTORY,
    DELETE_CHAT,
    MUTE_CHAT
}
