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

import fr.florianpal.fmessage.platform.ProxyBackendServer;
import fr.florianpal.fmessage.platform.ProxyLogger;
import fr.florianpal.fmessage.platform.ProxyPlatform;
import fr.florianpal.fmessage.platform.ProxyPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BungeePlatform implements ProxyPlatform {

    private final Plugin plugin;
    private final ProxyServer proxy;
    private final ProxyLogger logger;

    public BungeePlatform(Plugin plugin) {
        this.plugin = plugin;
        this.proxy = plugin.getProxy();
        this.logger = new BungeeLogger(plugin.getLogger());
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(UUID uuid) {
        return Optional.ofNullable(proxy.getPlayer(uuid)).map(BungeePlayer::new);
    }

    @Override
    public Optional<ProxyPlayer> getPlayer(String name) {
        return Optional.ofNullable(proxy.getPlayer(name)).map(BungeePlayer::new);
    }

    @Override
    public Collection<ProxyPlayer> getOnlinePlayers() {
        List<ProxyPlayer> players = new ArrayList<>();
        for (ProxiedPlayer player : proxy.getPlayers()) {
            players.add(new BungeePlayer(player));
        }
        return players;
    }

    @Override
    public Collection<ProxyBackendServer> getServers() {
        List<ProxyBackendServer> servers = new ArrayList<>();
        for (ServerInfo info : proxy.getServers().values()) {
            servers.add(new BungeeBackendServer(info));
        }
        return servers;
    }

    @Override
    public File getDataFolder() {
        return plugin.getDataFolder();
    }

    @Override
    public ProxyLogger getLogger() {
        return logger;
    }
}
