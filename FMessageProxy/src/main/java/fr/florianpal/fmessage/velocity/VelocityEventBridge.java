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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.core.FMessageCore;

/**
 * Turns Velocity events into calls on the shared services. Nothing else belongs here.
 */
public class VelocityEventBridge {

    private final FMessageCore core;

    public VelocityEventBridge(FMessageCore core) {
        this.core = core;
    }

    /**
     * Keeps the name to UUID directory up to date so /ignore works on offline players.
     */
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        Player player = event.getPlayer();
        core.getPlayerCommandManager().savePlayer(player.getUniqueId(), player.getUsername());
    }

    @Subscribe
    public void onMessage(PluginMessageEvent event) {
        if (event.getIdentifier().equals(VelocityChannels.FROM_BUKKIT)) {
            core.getChatRelayService().handle(event.getData());
        }
    }
}
