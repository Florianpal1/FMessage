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
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

/**
 * Turns BungeeCord events into calls on the shared services. Nothing else belongs here.
 */
public class BungeeEventBridge implements Listener {

    private final FMessageCore core;

    public BungeeEventBridge(FMessageCore core) {
        this.core = core;
    }

    /**
     * Keeps the name to UUID directory up to date so /ignore works on offline players.
     */
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        ProxiedPlayer player = event.getPlayer();
        core.getPlayerCommandManager().savePlayer(player.getUniqueId(), player.getName());
    }

    @EventHandler
    public void onMessage(PluginMessageEvent event) {
        if (event.getTag().equalsIgnoreCase(ChatProtocol.CHANNEL_FROM_BUKKIT)) {
            core.getChatRelayService().handle(event.getData());
        }
    }
}
