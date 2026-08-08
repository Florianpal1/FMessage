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

package fr.florianpal.fmessage.core.protocol;

import java.util.UUID;

/**
 * A frame sent by FMessageBukkit on the {@code fmessage:chatbungee} channel.
 *
 * <p>{@code colors} and {@code nickColors} are only present on public chat frames; staff
 * frames stop after {@code message}. Both default to {@code false} when absent, which is
 * the safe reading — the proxy would rather strip colours than grant them.</p>
 */
public record IncomingChat(String subchannel,
                           UUID uuid,
                           String displayName,
                           String format,
                           String message,
                           boolean colors,
                           boolean nickColors) {
}
