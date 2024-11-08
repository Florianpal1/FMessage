

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
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.MessageManager;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;

@CommandAlias("r")
public class RCommand extends BaseCommand {

    private final FMessage plugin;

    private final CommandManager commandManager;

    private final MessageManager messageManager;

    public RCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
        this.messageManager = plugin.getMessageManager();
    }

    @Default
    @CommandPermission("fmessage.r")
    @Description("{@@fmessage.r_help_description}")
    @Syntax("[message]")
    public void onR(Player playerSender, String message) {

        if (!plugin.havePreviousPlayer(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.NO_PREVIOUS_PLAYER);
            return;
        } else if (!plugin.isPreviousPlayerOnline(playerSender)) {
            CommandIssuer issuerTarget = commandManager.getCommandIssuer(playerSender);
            issuerTarget.sendInfo(MessageKeys.PLAYER_OFFLINE);
            return;
        }

        Player playerTarget = plugin.getPreviousPlayer(playerSender);
        messageManager.sendMessage(playerSender, playerTarget, message);
    }
}
