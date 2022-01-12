

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

import org.bukkit.configuration.Configuration;

public class ChatConfig {
    private String chatFormat;
    private String ignoreFormat;
    public void load(Configuration config) {
        chatFormat = config.getString("chatFormat");
        ignoreFormat = config.getString("ignoreFormat");
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public String getIgnoreFormat() {
        return ignoreFormat;
    }
}
