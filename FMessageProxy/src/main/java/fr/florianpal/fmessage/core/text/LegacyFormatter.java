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
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.core.text;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a configured format into legacy {@code §} text.
 *
 * <p>Reimplements what {@code net.md_5.bungee.api.ChatColor} used to do, without depending
 * on it: this is plain string manipulation and the core must stay platform-free. Velocity
 * used to go through Adventure's {@code legacyAmpersand()} instead, which silently dropped
 * {@code {#rrggbb}} tokens — that bug disappears now that both platforms share this code.</p>
 */
public final class LegacyFormatter {

    /** The section sign Minecraft uses as its colour prefix. */
    public static final char COLOR_CHAR = '§';

    /** Codes {@code &} may introduce, mirroring BungeeCord's {@code ALL_CODES}. */
    private static final String ALL_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx";

    private static final Pattern HEX_TOKEN = Pattern.compile("[{]#[a-fA-F0-9]{6}[}]");

    private LegacyFormatter() {
    }

    /**
     * @param msg text using {@code &} codes and {@code {#rrggbb}} tokens, may be null
     * @return the same text with {@code §} codes, never null
     */
    public static String format(String msg) {
        if (msg == null) {
            return "";
        }
        return translateAlternateColorCodes(translateHexTokens(msg));
    }

    /**
     * {@code {#FF0000}} becomes {@code §x§F§F§0§0§0§0}, the form vanilla clients understand.
     * The case of the hex digits is preserved, exactly like BungeeCord's {@code ChatColor.of}.
     */
    private static String translateHexTokens(String msg) {
        Matcher matcher = HEX_TOKEN.matcher(msg);
        if (!matcher.find()) {
            return msg;
        }

        StringBuilder out = new StringBuilder(msg.length() + 16);
        int last = 0;
        do {
            out.append(msg, last, matcher.start());
            // Skips "{#" and the trailing "}" to keep only the six hex digits.
            String hex = msg.substring(matcher.start() + 2, matcher.end() - 1);
            out.append(COLOR_CHAR).append('x');
            for (int i = 0; i < hex.length(); i++) {
                out.append(COLOR_CHAR).append(hex.charAt(i));
            }
            last = matcher.end();
        } while (matcher.find());
        out.append(msg, last, msg.length());

        return out.toString();
    }

    /**
     * {@code &a} becomes {@code §a}. A trailing lone {@code &} is left untouched, and the
     * code character is lowercased, both as BungeeCord does.
     */
    private static String translateAlternateColorCodes(String msg) {
        char[] chars = msg.toCharArray();
        for (int i = 0; i < chars.length - 1; i++) {
            if (chars[i] == '&' && ALL_CODES.indexOf(chars[i + 1]) > -1) {
                chars[i] = COLOR_CHAR;
                chars[i + 1] = Character.toLowerCase(chars[i + 1]);
            }
        }
        return new String(chars);
    }
}
