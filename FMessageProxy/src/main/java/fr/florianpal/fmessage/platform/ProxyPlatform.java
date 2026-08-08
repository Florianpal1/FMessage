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

import java.io.File;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

/**
 * Everything the shared core needs from the proxy it runs on.
 *
 * <p>Implemented once for BungeeCord and once for Velocity. No class of this package,
 * nor of {@code fr.florianpal.fmessage.core}, may reference a platform API directly:
 * a single leaked import would compile fine (both APIs are on the classpath) but blow up
 * at startup on the other platform. {@code ArchitectureTest} enforces that rule.</p>
 */
public interface ProxyPlatform {

    Optional<ProxyPlayer> getPlayer(UUID uuid);

    Optional<ProxyPlayer> getPlayer(String name);

    Collection<ProxyPlayer> getOnlinePlayers();

    /**
     * @return every backend server registered on the proxy, in no particular order.
     */
    Collection<ProxyBackendServer> getServers();

    File getDataFolder();

    ProxyLogger getLogger();
}
