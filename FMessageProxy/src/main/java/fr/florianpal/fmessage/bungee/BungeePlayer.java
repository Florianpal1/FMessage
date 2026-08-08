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

package fr.florianpal.fmessage.bungee;

import fr.florianpal.fmessage.core.protocol.ChatProtocol;
import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BungeePlayer implements ProxyPlayer {

    private final ProxiedPlayer handle;

    public BungeePlayer(ProxiedPlayer handle) {
        this.handle = handle;
    }

    @Override
    public UUID getUniqueId() {
        return handle.getUniqueId();
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public boolean hasPermission(String node) {
        return handle.hasPermission(node);
    }

    @Override
    public void sendMessage(String legacyText) {
        handle.sendMessage(TextComponent.fromLegacyText(legacyText));
    }

    /**
     * BungeeCord has no sound API, so the backend server hosting the player is asked to
     * play it. A player mid-transfer has no server yet and is simply skipped.
     */
    @Override
    public void playSound(String soundKey) {
        Server server = handle.getServer();
        if (server == null) {
            return;
        }
        server.getInfo().sendData(ChatProtocol.CHANNEL_TO_BUKKIT,
                ChatProtocol.encodeSound(handle.getUniqueId(), soundKey));
    }

    @Override
    public Optional<ProxyBackendServer> getCurrentServer() {
        Server server = handle.getServer();
        if (server == null) {
            return Optional.empty();
        }
        return Optional.of(new BungeeBackendServer(server.getInfo()));
    }

    @Override
    public Object nativeHandle() {
        return handle;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ProxyPlayer player && getUniqueId().equals(player.getUniqueId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getUniqueId());
    }
}
