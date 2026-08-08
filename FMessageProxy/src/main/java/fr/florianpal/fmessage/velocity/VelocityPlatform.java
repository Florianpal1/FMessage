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

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyLogger;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VelocityPlatform implements ProxyPlatform {

    private final ProxyServer proxy;
    private final Path dataDirectory;
    private final ProxyLogger logger;

    public VelocityPlatform(ProxyServer proxy, Path dataDirectory, org.slf4j.Logger logger) {
        this.proxy = proxy;
        this.dataDirectory = dataDirectory;
        this.logger = new VelocityLogger(logger);
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return proxy.getPlayer(uuid).map(this::wrap);
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(String name) {
        return proxy.getPlayer(name).map(this::wrap);
    }

    @Override
    public Collection<ProxyPlayer> getOnlinePlayers() {
        List<ProxyPlayer> players = new ArrayList<>();
        for (Player player : proxy.getAllPlayers()) {
            players.add(wrap(player));
        }
        return players;
    }

    @Override
    public Collection<ProxyBackendServer> getServers() {
        List<ProxyBackendServer> servers = new ArrayList<>();
        for (RegisteredServer server : proxy.getAllServers()) {
            servers.add(new VelocityBackendServer(server));
        }
        return servers;
    }

    @Override
    public File getDataFolder() {
        return dataDirectory.toFile();
    }

    @Override
    public ProxyLogger getLogger() {
        return logger;
    }

    ProxyPlayer wrap(Player player) {
        return new VelocityPlayer(player, logger);
    }
}
