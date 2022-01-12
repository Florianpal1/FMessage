
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

import co.aikar.commands.BungeeCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import fr.florianpal.fmessage.FMessage;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import java.util.stream.Collectors;

public class CommandCompletionsManager {
    private FMessage plugin;

    public CommandCompletionsManager(FMessage plugin) {
        this.plugin = plugin;
        registerCommandCompletions();
    }

    private void registerCommandCompletions() {
        CommandCompletions<BungeeCommandCompletionContext> commandCompletions = plugin.getCommandManager().getCommandCompletions();
        commandCompletions.registerAsyncCompletion("bungeeplayers", c -> {
            CommandSender sender = c.getSender();
            if (sender instanceof ProxiedPlayer) {
                System.out.println(plugin.getProxy().getPlayers());
                return plugin.getProxy().getPlayers()
                        .stream()
                        .map(ProxiedPlayer::getDisplayName)
                        .collect(Collectors.toList());
            }
            return null;
        });
    }
}
