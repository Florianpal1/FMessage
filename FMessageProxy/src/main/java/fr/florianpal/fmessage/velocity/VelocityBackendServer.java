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

import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.florianpal.fmessage.platform.ProxyBackendServer;

import java.util.Objects;

public class VelocityBackendServer implements ProxyBackendServer {

    private final RegisteredServer handle;

    public VelocityBackendServer(RegisteredServer handle) {
        this.handle = handle;
    }

    @Override
    public String getName() {
        return handle.getServerInfo().getName();
    }

    @Override
    public void sendData(byte[] payload) {
        handle.sendPluginMessage(VelocityChannels.TO_BUKKIT, payload);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VelocityBackendServer server && getName().equals(server.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getName());
    }
}
