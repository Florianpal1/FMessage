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

package fr.florianpal.fmessage.core.commands;

import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandCompletion;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@CommandAlias("unignore")
public class UnIgnoreCommand extends FMessageCommand {

    public UnIgnoreCommand(FMessageCore core) {
        super(core);
    }

    @Default
    @CommandPermission("fmessage.unignore")
    @Description("{@@acf-fmessage.unignore_help_description}")
    @CommandCompletion("@players")
    @Syntax("[playerTargetName]")
    public void onUnIgnore(CommandIssuer issuer, String playerTargetName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        Optional<ProxyPlayer> target = core.getPlatform().getPlayer(playerTargetName);

        // Falls back to the name directory: you can stop ignoring someone who is offline.
        UUID targetUuid = target
                .map(ProxyPlayer::getUniqueId)
                .orElseGet(() -> core.getPlayerCommandManager().getUuid(playerTargetName));

        if (targetUuid == null) {
            issuer.sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", playerTargetName);
            return;
        }

        if (!core.getIgnoreCommandManager().ignoreExist(sender.getUniqueId(), targetUuid)) {
            issuer.sendInfo(MessageKeys.UNIGNORE_ALREADY, "{player}", playerTargetName);
            return;
        }

        core.getIgnoreCommandManager().removeIgnore(sender.getUniqueId(), targetUuid);

        List<UUID> ignored = core.getIgnores().get(sender.getUniqueId());
        if (ignored != null) {
            ignored.remove(targetUuid);
        }

        issuer.sendInfo(MessageKeys.UNIGNORE_SUCCESS, "{player}", playerTargetName);
    }
}
