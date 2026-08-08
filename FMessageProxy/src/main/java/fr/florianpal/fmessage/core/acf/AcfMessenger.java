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

package fr.florianpal.fmessage.core.acf;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.CommandManager;
import fr.florianpal.fmessage.platform.ProxyPlayer;

/**
 * Backs {@link Messenger} with the platform's ACF manager.
 *
 * <p>{@code getCommandIssuer} takes an {@code Object} on the shared base class, which is
 * exactly why {@link ProxyPlayer#nativeHandle()} exists: the Bungee manager expects a
 * {@code CommandSender}, the Velocity one a {@code CommandSource}, and neither type may
 * appear here.</p>
 */
public class AcfMessenger implements Messenger {

    private final CommandManager<?, ?, ?, ?, ?, ?> commandManager;

    public AcfMessenger(CommandManager<?, ?, ?, ?, ?, ?> commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public CommandIssuer issuerOf(ProxyPlayer player) {
        return commandManager.getCommandIssuer(player.nativeHandle());
    }
}
