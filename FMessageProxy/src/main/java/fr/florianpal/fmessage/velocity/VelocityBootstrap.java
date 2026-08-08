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

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.florianpal.fmessage.core.FMessageCore;
import org.bstats.velocity.Metrics;
import org.slf4j.Logger;

import java.nio.file.Path;

/**
 * Velocity entry point. The annotation below is what produces {@code velocity-plugin.json}
 * at build time; BungeeCord never sees it, and never loads this class.
 *
 * <p>{@code id} stays {@code fmessage}: it decides the data directory
 * ({@code plugins/fmessage/}), so changing it would hide the configuration of every
 * existing installation.</p>
 */
@Plugin(id = "fmessage", name = "FMessage", version = "3.0.0",
        url = "https://florianpal.fr", description = "FMessage", authors = {"Florianpal"})
public class VelocityBootstrap {

    private static final int BSTATS_PLUGIN_ID = 24047;

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Metrics.Factory metricsFactory;
    private final Path dataDirectory;

    private FMessageCore core;

    @Inject
    public VelocityBootstrap(ProxyServer proxyServer,
                             Logger logger,
                             Metrics.Factory metricsFactory,
                             @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.metricsFactory = metricsFactory;
        this.dataDirectory = dataDirectory;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        metricsFactory.make(this, BSTATS_PLUGIN_ID);

        proxyServer.getChannelRegistrar().register(VelocityChannels.TO_BUKKIT);
        proxyServer.getChannelRegistrar().register(VelocityChannels.FROM_BUKKIT);

        core = new FMessageCore(new VelocityPlatform(proxyServer, dataDirectory, logger));

        VelocityCommandManagerImpl commandManager =
                new VelocityCommandManagerImpl(proxyServer, this, core);
        core.registerCommands(commandManager);
        VelocityCompletionsRegistrar.register(commandManager, core);

        proxyServer.getEventManager().register(this, new VelocityEventBridge(core));

        logger.info("FMessage enabled");
    }

    public FMessageCore getCore() {
        return core;
    }
}
