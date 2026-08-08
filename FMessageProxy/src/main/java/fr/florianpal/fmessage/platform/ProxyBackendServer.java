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

/**
 * A backend (game) server registered on the proxy.
 */
public interface ProxyBackendServer {

    String getName();

    /**
     * Sends a raw payload on the {@code fmessage:chatbukkit} channel, the one
     * FMessageBukkit listens to. The channel is implicit: it is the only one the proxy
     * ever writes to.
     */
    void sendData(byte[] payload);
}
