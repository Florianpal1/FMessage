
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
 * Last modification : 20/10/2021 19:57
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.managers.commandManagers;

import co.aikar.commands.CommandCompletions;
import co.aikar.commands.VelocityCommandCompletionContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;

import java.util.stream.Collectors;

public class CommandCompletionsManager {
    private FMessage plugin;

    public CommandCompletionsManager(FMessage plugin) {
        this.plugin = plugin;
        registerCommandCompletions();
    }

    private void registerCommandCompletions() {
        CommandCompletions<VelocityCommandCompletionContext> commandCompletions = plugin.getCommandManager().getCommandCompletions();
        commandCompletions.registerAsyncCompletion("bungeeplayers", c -> {
            CommandSource sender = c.getSender();
            if (sender instanceof Player) {
                return plugin.getServer().getAllPlayers()
                        .stream()
                        .map(Player::getUsername)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }
}
