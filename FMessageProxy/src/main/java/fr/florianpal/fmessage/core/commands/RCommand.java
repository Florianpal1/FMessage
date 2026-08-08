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
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Syntax;
import fr.florianpal.fmessage.core.FMessageCore;
import fr.florianpal.fmessage.core.languages.MessageKeys;
import fr.florianpal.fmessage.platform.ProxyPlayer;

import java.util.Optional;

@CommandAlias("r")
public class RCommand extends FMessageCommand {

    public RCommand(FMessageCore core) {
        super(core);
    }

    @Default
    @CommandPermission("fmessage.r")
    @Description("{@@acf-fmessage.r_help_description}")
    @Syntax("[message]")
    public void onR(CommandIssuer issuer, String message) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        if (!core.getSessionState().hasPreviousPlayer(sender.getUniqueId())) {
            issuer.sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
            return;
        }

        Optional<ProxyPlayer> target = core.getPrivateMessageService().replyTargetOf(sender);
        if (target.isEmpty()) {
            issuer.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        core.getPrivateMessageService().send(sender, target.get(), message);
    }
}
