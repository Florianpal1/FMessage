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

package fr.florianpal.fmessage.platform;

import java.util.Optional;
import java.util.UUID;

/**
 * A player connected to the proxy.
 *
 * <p>Implementations are thin wrappers created on demand, so they must implement
 * {@code equals}/{@code hashCode} on {@link #getUniqueId()} alone.</p>
 */
public interface ProxyPlayer {

    UUID getUniqueId();

    String getName();

    boolean hasPermission(String node);

    /**
     * Sends a message written in the legacy {@code §} format.
     *
     * <p>The core never builds platform components: it produces legacy text and the
     * adapter converts it at the very last moment — {@code TextComponent.fromLegacyText}
     * on BungeeCord, a {@code LegacyComponentSerializer} on Velocity. That is what keeps
     * Adventure out of the shared code, and out of the shaded jar.</p>
     */
    void sendMessage(String legacyText);

    /**
     * Plays the notification sound identified by a Minecraft sound key.
     *
     * <p>Velocity does it natively; BungeeCord has no sound API and asks the backend
     * server to play it through the plugin message channel.</p>
     */
    void playSound(String soundKey);

    /**
     * @return the backend server the player is currently connected to, empty while switching.
     */
    Optional<ProxyBackendServer> getCurrentServer();

    /**
     * The underlying platform object, for the sole use of ACF's
     * {@code CommandManager#getCommandIssuer(Object)}, which accepts an {@code Object}.
     */
    Object nativeHandle();
}
