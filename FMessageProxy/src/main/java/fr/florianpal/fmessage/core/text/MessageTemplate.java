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

/**
 * Placeholder substitution inside a chat format.
 *
 * <p>Replaces the two {@code StringUtils} classes that used to differ between platforms:
 * the Velocity one substituted inside an Adventure component tree, the BungeeCord one
 * inside the string. Both now go through the string, which is also the only variant that
 * lets a value keep its own colours under a permission check.</p>
 */
public final class MessageTemplate {

    private MessageTemplate() {
    }

    public static boolean isNullOrEmpty(String text) {
        return text == null || text.isEmpty();
    }

    /**
     * @param template    the format holding the placeholder
     * @param placeholder the literal token, e.g. {@code {sender}}
     * @param value       what to put in its place, null is treated as empty
     * @param colors      whether {@code value} may carry its own colour codes; when false the
     *                    codes stay literal, which is what the {@code fmessage.colors} and
     *                    {@code fmessage.nick.colors} permissions gate
     */
    public static String replace(String template, String placeholder, String value, boolean colors) {
        if (template == null) {
            return "";
        }
        String safeValue = value == null ? "" : value;
        return template.replace(placeholder, colors ? LegacyFormatter.format(safeValue) : safeValue);
    }
}
