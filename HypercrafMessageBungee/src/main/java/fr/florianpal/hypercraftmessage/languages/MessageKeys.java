package fr.florianpal.fmessage.languages;


import co.aikar.locales.MessageKey;
import co.aikar.locales.MessageKeyProvider;

public enum MessageKeys implements MessageKeyProvider {

    DATABASEERROR,

    NO_PREVIOUS_PLAYER,
    PLAYER_OFFLINE,
    SPY_ACTIVATE,
    SPY_DESACTIVATE,
    IGNORE_SUCCESS,
    IGNORE_ALREADY,
    UNIGNORE_SUCCESS,
    UNIGNORE_ALREADY,

    SENDER_IGNORE_MESSAGE,
    TARGET_IGNORE_MESSAGE,

    GROUP_ALREADY_EXIST,
    GROUP_CREATE_SUCCESS,

    GROUP_CANNOT_EXIST,
    GROUP_REMOVE_SUCCESS,

    GROUP_ALREADY_IN_GROUP,
    GROUP_ADDMEMBER_SUCCESS,

    GROUP_MEMBER_NOT_INGROUP,
    GROUP_REMOVEMEMBER_SUCCESS,

    GROUP_TOGGLE_DESACTIVATE,
    GROUP_TOGGLE_ACTIVATE,

    GROUP_ALREADY_TOGGLE,

    GROUP_MSG,

    CANNOT_IGNORE,

    IGNORE_NOT_EXIST;

    private static final String PREFIX = "hypercraft";

    private final MessageKey key = MessageKey.of(PREFIX + "." + this.name().toLowerCase());

    public MessageKey getMessageKey() {
        return key;
    }
}
