

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
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Description;
import com.velocitypowered.api.proxy.Player;
import fr.florianpal.fmessage.FMessage;
import fr.florianpal.fmessage.languages.MessageKeys;
import fr.florianpal.fmessage.managers.commandManagers.CommandManager;

@CommandAlias("chatspy")
public class MSGSpyCommand extends BaseCommand {
    private final FMessage plugin;
    private final CommandManager commandManager;

    public MSGSpyCommand(FMessage plugin) {
        this.plugin = plugin;
        this.commandManager = plugin.getCommandManager();
    }

    @Default
    @CommandPermission("fmessage.chatspy")
    @Description("{@@fmessage.msgspy_help_description}")
    public void onMSGSpy(Player playerSender) {

        CommandIssuer issuerSender = commandManager.getCommandIssuer(playerSender);
        if (plugin.isPlayerSpy(playerSender.getUniqueId())) {
            plugin.removePlayerSpy(playerSender.getUniqueId());
            issuerSender.sendInfo(MessageKeys.SPY_DESACTIVATE);
            return;
        }

        plugin.addPlayerSpy(playerSender.getUniqueId());
        issuerSender.sendInfo(MessageKeys.SPY_ACTIVATE);
    }
}
