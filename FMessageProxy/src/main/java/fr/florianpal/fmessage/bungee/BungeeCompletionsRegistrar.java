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

import co.aikar.commands.BungeeCommandCompletionContext;
import co.aikar.commands.CommandCompletions;
import fr.florianpal.fmessage.core.FMessageCore;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * The ~20 lines of duplication the merge could not remove: {@code getCommandCompletions()}
 * is declared as {@code CommandCompletions<?>} on ACF's shared base class, so a handler can
 * only be typed against the platform context. The data itself comes from
 * {@code CompletionProviders}, which is shared and unit-tested.
 */
public final class BungeeCompletionsRegistrar {

    private BungeeCompletionsRegistrar() {
    }

    public static void register(BungeeCommandManagerImpl commandManager, FMessageCore core) {
        CommandCompletions<BungeeCommandCompletionContext> completions =
                commandManager.getCommandCompletions();

        completions.registerAsyncCompletion("groups", context -> {
            CommandSender sender = context.getSender();
            if (!(sender instanceof ProxiedPlayer player)) {
                return null;
            }
            return core.getCompletionProviders().groupsOf(player.getUniqueId());
        });

        completions.registerAsyncCompletion("owngroups", context -> {
            CommandSender sender = context.getSender();
            if (!(sender instanceof ProxiedPlayer player)) {
                return null;
            }
            return core.getCompletionProviders().ownedGroupsOf(player.getUniqueId());
        });
    }
}
