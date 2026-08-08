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

import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.protocol.ChatProtocol;
import net.md_5.bungee.api.plugin.Plugin;
import org.bstats.bungeecord.Metrics;

/**
 * BungeeCord entry point, named by {@code plugin.yml}.
 *
 * <p>Velocity never loads this class, and BungeeCord never loads
 * {@code VelocityBootstrap}: class loading is lazy and no shared class references either
 * bootstrap. That is the whole trick behind the single jar.</p>
 */
public class BungeeBootstrap extends Plugin {

    private static final int BSTATS_PLUGIN_ID = 24047;

    private FMessageCore core;

    @Override
    public void onEnable() {
        new Metrics(this, BSTATS_PLUGIN_ID);

        getProxy().registerChannel(ChatProtocol.CHANNEL_TO_BUKKIT);
        getProxy().registerChannel(ChatProtocol.CHANNEL_FROM_BUKKIT);

        core = new FMessageCore(new BungeePlatform(this));

        BungeeCommandManagerImpl commandManager = new BungeeCommandManagerImpl(this, core);
        core.registerCommands(commandManager);
        BungeeCompletionsRegistrar.register(commandManager, core);

        getProxy().getPluginManager().registerListener(this, new BungeeEventBridge(core));

        getLogger().info("FMessage enabled");
    }

    public FMessageCore getCore() {
        return core;
    }
}
