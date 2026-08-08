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

import co.aikar.commands.CommandCompletions;
import co.aikar.commands.VelocityCommandCompletionContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.core.FMessageCore;

/**
 * Velocity counterpart of {@code BungeeCompletionsRegistrar}; same twenty lines, same
 * reason. Both delegate to the shared {@code CompletionProviders}.
 */
public final class VelocityCompletionsRegistrar {

    private VelocityCompletionsRegistrar() {
    }

    public static void register(VelocityCommandManagerImpl commandManager, FMessageCore core) {
        CommandCompletions<VelocityCommandCompletionContext> completions =
                commandManager.getCommandCompletions();

        completions.registerAsyncCompletion("groups", context -> {
            CommandSource sender = context.getSender();
            if (!(sender instanceof Player player)) {
                return null;
            }
            return core.getCompletionProviders().groupsOf(player.getUniqueId());
        });

        completions.registerAsyncCompletion("owngroups", context -> {
            CommandSource sender = context.getSender();
            if (!(sender instanceof Player player)) {
                return null;
            }
            return core.getCompletionProviders().ownedGroupsOf(player.getUniqueId());
        });
    }
}
