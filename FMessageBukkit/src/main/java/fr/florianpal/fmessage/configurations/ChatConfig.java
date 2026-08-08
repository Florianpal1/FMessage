

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

    private String lang;
    private String chatFormat;
    private String ignoreFormat;
    private String floodFormat;
    private String spamFormat;
    private String staffChatFormat;

    private boolean floodEnabled;
    private int floodCharRepeat;

    private boolean spamEnabled;
    private int spamMinLength;
    private double spamRatio;

    public void load(Configuration config) {

        lang = config.getString("lang");

        chatFormat = config.getString("chatFormat.general");
        staffChatFormat = config.getString("chatFormat.staffChat");

        ignoreFormat = config.getString("ignoreFormat");
        floodFormat = config.getString("floodFormat");
        spamFormat = config.getString("spamFormat");

        // Defaults match the behaviour that used to be hard-coded in ChatListener.
        floodEnabled = config.getBoolean("antiFlood.enabled", true);
        floodCharRepeat = config.getInt("antiFlood.charRepeat", 7);

        spamEnabled = config.getBoolean("antiCaps.enabled", true);
        spamMinLength = config.getInt("antiCaps.minLength", 3);
        spamRatio = config.getDouble("antiCaps.ratio", 1.0);
    }

    public boolean isFloodEnabled() {
        return floodEnabled;
    }

    public int getFloodCharRepeat() {
        return floodCharRepeat;
    }

    public boolean isSpamEnabled() {
        return spamEnabled;
    }

    public int getSpamMinLength() {
        return spamMinLength;
    }

    public double getSpamRatio() {
        return spamRatio;
    }

    public String getChatFormat() {
        return chatFormat;
    }

    public String getIgnoreFormat() {
        return ignoreFormat;
    }

    public String getFloodFormat() {
        return floodFormat;
    }

    public String getSpamFormat() {
        return spamFormat;
    }

    public String getStaffChatFormat() {
        return staffChatFormat;
    }

    public String getLang() {
        return lang;
    }
}
