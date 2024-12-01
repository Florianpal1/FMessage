

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
 * Last modification : 07/01/2022 23:05
 *
 *  @author Florianpal.
 */

package fr.florianpal.fmessage.commands;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.CommandIssuer;
import co.aikar.commands.annotation.*;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.MessageManager;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;
import net.md_5.bungee.api.connection.ProxiedPlayer;

@CommandAlias("m|msg")
public class MSGCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;
    private final MessageManager messageManager;

    public MSGCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Default
    @CommandPermission("fmessage.msg")
    @Description("{@@fmessage.msg_help_description}")
    @CommandCompletion("@players")
    @Syntax("[groupName] [message]")
    public void onMSG(ProxiedPlayer playerSender, String playerTargetName, String message) {

        ProxiedPlayer playerTargetOptional = plugin.getProxy().getPlayer(playerTargetName);
        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);

        if (playerTargetOptional == null) {

            issuerSender.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }
        ProxiedPlayer playerTarget = playerTargetOptional;

        messageManager.sendMessage(playerSender, playerTarget, message);
    }
}
