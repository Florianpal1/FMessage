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

@CommandAlias("m|msg")
public class MSGCommand extends FMessageCommand {

    public MSGCommand(FMessageCore core) {
        super(core);
    }

    @Default
    @CommandPermission("fmessage.msg")
    @Description("{@@acf-fmessage.msg_help_description}")
    @CommandCompletion("@players")
    @Syntax("[playerTargetName] [message]")
    public void onMSG(CommandIssuer issuer, String playerTargetName, String message) {
        ProxyPlayer sender = requirePlayer(issuer);
        if (sender == null) {
            return;
        }

        Optional<ProxyPlayer> target = core.getPlatform().getPlayer(playerTargetName);
        if (target.isEmpty()) {
            issuer.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        core.getPrivateMessageService().send(sender, target.get(), message);
    }
}
