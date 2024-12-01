
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
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.configurations;

import dev.dejvokep.boostedyaml.YamlDocument;

public class ChatConfig {

    private String lang;
    private String targetChatFormat;
    private String senderChatFormat;
    private String spyChatFormat;

    public void load(YamlDocument config) {
        lang = config.getString("lang");
        targetChatFormat = config.getString("format.targetChatFormat");
        senderChatFormat = config.getString("format.senderChatFormat");
        spyChatFormat = config.getString("format.spyChatFormat");
    }

    public String getTargetChatFormat() {
        return targetChatFormat;
    }

    public String getSenderChatFormat() {
        return senderChatFormat;
    }

    public String getSpyChatFormat() {
        return spyChatFormat;
    }

    public String getLang() {
        return lang;
    }
}
