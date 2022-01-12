
/*
 * Copyright (C) 2022 Florianpal
 *
 * This program is free software;
 * you can redistribute it and/or modify it under the terms of the GNU General
 * Public License as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, see <http://www.gnu.org/licenses/>.
 *
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

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

    private static final String PREFIX = "fmessage";

    private final MessageKey key = MessageKey.of(PREFIX + "." + this.name().toLowerCase());

    public MessageKey getMessageKey() {
        return key;
    }
}
