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

import java.util.Optional;
import java.util.UUID;

@CommandAlias("ignore")
public class IgnoreCommand extends FMessageCommand {

    public IgnoreCommand(FMessageCore core) {
        super(core);
    }

    @Default
    @CommandPermission("fmessage.ignore")
    @Description("{@@acf-fmessage.ignore_help_description}")
    @CommandCompletion("@players")
    @Syntax("[playerTargetName]")
    public void onIgnore(CommandIssuer issuer, String playerTargetName) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        Optional<ProxyPlayer> target = core.getPlatform().getPlayer(playerTargetName);

        // Falls back to the name directory so an offline player can be ignored too.
        UUID targetUuid = target
                .map(ProxyPlayer::getUniqueId)
                .orElseGet(() -> core.getPlayerCommandManager().getUuid(playerTargetName));

        if (targetUuid == null) {
            issuer.sendInfo(MessageKeys.IGNORE_NOT_EXIST, "{player}", playerTargetName);
            return;
        }

        // The protection node can only be read while the player is connected.
        if (target.isPresent() && target.get().hasPermission("fmessage.cannot_ignore")) {
            issuer.sendInfo(MessageKeys.CANNOT_IGNORE);
            return;
        }

        if (targetUuid.equals(sender.getUniqueId())) {
            issuer.sendInfo(MessageKeys.CANNOT_IGNORE);
            return;
        }

        if (core.getIgnoreCommandManager().ignoreExist(sender.getUniqueId(), targetUuid)) {
            issuer.sendInfo(MessageKeys.IGNORE_ALREADY, "{player}", playerTargetName);
            return;
        }

        core.getIgnoreCommandManager().addIgnore(sender.getUniqueId(), targetUuid);
        core.updateIgnores();

        issuer.sendInfo(MessageKeys.IGNORE_SUCCESS, "{player}", playerTargetName);
    }
}
