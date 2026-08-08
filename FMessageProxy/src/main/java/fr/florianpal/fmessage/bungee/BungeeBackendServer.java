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
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Objects;

public class BungeeBackendServer implements ProxyBackendServer {

    private final ServerInfo handle;

    public BungeeBackendServer(ServerInfo handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getName();
    }

    @Override
    public void sendData(byte[] payload) {
        handle.sendData(ChatProtocol.CHANNEL_TO_BUKKIT, payload);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof BungeeBackendServer server && getName().equals(server.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
