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

package fr.florianpal.fmessage.velocity;

import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import fr.florianpal.fmessage.core.protocol.ChatProtocol;

/**
 * Velocity wants typed channel identifiers where BungeeCord takes plain strings, so the
 * shared protocol keeps the strings and this class adapts them.
 */
final class VelocityChannels {

    static final MinecraftChannelIdentifier TO_BUKKIT =
            MinecraftChannelIdentifier.from(ChatProtocol.CHANNEL_TO_BUKKIT);

    static final MinecraftChannelIdentifier FROM_BUKKIT =
            MinecraftChannelIdentifier.from(ChatProtocol.CHANNEL_FROM_BUKKIT);

    private VelocityChannels() {
    }
}
